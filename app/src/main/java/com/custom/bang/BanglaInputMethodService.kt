package com.custom.bang

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.inputmethodservice.InputMethodService

class BanglaInputMethodService : InputMethodService() {

    private lateinit var rootView: View
    private lateinit var row1: LinearLayout
    private lateinit var row2: LinearLayout
    private lateinit var row3: LinearLayout
    private lateinit var row4: LinearLayout
    private lateinit var row5: LinearLayout

    private var isSymbolMode = false
    private var longPressPopup: PopupWindow? = null

    private val deleteHandler = Handler(Looper.getMainLooper())
    private var deleteRunnable: Runnable? = null

    private lateinit var clipboardHistory: ClipboardHistory

    // ==================================================================
    //  বাং / ABC টাইপিং মোড
    //   - BANGLA_PHONETIC : QWERTY লেআউট দেখায়, ইংরেজি লিখলে বাংলা তৈরি হয় ("বাং" বাটন)
    //   - ROMAN_REVERSE    : স্বাভাবিক বাংলা লেআউট (ডিফল্ট, ছবির মতো), স্পেসে বাংলা শব্দটি
    //                        রোমান বানানে বদলে যায় ("ABC" বাটন)
    // ==================================================================
    private enum class TypingMode { BANGLA_PHONETIC, ROMAN_REVERSE }
    private var typingMode = TypingMode.ROMAN_REVERSE

    // "বাং" মোডে টাইপ করা ইংরেজি অক্ষর জমা রাখার বাফার (কনভার্সন-এর আগে)
    private val phoneticBuffer = StringBuilder()

    // "ABC" মোডে চলতি শব্দে টাইপ করা প্রতিটি বাংলা key/যুক্তাক্ষর (রোমানে ফেরানোর জন্য)
    private val banglaTokens = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()
        clipboardHistory = ClipboardHistory(this)
        clipboardHistory.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardHistory.stop()
    }

    override fun onCreateInputView(): View {
        rootView = layoutInflater.inflate(R.layout.keyboard_view, null)

        row1 = rootView.findViewById(R.id.row1)
        row2 = rootView.findViewById(R.id.row2)
        row3 = rootView.findViewById(R.id.row3)
        row4 = rootView.findViewById(R.id.row4)
        row5 = rootView.findViewById(R.id.row5)

        setupTopBar()
        setupBottomRow()
        buildCharacterRows()

        // Wire up the bridges so results from the trampoline activities land here.
        VoiceInputBridge.onResult = { text -> commitText(text) }
        ImagePickerBridge.onImagePicked = { uri -> commitImage(uri) }

        return rootView
    }

    // ==================================================================
    //  Character rows (Bangla letters or symbols, depending on mode)
    // ==================================================================

    private fun buildCharacterRows() {
        row1.removeAllViews()
        row2.removeAllViews()
        row3.removeAllViews()
        row4.removeAllViews()
        row5.removeAllViews()

        when {
            isSymbolMode -> {
                KeyboardData.symbolsRow1.forEach { addSymbolKey(row1, it) }
                KeyboardData.symbolsRow2.forEach { addSymbolKey(row2, it) }
                KeyboardData.symbolsRow3.forEach { addSymbolKey(row3, it) }
                KeyboardData.symbolsRow4.forEach { addSymbolKey(row4, it) }
                KeyboardData.symbolsRow5.forEach { addSymbolKey(row5, it) }
                addDeleteKey(row5)
            }
            typingMode == TypingMode.BANGLA_PHONETIC -> {
                // "বাং" মোড: QWERTY লেআউট, ইংরেজি লিখলে বাংলায় রূপান্তর হবে
                KeyboardData.qwertyRow1.forEach { addPhoneticKey(row1, it) }
                KeyboardData.qwertyRow2.forEach { addPhoneticKey(row2, it) }
                KeyboardData.qwertyRow3.forEach { addPhoneticKey(row3, it) }
                addDeleteKey(row5)
            }
            else -> {
                // "ABC" মোড (ডিফল্ট): স্বাভাবিক বাংলা লেআউট, স্পেসে রোমান বানানে বদলে যাবে
                // সব সারিতে long-press চালু, যাতে কার-চিহ্ন (matra) ও হসন্ত-রূপ ব্যবহার করা যায়।
                KeyboardData.banglaRow1.forEach { addBanglaKey(row1, it, showLongPress = true) }
                KeyboardData.banglaRow2.forEach { addBanglaKey(row2, it, showLongPress = true) }
                KeyboardData.banglaRow3.forEach { addBanglaKey(row3, it, showLongPress = true) }
                KeyboardData.banglaRow4.forEach { addBanglaKey(row4, it, showLongPress = true) }
                KeyboardData.banglaRow5.forEach { addBanglaKey(row5, it, showLongPress = true) }
                addDeleteKey(row5)
            }
        }
    }

    /** সাধারণ বাংলা অক্ষরের কী: ট্যাপ করলে বাংলা বসে, ABC মোডে থাকলে রোমান-রূপান্তরের জন্য টোকেন জমা রাখে। */
    private fun addBanglaKey(row: LinearLayout, label: String, showLongPress: Boolean = false) {
        val key = TextView(android.view.ContextThemeWrapper(this, R.style.KeyboardKey), null, 0, R.style.KeyboardKey)
        key.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(3, 3, 3, 3)
        }
        key.text = label
        key.setOnClickListener {
            handleBanglaKeyPress(label)
            playClick()
        }
        val extras = KeyboardData.longPressMap[label]
        if (showLongPress && extras != null) {
            key.setOnLongClickListener {
                showLongPressPopup(key, listOf(label) + extras) { chosen -> handleBanglaKeyPress(chosen) }
                true
            }
        }
        row.addView(key)
    }

    /** একটি বাংলা অক্ষর/যুক্তাক্ষর কমিট করে; ABC মোডে থাকলে রোমান-বানান বের করার জন্য টোকেন জমা রাখে। */
    private fun handleBanglaKeyPress(text: String) {
        commitText(text)
        if (typingMode == TypingMode.ROMAN_REVERSE) {
            banglaTokens.add(text)
        }
    }

    /** ?123 সংখ্যা/চিহ্ন কী — সরাসরি কমিট হয়, কোনো রূপান্তর হয় না। */
    private fun addSymbolKey(row: LinearLayout, label: String) {
        val key = TextView(android.view.ContextThemeWrapper(this, R.style.KeyboardKey), null, 0, R.style.KeyboardKey)
        key.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(3, 3, 3, 3)
        }
        key.text = label
        key.setOnClickListener {
            finishCurrentWord()
            commitText(label)
            playClick()
        }
        row.addView(key)
    }

    /** "বাং" মোডের ইংরেজি কী: সরাসরি কমিট হয় না, বরং কম্পোজিং টেক্সট হিসেবে বাফারে জমা হয়। */
    private fun addPhoneticKey(row: LinearLayout, label: String) {
        val key = TextView(android.view.ContextThemeWrapper(this, R.style.KeyboardKey), null, 0, R.style.KeyboardKey)
        key.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(3, 3, 3, 3)
        }
        key.text = label
        key.setOnClickListener {
            phoneticBuffer.append(label)
            // লাইভ প্রিভিউ: টাইপ করার সাথে সাথে composing text এ ইংরেজি অক্ষর দেখানো হয়
            currentInputConnection?.setComposingText(phoneticBuffer.toString(), 1)
            playClick()
        }
        row.addView(key)
    }

    /**
     * চলতি শব্দ ফাইনালাইজ করে (স্পেস/এন্টার/মোড-পরিবর্তনের আগে ডাকা হয়):
     *  - বাং মোডে: বাফারের ইংরেজি বানান বাংলায় রূপান্তর করে কমিট করে।
     *  - ABC মোডে: চলতি বাংলা শব্দটি মুছে তার রোমান বানান বসিয়ে দেয়।
     */
    private fun finishCurrentWord() {
        val ic = currentInputConnection ?: return
        when (typingMode) {
            TypingMode.BANGLA_PHONETIC -> {
                if (phoneticBuffer.isNotEmpty()) {
                    val bangla = Transliterator.toBangla(phoneticBuffer.toString())
                    ic.setComposingText(bangla, 1)
                    ic.finishComposingText()
                    phoneticBuffer.clear()
                }
            }
            TypingMode.ROMAN_REVERSE -> {
                if (banglaTokens.isNotEmpty()) {
                    val totalLen = banglaTokens.sumOf { it.length }
                    val roman = banglaTokens.joinToString("") { Transliterator.romanFor(it) }
                    ic.deleteSurroundingText(totalLen, 0)
                    ic.commitText(roman, 1)
                    banglaTokens.clear()
                }
            }
        }
    }

    private fun addDeleteKey(row: LinearLayout) {
        val key = TextView(android.view.ContextThemeWrapper(this, R.style.KeyboardFunctionKey), null, 0, R.style.KeyboardFunctionKey)
        key.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(3, 3, 3, 3)
        }
        key.text = "⌫"
        key.textSize = 20f

        key.setOnClickListener {
            deleteOneChar()
            playClick()
        }
        key.setOnLongClickListener {
            startRepeatingDelete()
            true
        }
        key.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                stopRepeatingDelete()
            }
            false
        }
        row.addView(key)
    }

    private fun startRepeatingDelete() {
        stopRepeatingDelete()
        deleteRunnable = object : Runnable {
            override fun run() {
                deleteOneChar()
                deleteHandler.postDelayed(this, 60)
            }
        }
        deleteHandler.postDelayed(deleteRunnable!!, 300)
    }

    private fun stopRepeatingDelete() {
        deleteRunnable?.let { deleteHandler.removeCallbacks(it) }
        deleteRunnable = null
    }

    private fun deleteOneChar() {
        val ic = currentInputConnection ?: return

        // বাং মোডে বাফারে কিছু জমা থাকলে শুধু বাফারের শেষ অক্ষরটি মুছে composing text আপডেট করা হয়
        if (typingMode == TypingMode.BANGLA_PHONETIC && phoneticBuffer.isNotEmpty()) {
            phoneticBuffer.deleteCharAt(phoneticBuffer.length - 1)
            if (phoneticBuffer.isEmpty()) {
                ic.commitText("", 1)
                ic.finishComposingText()
            } else {
                ic.setComposingText(phoneticBuffer.toString(), 1)
            }
            return
        }

        // ABC মোডে চলতি শব্দের শেষ বাংলা key/যুক্তাক্ষরটি টোকেন-লিস্ট থেকে বাদ দিয়ে সেটুকুই মুছে ফেলা হয়
        if (typingMode == TypingMode.ROMAN_REVERSE && banglaTokens.isNotEmpty()) {
            val last = banglaTokens.removeAt(banglaTokens.size - 1)
            ic.deleteSurroundingText(last.length, 0)
            return
        }

        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    // ==================================================================
    //  Long-press popup for conjunct characters
    // ==================================================================

    private fun showLongPressPopup(anchor: View, options: List<String>, onChoose: (String) -> Unit) {
        dismissPopup()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.popup_background)
            setPadding(8, 8, 8, 8)
        }
        options.forEach { option ->
            val tv = TextView(this).apply {
                text = option
                textSize = 18f
                setTextColor(resources.getColor(R.color.white, theme))
                setPadding(28, 16, 28, 16)
                setOnClickListener {
                    onChoose(option)
                    dismissPopup()
                }
            }
            container.addView(tv)
        }
        val popup = PopupWindow(
            container,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        popup.isOutsideTouchable = true
        popup.showAsDropDown(anchor, 0, -(anchor.height * 2), Gravity.CENTER)
        longPressPopup = popup
    }

    private fun dismissPopup() {
        longPressPopup?.dismiss()
        longPressPopup = null
    }

    // ==================================================================
    //  Top bar
    // ==================================================================

    private fun setupTopBar() {
        rootView.findViewById<View>(R.id.btnGrid).setOnClickListener {
            openAppSettings()
        }
        rootView.findViewById<View>(R.id.btnImage).setOnClickListener {
            launchImagePicker(wantGif = false)
        }
        rootView.findViewById<View>(R.id.btnGif).setOnClickListener {
            launchImagePicker(wantGif = true)
        }
        rootView.findViewById<View>(R.id.btnClipboard).setOnClickListener { view ->
            showClipboardPopup(view)
        }
        rootView.findViewById<View>(R.id.btnHandwriting).setOnClickListener {
            Toast.makeText(this, "হাতের লেখা শীঘ্রই আসছে", Toast.LENGTH_SHORT).show()
        }
        rootView.findViewById<View>(R.id.btnMic).setOnClickListener {
            launchVoiceTyping()
        }
        rootView.findViewById<View>(R.id.btnSettings).setOnClickListener {
            openAppSettings()
        }

        // বাং / ABC টগল বাটন — সবচেয়ে গুরুত্বপূর্ণ ফিচার
        val btnMode = rootView.findViewById<TextView>(R.id.btnBanglaAbc)
        btnMode.text = if (typingMode == TypingMode.BANGLA_PHONETIC) "বাং" else "ABC"
        btnMode.setOnClickListener {
            finishCurrentWord() // মোড পাল্টানোর আগে চলতি শব্দটি ফাইনালাইজ করে নেওয়া হয়
            typingMode = if (typingMode == TypingMode.ROMAN_REVERSE) {
                TypingMode.BANGLA_PHONETIC
            } else {
                TypingMode.ROMAN_REVERSE
            }
            btnMode.text = if (typingMode == TypingMode.BANGLA_PHONETIC) "বাং" else "ABC"
            isSymbolMode = false
            buildCharacterRows()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun launchVoiceTyping() {
        val intent = Intent(this, VoiceTypingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun launchImagePicker(wantGif: Boolean) {
        val intent = Intent(this, ImagePickerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(ImagePickerActivity.EXTRA_WANT_GIF, wantGif)
        }
        startActivity(intent)
    }

    private fun showClipboardPopup(anchor: View) {
        val history = clipboardHistory.getHistory()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.popup_background)
            setPadding(16, 16, 16, 16)
        }
        if (history.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "কোনো ইতিহাস নেই"
                setTextColor(resources.getColor(R.color.white, theme))
                setPadding(8, 8, 8, 8)
            })
        } else {
            history.take(10).forEach { item ->
                container.addView(TextView(this).apply {
                    text = item
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setTextColor(resources.getColor(R.color.white, theme))
                    setPadding(8, 12, 8, 12)
                    setBackgroundResource(R.drawable.key_background_special)
                    setOnClickListener {
                        commitText(item)
                        dismissPopup()
                    }
                })
            }
        }
        val popup = PopupWindow(
            container,
            (resources.displayMetrics.widthPixels * 0.7).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.isOutsideTouchable = true
        popup.showAsDropDown(anchor, 0, 8)
        longPressPopup = popup
    }

    // ==================================================================
    //  Bottom row
    // ==================================================================

    private fun setupBottomRow() {
        val btn123 = rootView.findViewById<TextView>(R.id.btn123)
        btn123.setOnClickListener {
            finishCurrentWord()
            isSymbolMode = !isSymbolMode
            btn123.text = if (isSymbolMode) "বাংলা" else "?123"
            buildCharacterRows()
        }

        rootView.findViewById<View>(R.id.btnEmoji).setOnClickListener {
            // Hook an emoji panel/RecyclerView here. Left as a stub so the
            // button is wired up and ready for you to plug an emoji grid into.
            Toast.makeText(this, "ইমোজি প্যানেল যুক্ত করুন এখানে", Toast.LENGTH_SHORT).show()
        }

        rootView.findViewById<View>(R.id.btnGlobe).setOnClickListener {
            finishCurrentWord()
            switchToNextKeyboard()
        }
        rootView.findViewById<View>(R.id.btnGlobe).setOnLongClickListener {
            finishCurrentWord()
            switchToNextKeyboard()
            true
        }

        val space = rootView.findViewById<TextView>(R.id.btnSpace)
        space.setOnClickListener {
            // স্পেসে চাপ দিলেই বাং/ABC রূপান্তর ঘটে — এখানেই মূল জাদু
            finishCurrentWord()
            commitText(" ")
            playClick()
        }

        rootView.findViewById<View>(R.id.btnArrowLeft).setOnClickListener {
            finishCurrentWord()
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
        }
        rootView.findViewById<View>(R.id.btnArrowRight).setOnClickListener {
            finishCurrentWord()
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
        }

        rootView.findViewById<View>(R.id.btnEnter).setOnClickListener {
            performEnter()
        }
    }

    private fun switchToNextKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            imm.showInputMethodPicker()
        }
    }

    private fun performEnter() {
        finishCurrentWord()
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE &&
            editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION == 0
        ) {
            ic.performEditorAction(action)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    // ==================================================================
    //  Helpers
    // ==================================================================

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun commitImage(uri: Uri) {
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo ?: return
        val mimeTypes = editorInfo.contentMimeTypes
        val supportsImages = mimeTypes != null && mimeTypes.any { it.startsWith("image/") }

        if (!supportsImages) {
            Toast.makeText(this, "এই অ্যাপে সরাসরি ছবি পাঠানো সমর্থিত নয়", Toast.LENGTH_SHORT).show()
            return
        }

        grantUriPermission(
            editorInfo.packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val inputContentInfo = InputContentInfoCompat(
            uri,
            ClipDescription("image", arrayOf("image/png", "image/jpeg", "image/gif")),
            null
        )

        InputConnectionCompat.commitContent(ic, editorInfo, inputContentInfo, 0, null)
    }

    private fun playClick() {
        // Lightweight tactile/audio feedback hook; intentionally left silent
        // by default so the keyboard is not noisy. Hook up AudioManager /
        // Vibrator here if you want key-press feedback.
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        finishCurrentWord()
        super.onFinishInputView(finishingInput)
        dismissPopup()
        stopRepeatingDelete()
    }
}

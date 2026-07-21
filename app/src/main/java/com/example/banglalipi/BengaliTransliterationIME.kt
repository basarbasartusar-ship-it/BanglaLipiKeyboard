package com.example.banglalipi

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout

class BengaliTransliterationIME : InputMethodService() {

    private enum class Mode { BENGALI, ENGLISH }

    private lateinit var container: FrameLayout
    private var mode: Mode = Mode.BENGALI
    private var shiftOn: Boolean = false

    // Buffer of raw Bengali characters typed for the *current, uncommitted* word.
    private val buffer = StringBuilder()

    override fun onCreateInputView(): View {
        val root = LayoutInflater.from(this).inflate(R.layout.keyboard_view, null) as FrameLayout
        container = root
        buffer.clear()
        showLayout(mode)
        return root
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        buffer.clear()
    }

    private fun showLayout(newMode: Mode) {
        mode = newMode
        shiftOn = false
        container.removeAllViews()
        val layoutRes = if (mode == Mode.BENGALI) R.layout.keyboard_bengali_view else R.layout.keyboard_english_view
        val view = LayoutInflater.from(this).inflate(layoutRes, container, false)
        container.addView(view)
        if (mode == Mode.BENGALI) wireBengaliLayout(view) else wireEnglishLayout(view)
    }

    // ---------- Bengali layout wiring ----------

    private fun wireBengaliLayout(root: View) {
        forEachButtonWithTag(root) { button, tag ->
            button.setOnClickListener { handleBengaliChar(tag) }
        }
        root.findViewById<Button>(R.id.btnBackspace)?.setOnClickListener { handleBackspace() }
        root.findViewById<Button>(R.id.btnGlobe)?.setOnClickListener { showLayout(Mode.ENGLISH) }
        root.findViewById<Button>(R.id.btnComma)?.setOnClickListener { commitPunctuation(",") }
        root.findViewById<Button>(R.id.btnPeriod)?.setOnClickListener { commitPunctuation(".") }
        root.findViewById<Button>(R.id.btnSpace)?.setOnClickListener { handleSpace() }
        root.findViewById<Button>(R.id.btnEnter)?.setOnClickListener { handleEnter() }
    }

    private fun handleBengaliChar(ch: String) {
        val ic = currentInputConnection ?: return
        buffer.append(ch)
        ic.setComposingText(TransliterationEngine.transliterate(buffer.toString()), 1)
    }

    private fun commitBengaliBuffer(ic: InputConnection) {
        if (buffer.isNotEmpty()) {
            ic.finishComposingText()
            ic.commitText(TransliterationEngine.transliterate(buffer.toString()), 1)
            buffer.clear()
        }
    }

    // ---------- English layout wiring ----------

    private fun wireEnglishLayout(root: View) {
        forEachButtonWithTag(root) { button, tag ->
            button.setOnClickListener { handleEnglishChar(tag) }
        }
        root.findViewById<Button>(R.id.btnShift)?.setOnClickListener {
            shiftOn = !shiftOn
        }
        root.findViewById<Button>(R.id.btnBackspaceEn)?.setOnClickListener { handleBackspace() }
        root.findViewById<Button>(R.id.btnGlobeEn)?.setOnClickListener { showLayout(Mode.BENGALI) }
        root.findViewById<Button>(R.id.btnCommaEn)?.setOnClickListener { commitPunctuation(",") }
        root.findViewById<Button>(R.id.btnPeriodEn)?.setOnClickListener { commitPunctuation(".") }
        root.findViewById<Button>(R.id.btnSpaceEn)?.setOnClickListener { handleSpace() }
        root.findViewById<Button>(R.id.btnEnterEn)?.setOnClickListener { handleEnter() }
    }

    private fun handleEnglishChar(ch: String) {
        val ic = currentInputConnection ?: return
        val out = if (shiftOn) ch.uppercase() else ch
        ic.commitText(out, 1)
        if (shiftOn) shiftOn = false
    }

    // ---------- Shared controls ----------

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.BENGALI && buffer.isNotEmpty()) {
            buffer.deleteCharAt(buffer.length - 1)
            ic.setComposingText(TransliterationEngine.transliterate(buffer.toString()), 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.BENGALI) commitBengaliBuffer(ic)
        ic.commitText(" ", 1)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.BENGALI) commitBengaliBuffer(ic)
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun commitPunctuation(mark: String) {
        val ic = currentInputConnection ?: return
        if (mode == Mode.BENGALI) commitBengaliBuffer(ic)
        ic.commitText(mark, 1)
    }

    // ---------- Helpers ----------

    /** Walks the view tree and calls [action] for every Button that has a non-null tag. */
    private fun forEachButtonWithTag(view: View, action: (Button, String) -> Unit) {
        if (view is Button) {
            val tag = view.tag
            if (tag is String) action(view, tag)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                forEachButtonWithTag(view.getChildAt(i), action)
            }
        }
    }
}

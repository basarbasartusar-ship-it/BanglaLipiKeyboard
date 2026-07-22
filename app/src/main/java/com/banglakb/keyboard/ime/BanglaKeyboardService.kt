package com.banglakb.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData
import com.banglakb.keyboard.data.layouts.BanglishPhoneticEngine
import com.banglakb.keyboard.data.layouts.AlphabeticLayout
import com.banglakb.keyboard.data.layouts.BijoyLayout
import com.banglakb.keyboard.data.layouts.ProbhatLayout
import com.banglakb.keyboard.data.layouts.QwertyLayout
import com.banglakb.keyboard.db.AppDatabase
import com.banglakb.keyboard.repository.DictionaryRepository
import com.banglakb.keyboard.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The keyboard's InputMethodService. Owns keyboard-layout switching, key handling,
 * auto-capitalization / auto-spacing / double-space-period, live Banglish transliteration,
 * word-learning + suggestions, and incognito-mode awareness.
 */
class BanglaKeyboardService : InputMethodService(), KeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var suggestionBar: LinearLayout
    private lateinit var prefs: Prefs
    private lateinit var repository: DictionaryRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val availableLayouts by lazy {
        listOf(QwertyLayout.layout, ProbhatLayout.layout, AlphabeticLayout.layout, BijoyLayout.layout, banglishLayout())
    }
    private var currentLayoutIndex = 0
    private var showingSymbols = false
    private var isIncognito = false

    // Word/composing state
    private val wordBuffer = StringBuilder()

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        repository = DictionaryRepository(AppDatabase.getInstance(this).wordDao())
    }

    /** Banglish reuses the QWERTY key positions but is flagged phonetic so key handling differs. */
    private fun banglishLayout(): KeyboardLayoutData =
        QwertyLayout.layout.copy(id = "BANGLISH_PHONETIC", displayName = "Banglish", isPhonetic = true)

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(
            resources.getIdentifier("keyboard_container", "layout", packageName), null
        )
        suggestionBar = root.findViewById(
            resources.getIdentifier("suggestionBar", "id", packageName)
        )
        val container = root.findViewById<android.widget.FrameLayout>(
            resources.getIdentifier("keyboardContainer", "id", packageName)
        )

        keyboardView = KeyboardView(this)
        keyboardView.listener = this
        keyboardView.applyBackgroundForTheme()
        container.addView(keyboardView)

        restoreLayoutFromPrefs()
        renderCurrentLayout()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        isIncognito = (info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
        wordBuffer.clear()
        keyboardView.isShifted = shouldAutoCapitalizeNow()
        updateSuggestions("")
    }

    private fun restoreLayoutFromPrefs() {
        val idx = availableLayouts.indexOfFirst { it.id == prefs.currentLayoutId }
        currentLayoutIndex = if (idx >= 0) idx else 0
    }

    private fun renderCurrentLayout() {
        val data = if (showingSymbols) QwertyLayout.symbolsPage else availableLayouts[currentLayoutIndex]
        keyboardView.setKeyboardLayout(data)
    }

    // ---------------------------------------------------------------------
    // KeyboardActionListener
    // ---------------------------------------------------------------------

    override fun onKey(key: KeyModel, isShifted: Boolean) {
        val ic = currentInputConnection ?: return
        playSoundIfEnabled()

        when (key.type) {
            KeyType.CHARACTER -> handleCharacter(ic, key, isShifted)
            KeyType.SPACE -> handleSpace(ic)
            KeyType.ENTER -> handleEnter(ic)
            KeyType.BACKSPACE -> handleBackspace(ic)
            KeyType.SHIFT -> toggleShift()
            KeyType.SYMBOLS -> { showingSymbols = true; renderCurrentLayout() }
            KeyType.LETTERS -> { showingSymbols = false; renderCurrentLayout() }
            KeyType.LANG_SWITCH -> cycleLayout()
            KeyType.LAYOUT_SWITCH -> cycleLayout()
            KeyType.NUMBERS_TOGGLE -> { showingSymbols = !showingSymbols; renderCurrentLayout() }
            KeyType.EMOJI -> { /* Phase 2: open emoji panel */ }
        }
    }

    override fun onRepeatableBackspace() {
        currentInputConnection?.let { handleBackspace(it) }
    }

    // ---------------------------------------------------------------------
    // Key handling
    // ---------------------------------------------------------------------

    private fun handleCharacter(ic: InputConnection, key: KeyModel, isShifted: Boolean) {
        val currentLayout = availableLayouts[currentLayoutIndex]

        if (currentLayout.isPhonetic && !showingSymbols) {
            // Banglish: buffer latin chars, live-preview the Bangla transliteration as composing text.
            val ch = if (isShifted) key.shiftedOutput else key.output
            wordBuffer.append(ch)
            val preview = BanglishPhoneticEngine.transliterateWord(wordBuffer.toString())
            ic.setComposingText(preview, 1)
        } else {
            val output = if (isShifted) key.shiftedOutput else key.output
            ic.commitText(output, 1)
            if (!currentLayout.isPhonetic && output.firstOrNull()?.isLetter() == true) {
                wordBuffer.append(output)
                updateSuggestions(wordBuffer.toString())
            }
        }

        // Auto un-shift after a single character unless caps lock is on.
        if (keyboardView.isShifted && !keyboardView.isCapsLock) {
            keyboardView.isShifted = false
        }
    }

    private fun handleSpace(ic: InputConnection) {
        val currentLayout = availableLayouts[currentLayoutIndex]

        // Double-space-period: two spaces -> ". "
        if (prefs.doubleSpacePeriod) {
            val before = ic.getTextBeforeCursor(1, 0)?.toString()
            if (before == " ") {
                ic.deleteSurroundingText(1, 0)
                ic.commitText(". ", 1)
                finishWord(currentLayout)
                keyboardView.isShifted = true
                return
            }
        }

        if (currentLayout.isPhonetic && wordBuffer.isNotEmpty()) {
            ic.finishComposingText()
        }
        ic.commitText(" ", 1)
        finishWord(currentLayout)
        keyboardView.isShifted = shouldAutoCapitalizeNow()
    }

    private fun handleEnter(ic: InputConnection) {
        val currentLayout = availableLayouts[currentLayoutIndex]
        if (currentLayout.isPhonetic && wordBuffer.isNotEmpty()) ic.finishComposingText()
        finishWord(currentLayout)
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
    }

    private fun handleBackspace(ic: InputConnection) {
        if (wordBuffer.isNotEmpty()) {
            wordBuffer.deleteCharAt(wordBuffer.length - 1)
            val currentLayout = availableLayouts[currentLayoutIndex]
            if (currentLayout.isPhonetic) {
                if (wordBuffer.isEmpty()) {
                    ic.finishComposingText()
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.setComposingText(BanglishPhoneticEngine.transliterateWord(wordBuffer.toString()), 1)
                }
                return
            }
        }
        ic.deleteSurroundingText(1, 0)
    }

    private fun finishWord(layout: KeyboardLayoutData) {
        val word = wordBuffer.toString()
        wordBuffer.clear()
        if (word.isNotBlank()) {
            val lang = if (layout.id == "QWERTY_EN") "en" else "bn"
            serviceScope.launch { repository.learnWord(word, lang, isIncognito) }
        }
        updateSuggestions("")
    }

    private fun toggleShift() {
        if (keyboardView.isShifted && !keyboardView.isCapsLock) {
            keyboardView.isCapsLock = true
        } else if (keyboardView.isCapsLock) {
            keyboardView.isCapsLock = false
            keyboardView.isShifted = false
        } else {
            keyboardView.isShifted = true
        }
    }

    private fun cycleLayout() {
        showingSymbols = false
        currentLayoutIndex = (currentLayoutIndex + 1) % availableLayouts.size
        prefs.currentLayoutId = availableLayouts[currentLayoutIndex].id
        wordBuffer.clear()
        renderCurrentLayout()
    }

    private fun shouldAutoCapitalizeNow(): Boolean {
        if (!prefs.autoCapitalize) return false
        val ic = currentInputConnection ?: return false
        val before = ic.getTextBeforeCursor(2, 0)?.toString() ?: return true
        if (before.isEmpty()) return true
        val trimmed = before.trimEnd()
        return trimmed.isEmpty() || trimmed.last() in charArrayOf('.', '!', '?')
    }

    // ---------------------------------------------------------------------
    // Suggestions
    // ---------------------------------------------------------------------

    private fun updateSuggestions(prefix: String) {
        if (!prefs.wordSuggestions) {
            suggestionBar.removeAllViews()
            return
        }
        val layout = availableLayouts[currentLayoutIndex]
        val lang = if (layout.id == "QWERTY_EN") "en" else "bn"
        serviceScope.launch {
            val results = if (prefix.isBlank()) emptyList() else repository.suggestionsFor(prefix, lang)
            renderSuggestions(results)
        }
    }

    private fun renderSuggestions(words: List<String>) {
        suggestionBar.removeAllViews()
        for (word in words) {
            val tv = TextView(this).apply {
                text = word
                setPadding(24, 8, 24, 8)
                textSize = 16f
                setOnClickListener { replaceCurrentWord(word) }
            }
            suggestionBar.addView(tv)
        }
    }

    private fun replaceCurrentWord(word: String) {
        val ic = currentInputConnection ?: return
        val layout = availableLayouts[currentLayoutIndex]
        if (layout.isPhonetic) {
            ic.setComposingText(word, 1)
            ic.finishComposingText()
        } else {
            ic.deleteSurroundingText(wordBuffer.length, 0)
            ic.commitText(word, 1)
        }
        wordBuffer.clear()
        ic.commitText(" ", 1)
    }

    // ---------------------------------------------------------------------
    // Misc
    // ---------------------------------------------------------------------

    private fun playSoundIfEnabled() {
        if (!prefs.soundEnabled) return
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        wordBuffer.clear()
    }
}

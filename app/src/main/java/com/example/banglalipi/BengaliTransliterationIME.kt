package com.example.banglalipi

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection

class BengaliTransliterationIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private val buffer = StringBuilder()

    private val codeMap: Map<Int, String> = mapOf(
        -101 to "অ", -102 to "আ", -103 to "ই", -104 to "ঈ", -105 to "উ", -106 to "ঊ",
        -107 to "এ", -108 to "ঐ", -109 to "ও", -110 to "ঔ",

        -201 to "ক", -202 to "খ", -203 to "গ", -204 to "ঘ", -205 to "ঙ",
        -206 to "চ", -207 to "ছ", -208 to "জ", -209 to "ঝ", -210 to "ঞ",

        -211 to "ট", -212 to "ঠ", -213 to "ড", -214 to "ঢ", -215 to "ণ",
        -216 to "ত", -217 to "থ", -218 to "দ", -219 to "ধ", -220 to "ন",

        -221 to "প", -222 to "ফ", -223 to "ব", -224 to "ভ", -225 to "ম",
        -226 to "য", -227 to "র", -228 to "ল", -229 to "শ", -230 to "স",
        -231 to "হ",

        -301 to "া", -302 to "ি", -303 to "ী", -304 to "ু", -305 to "ে", -306 to "ো", -307 to "ৌ",
        -401 to "্"
    )

    override fun onCreateInputView(): View {
        keyboard = Keyboard(this, R.xml.keyboard_bengali)
        val view = LayoutInflater.from(this)
            .inflate(R.layout.keyboard_view, null) as KeyboardView
        view.keyboard = keyboard
        view.setOnKeyboardActionListener(this)
        keyboardView = view
        return view
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return

        when (primaryCode) {
            -1 -> {
                if (buffer.isNotEmpty()) {
                    buffer.deleteCharAt(buffer.length - 1)
                    ic.setComposingText(TransliterationEngine.transliterate(buffer.toString()), 1)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            32 -> {
                commitBuffer(ic)
                ic.commitText(" ", 1)
            }
            10 -> {
                commitBuffer(ic)
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            else -> {
                val bengaliChar = codeMap[primaryCode] ?: return
                buffer.append(bengaliChar)
                ic.setComposingText(TransliterationEngine.transliterate(buffer.toString()), 1)
            }
        }
    }

    private fun commitBuffer(ic: InputConnection) {
        if (buffer.isNotEmpty()) {
            ic.finishComposingText()
            ic.commitText(TransliterationEngine.transliterate(buffer.toString()), 1)
            buffer.clear()
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        buffer.clear()
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}

package com.banglakb.keyboard.data

/**
 * Functional role of a key. CHARACTER keys commit their [KeyModel.output] text directly;
 * the rest are handled specially by the IME service / KeyboardView.
 */
enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    SPACE,
    ENTER,
    SYMBOLS,   // toggles to symbols page
    LETTERS,   // toggles back to letters page from symbols page
    LANG_SWITCH,
    LAYOUT_SWITCH, // cycles Bijoy / Probhat / Banglish / QWERTY
    NUMBERS_TOGGLE,
    EMOJI
}

/**
 * A single key on the keyboard.
 *
 * @param label what is drawn on the key cap (may differ from [output], e.g. Shift icon)
 * @param output the literal text committed to the input field for CHARACTER keys
 * @param shiftedLabel label to show when Shift/Caps is active (defaults to [label])
 * @param shiftedOutput output to commit when Shift/Caps is active (defaults to [output])
 * @param widthWeight relative width of the key compared to a standard 1f key
 */
data class KeyModel(
    val label: String,
    val output: String = label,
    val type: KeyType = KeyType.CHARACTER,
    val shiftedLabel: String = label,
    val shiftedOutput: String = output,
    val widthWeight: Float = 1f
)

data class KeyboardLayoutData(
    val id: String,
    val displayName: String,
    val rows: List<List<KeyModel>>,
    /** true for phonetic engines (Banglish) that need post-processing instead of direct commit */
    val isPhonetic: Boolean = false
)

enum class LayoutId {
    QWERTY_EN,
    BANGLA_PROBHAT,
    BANGLA_BIJOY,
    BANGLISH_PHONETIC
}

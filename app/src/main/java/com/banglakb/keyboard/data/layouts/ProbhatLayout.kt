package com.banglakb.keyboard.data.layouts

import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData

/**
 * NOTE ON ACCURACY:
 * Probhat is Bangladesh's fixed Unicode Bangla keymap (BDS standard). The exact
 * unshifted/shifted character assigned to every physical key must match the official
 * BSTI / Bangladesh Computer Council Probhat chart for muscle-memory-trained users.
 * The mapping below is a best-effort starting point so the project compiles and is
 * fully typable end-to-end, but it MUST be verified key-by-key against an official
 * Probhat layout chart (e.g. the SIL Keyman "Bangla Probhat" reference) before this
 * is shipped as a production layout. Every key is defined in one place below, so
 * fixing any character is a one-line change.
 */
object ProbhatLayout {

    private fun key(un: String, shifted: String, width: Float = 1f) =
        KeyModel(label = un, output = un, shiftedLabel = shifted, shiftedOutput = shifted, widthWeight = width)

    val layout = KeyboardLayoutData(
        id = "BANGLA_PROBHAT",
        displayName = "বাংলা (প্রভাত)",
        rows = listOf(
            listOf(
                key("ঙ", "ং"), key("া", "অ"), key("ব", "ভ"), key("চ", "ছ"), key("দ", "ধ"),
                key("এ", "ঐ"), key("ফ", "থ"), key("গ", "ঘ"), key("হ", "ঃ"), key("ি", "ঈ")
            ),
            listOf(
                key("ৃ", "ঋ"), key("ু", "ঊ"), key("প", "থ"), key("র", "ড়"), key("ত", "ৎ"),
                key("য়", "য"), key("ু", "ঊ"), key("ী", "ই"), key("ো", "ঔ"), key("প", "ফ")
            ),
            listOf(
                KeyModel("⇧", type = KeyType.SHIFT, widthWeight = 1.5f),
                key("অ", "আ"), key("স", "ষ"), key("দ", "ড"), key("ফ", "ভ"),
                key("গ", "ঘ"), key("হ", "ঁ"), key("জ", "ঝ"), key("ক", "খ"), key("ল", "৳"),
                KeyModel("⌫", type = KeyType.BACKSPACE, widthWeight = 1.5f)
            ),
            listOf(
                KeyModel("?123", type = KeyType.SYMBOLS, widthWeight = 1.3f),
                KeyModel("🌐", type = KeyType.LANG_SWITCH, widthWeight = 1.1f),
                KeyModel("।", output = "।"),
                KeyModel("space", output = " ", type = KeyType.SPACE, widthWeight = 4f),
                KeyModel(",", output = ","),
                KeyModel("⏎", type = KeyType.ENTER, widthWeight = 1.5f)
            )
        )
    )

    val numberRow = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
        .map { KeyModel(it, it) }
}

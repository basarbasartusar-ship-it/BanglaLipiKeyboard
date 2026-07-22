package com.banglakb.keyboard.data.layouts

import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData

object AlphabeticLayout {

    private fun k(ch: String, width: Float = 1f) =
        KeyModel(label = ch, output = ch, widthWeight = width)

    val layout = KeyboardLayoutData(
        id = "BANGLA_ALPHABETIC",
        displayName = "বাংলা",
        rows = listOf(
            listOf(
                k("অ"), k("আ"), k("ই"), k("ঈ"), k("উ"),
                k("ঊ"), k("এ"), k("ঐ"), k("ও"), k("ঔ")
            ),
            listOf(
                k("ক"), k("খ"), k("গ"), k("ঘ"), k("ঙ"),
                k("চ"), k("ছ"), k("জ"), k("ঝ"), k("ঞ")
            ),
            listOf(
                k("ট"), k("ঠ"), k("ড"), k("ঢ"), k("ণ"),
                k("ত"), k("থ"), k("দ"), k("ধ"), k("ন")
            ),
            listOf(
                k("প"), k("ফ"), k("ব"), k("ভ"), k("ম"),
                k("য"), k("র"), k("ল"), k("শ"), k("ষ")
            ),
            listOf(
                k("স"), k("হ"), k("ড়"), k("ঢ়"), k("য়"),
                k("ৎ"), k("ৗ"), k("ঋ"), k("্"),
                KeyModel("⌫", type = KeyType.BACKSPACE, widthWeight = 1.3f)
            ),
            listOf(
                KeyModel("?123", type = KeyType.SYMBOLS, widthWeight = 1.2f),
                KeyModel("😊", type = KeyType.EMOJI, widthWeight = 1f),
                KeyModel("🌐", type = KeyType.LANG_SWITCH, widthWeight = 1.1f),
                KeyModel("space", output = " ", type = KeyType.SPACE, widthWeight = 4f),
                KeyModel(",", output = ","),
                KeyModel("⏎", type = KeyType.ENTER, widthWeight = 1.4f)
            )
        )
    )

    val vowelSigns = listOf("া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ")
        .map { KeyModel(it, it) }
}

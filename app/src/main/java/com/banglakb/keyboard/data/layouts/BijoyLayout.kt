package com.banglakb.keyboard.data.layouts

import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData

/**
 * NOTE ON ACCURACY: same caveat as ProbhatLayout.kt — this reproduces the Bijoy/UniBijoy
 * *key positions* (the layout Bangladeshi typists learn by muscle memory) but outputs
 * Unicode Bangla instead of legacy ANSI. Verify every key against an official
 * UniBijoy/Bijoy Bayanno chart before shipping; every key is defined in one place here.
 */
object BijoyLayout {

    private fun key(un: String, shifted: String, width: Float = 1f) =
        KeyModel(label = un, output = un, shiftedLabel = shifted, shiftedOutput = shifted, widthWeight = width)

    val layout = KeyboardLayoutData(
        id = "BANGLA_BIJOY",
        displayName = "বাংলা (বিজয়)",
        rows = listOf(
            listOf(
                key("ঙ", "ৎ"), key("া", "১"), key("ী", "২"), key("া", "৩"), key("্র", "৪"),
                key("এ", "৫"), key("ু", "৬"), key("ূ", "৭"), key("ব", "৮"), key("ও", "৯")
            ),
            listOf(
                key("ৈ", "ঐ"), key("ু", "ঊ"), key("ি", "ঈ"), key("ো", "ঔ"), key("প", "ফ"),
                key("র", "ড়"), key("ক", "খ"), key("ত", "থ"), key("ে", "ৈ"), key("ৌ", "ঔ")
            ),
            listOf(
                KeyModel("⇧", type = KeyType.SHIFT, widthWeight = 1.5f),
                key("দ", "ধ"), key("জ", "ঝ"), key("ষ", "শ"), key("ল", "৳"),
                key("গ", "ঘ"), key("হ", "ঁ"), key("চ", "ছ"), key("ম", "ং"), key("ন", "ণ"),
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
}

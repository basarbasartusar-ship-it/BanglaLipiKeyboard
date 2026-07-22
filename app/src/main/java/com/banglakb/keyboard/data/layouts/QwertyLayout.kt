package com.banglakb.keyboard.data.layouts

import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData

object QwertyLayout {

    private fun letter(c: Char) = KeyModel(
        label = c.toString(),
        output = c.toString(),
        shiftedLabel = c.uppercaseChar().toString(),
        shiftedOutput = c.uppercaseChar().toString()
    )

    val layout = KeyboardLayoutData(
        id = "QWERTY_EN",
        displayName = "English (QWERTY)",
        rows = listOf(
            "qwertyuiop".map { letter(it) },
            "asdfghjkl".map { letter(it) },
            listOf(KeyModel("⇧", type = KeyType.SHIFT, widthWeight = 1.5f)) +
                "zxcvbnm".map { letter(it) } +
                listOf(KeyModel("⌫", type = KeyType.BACKSPACE, widthWeight = 1.5f)),
            listOf(
                KeyModel("?123", type = KeyType.SYMBOLS, widthWeight = 1.3f),
                KeyModel("🌐", type = KeyType.LANG_SWITCH, widthWeight = 1.1f),
                KeyModel(",", output = ","),
                KeyModel("space", output = " ", type = KeyType.SPACE, widthWeight = 4f),
                KeyModel(".", output = "."),
                KeyModel("⏎", type = KeyType.ENTER, widthWeight = 1.5f)
            )
        )
    )

    val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        .map { KeyModel(it, it) }

    val symbolsPage = KeyboardLayoutData(
        id = "SYMBOLS",
        displayName = "Symbols",
        rows = listOf(
            numberRow,
            "@#\$_&-+()".map { KeyModel(it.toString(), it.toString()) },
            listOf(KeyModel("=\\<", type = KeyType.SHIFT, widthWeight = 1.5f)) +
                "*\"':;!?".map { KeyModel(it.toString(), it.toString()) } +
                listOf(KeyModel("⌫", type = KeyType.BACKSPACE, widthWeight = 1.5f)),
            listOf(
                KeyModel("ABC", type = KeyType.LETTERS, widthWeight = 1.3f),
                KeyModel("🌐", type = KeyType.LANG_SWITCH, widthWeight = 1.1f),
                KeyModel(",", output = ","),
                KeyModel("space", output = " ", type = KeyType.SPACE, widthWeight = 4f),
                KeyModel(".", output = "."),
                KeyModel("⏎", type = KeyType.ENTER, widthWeight = 1.5f)
            )
        )
    )
}

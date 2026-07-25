package com.custom.bang

/**
 * Static keyboard layout data: the Bangla character rows, the numeric/symbol
 * page, and the extra characters shown in the long-press popup for each key.
 */
object KeyboardData {

    // ---------- Bangla layout (5 rows) ----------
    val banglaRow1 = listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "এ", "ঐ", "ও", "ঔ")
    val banglaRow2 = listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ")
    val banglaRow3 = listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন")
    val banglaRow4 = listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ")
    // Row 5 has 9 characters; the 10th slot is the Delete key (handled separately in the service).
    val banglaRow5 = listOf("স", "হ", "ড়", "ঢ়", "য়", "ং", "ঃ", "ঁ", "ৃ")

    // ---------- ইংরেজি QWERTY লেআউট ("বাং" ফোনেটিক মোডের জন্য) ----------
    // এখানে টাইপ করা ইংরেজি বর্ণ Transliterator.toBangla() দিয়ে বাংলায় রূপান্তর হয়।
    val qwertyRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val qwertyRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val qwertyRow3 = listOf("z", "x", "c", "v", "b", "n", "m")

    // ---------- Numeric / symbol layout (?123) ----------
    val symbolsRow1 = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
    val symbolsRow2 = listOf("@", "#", "৳", "&", "*", "-", "+", "(", ")", "/")
    val symbolsRow3 = listOf("!", "\"", "'", ":", ";", "?", "%", "_", "=", "~")
    val symbolsRow4 = listOf(",", ".", "।", "-", "+", "=", "/", ":", ";", "!")
    val symbolsRow5 = listOf("[", "]", "{", "}", "<", ">", "$", "^", "\\")

    /**
     * Extra characters offered on long-press, keyed by the base character.
     * This is a representative set of common conjuncts (যুক্তাক্ষর) — extend
     * freely with more entries as needed.
     */
    val longPressMap: Map<String, List<String>> = mapOf(
        "ক" to listOf("ক্ক", "ক্ত", "ক্র", "ক্ষ", "ক্ল"),
        "খ" to listOf("খ্য", "খ্র"),
        "গ" to listOf("গ্গ", "গ্র", "গ্ল", "জ্ঞ"),
        "ঘ" to listOf("ঘ্র"),
        "চ" to listOf("চ্চ", "চ্ছ"),
        "ছ" to listOf("ছ্য"),
        "জ" to listOf("জ্জ", "জ্ঞ", "জ্র"),
        "ঝ" to listOf("ঝ্য"),
        "ট" to listOf("ট্ট", "ট্র"),
        "ড" to listOf("ড্ড", "ড্র"),
        "ণ" to listOf("ণ্ট", "ণ্ঠ", "ণ্ড"),
        "ত" to listOf("ত্ত", "ত্র", "ত্ন", "ত্ম"),
        "থ" to listOf("থ্র"),
        "দ" to listOf("দ্দ", "দ্ধ", "দ্ব", "দ্ম", "দ্র"),
        "ধ" to listOf("ধ্র", "ধ্ব"),
        "ন" to listOf("ন্ত", "ন্দ", "ন্ন", "ন্ম", "ন্স", "ন্ট"),
        "প" to listOf("প্প", "প্র", "প্ল", "প্ট"),
        "ফ" to listOf("ফ্র", "ফ্ল"),
        "ব" to listOf("ব্ব", "ব্র", "ব্ল"),
        "ভ" to listOf("ভ্র"),
        "ম" to listOf("ম্ম", "ম্প", "ম্ব", "ম্র", "ম্ল"),
        "য" to listOf("য়", "্য"),
        "র" to listOf("র্", "্র"),
        "ল" to listOf("ল্ল", "ল্প", "ল্ক"),
        "শ" to listOf("শ্চ", "শ্ব", "শ্র"),
        "ষ" to listOf("ষ্ট", "ষ্ঠ", "ষ্ণ"),
        "স" to listOf("স্ত", "স্ট", "স্প", "স্ক", "স্র"),
        "হ" to listOf("হ্ন", "হ্ম", "হ্র", "হ্ল"),
        "অ" to listOf("আ"),
        "ই" to listOf("ঈ"),
        "উ" to listOf("ঊ"),
        "এ" to listOf("ঐ"),
        "ও" to listOf("ঔ")
    )
}

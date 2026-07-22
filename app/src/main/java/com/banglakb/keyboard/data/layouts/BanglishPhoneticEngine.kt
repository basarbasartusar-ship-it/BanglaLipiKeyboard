package com.banglakb.keyboard.data.layouts

/**
 * A rule-based Latin-to-Bangla phonetic transliterator, inspired by Avro Phonetic.
 *
 * Coverage note: this implements the common consonant/vowel/conjunct rules that cover the
 * large majority of everyday Banglish typing ("ami banglay likhi" -> "আমি বাংলায় লিখি"),
 * but it is NOT a full reimplementation of Avro's ~100-rule grammar (context-sensitive
 * exceptions, English loanword handling, etc. are not all covered). It is structured so
 * additional rules can be added to [consonants] / [vowels] / [specialWords] independently.
 */
object BanglishPhoneticEngine {

    // Longest-match-first special whole-word overrides (common words/exceptions).
    private val specialWords = mapOf(
        "ami" to "আমি", "amar" to "আমার", "tumi" to "তুমি", "tomar" to "তোমার",
        "apni" to "আপনি", "apnar" to "আপনার", "ki" to "কি", "kemon" to "কেমন",
        "acho" to "আছো", "achen" to "আছেন", "bhalo" to "ভালো", "na" to "না",
        "hae" to "হ্যাঁ", "ha" to "হ্যাঁ", "kotha" to "কথা", "din" to "দিন",
        "rat" to "রাত", "bangla" to "বাংলা", "bangladesh" to "বাংলাদেশ"
    )

    // Multi-letter consonant clusters must be checked before single letters (longest match first).
    private val consonantsOrdered: List<Pair<String, String>> = listOf(
        "kkh" to "ক্ষ", "gg" to "জ্ঞ",
        "kh" to "খ", "gh" to "ঘ", "ng" to "ঙ",
        "chh" to "ছ", "ch" to "চ", "jh" to "ঝ",
        "Th" to "ঠ", "th" to "থ", "Dh" to "ঢ", "dh" to "ধ",
        "ph" to "ফ", "bh" to "ভ", "Rh" to "ঢ়",
        "sh" to "শ", "Sh" to "ষ",
        "k" to "ক", "g" to "গ", "j" to "জ", "T" to "ট", "D" to "ড",
        "N" to "ণ", "t" to "ত", "d" to "দ", "n" to "ন", "p" to "প",
        "f" to "ফ", "b" to "ব", "v" to "ভ", "m" to "ম", "z" to "জ",
        "r" to "র", "R" to "ড়", "l" to "ল", "s" to "স", "S" to "শ",
        "h" to "হ", "y" to "য়", "w" to "ও", "x" to "ক্স", "q" to "ক"
    )

    // Independent vowel forms (used word-initially / after another vowel).
    private val vowelIndependent: List<Pair<String, String>> = listOf(
        "oi" to "ঐ", "OI" to "ঐ", "ou" to "ঔ", "OU" to "ঔ",
        "rri" to "ঋ", "ee" to "ঈ", "oo" to "ঊ",
        "a" to "আ", "i" to "ই", "I" to "ঈ", "u" to "উ", "U" to "ঊ",
        "e" to "এ", "o" to "ও"
    )

    // Dependent vowel signs (matra) used after a consonant.
    private val vowelMatra: List<Pair<String, String>> = listOf(
        "oi" to "ৈ", "OI" to "ৈ", "ou" to "ৌ", "OU" to "ৌ",
        "rri" to "ৃ", "ee" to "ী", "oo" to "ূ",
        "a" to "া", "i" to "ি", "I" to "ী", "u" to "ু", "U" to "ূ",
        "e" to "ে", "o" to "ো"
    )

    private val hasoontChar = '্'

    /** Converts one Banglish (romanized) word into Bangla script. */
    fun transliterateWord(input: String): String {
        if (input.isEmpty()) return input
        specialWords[input.lowercase()]?.let { return it }

        val out = StringBuilder()
        var i = 0
        var lastWasConsonant = false

        while (i < input.length) {
            val remaining = input.substring(i)

            // Try vowels first when at word-start or right after another vowel.
            val vowelList = if (lastWasConsonant) vowelMatra else vowelIndependent
            val vowelMatch = vowelList.firstOrNull { remaining.startsWith(it.first) }
            if (vowelMatch != null) {
                out.append(vowelMatch.second)
                i += vowelMatch.first.length
                lastWasConsonant = false
                continue
            }

            // Then consonants (longest match first, as ordered above).
            val consMatch = consonantsOrdered.firstOrNull { remaining.startsWith(it.first) }
            if (consMatch != null) {
                // If the previous character was also a consonant with no vowel between them,
                // insert a hasoont (্) to form a conjunct, e.g. "kt" -> ক্ত
                if (lastWasConsonant) out.append(hasoontChar)
                out.append(consMatch.second)
                i += consMatch.first.length
                lastWasConsonant = true
                continue
            }

            // Unknown character (digit, punctuation, non-latin): pass through as-is.
            out.append(remaining[0])
            i += 1
            lastWasConsonant = false
        }
        return out.toString()
    }

    /** Splits on whitespace, transliterates each token, and rejoins. */
    fun transliterate(text: String): String =
        text.split(" ").joinToString(" ") { token ->
            if (token.isBlank()) token else transliterateWord(token)
        }
}

package com.example.banglalipi

object TransliterationEngine {

    private val INDEPENDENT_VOWELS = mapOf(
        'অ' to "o", 'আ' to "a", 'ই' to "i", 'ঈ' to "i", 'উ' to "u", 'ঊ' to "u",
        'ঋ' to "ri", 'এ' to "e", 'ঐ' to "oi", 'ও' to "o", 'ঔ' to "ou"
    )

    private val VOWEL_SIGNS = mapOf(
        'া' to "a", 'ি' to "i", 'ী' to "i", 'ু' to "u", 'ূ' to "u",
        'ৃ' to "ri", 'ে' to "e", 'ৈ' to "oi", 'ো' to "o", 'ৌ' to "ou"
    )

    private val CONSONANTS = mapOf(
        'ক' to "k", 'খ' to "kh", 'গ' to "g", 'ঘ' to "gh", 'ঙ' to "ng",
        'চ' to "ch", 'ছ' to "ch", 'জ' to "j", 'ঝ' to "jh", 'ঞ' to "n",
        'ট' to "t", 'ঠ' to "th", 'ড' to "d", 'ঢ' to "dh", 'ণ' to "n",
        'ত' to "t", 'থ' to "th", 'দ' to "d", 'ধ' to "dh", 'ন' to "n",
        'প' to "p", 'ফ' to "ph", 'ব' to "b", 'ভ' to "bh", 'ম' to "m",
        'য' to "j", 'র' to "r", 'ল' to "l", 'শ' to "sh", 'ষ' to "sh",
        'স' to "s", 'হ' to "h", 'ড়' to "r", 'ঢ়' to "rh", 'য়' to "y", 'ৎ' to "t"
    )

    private const val VIRAMA = '্'

    /** Converts a buffered Bengali string into its phonetic Roman spelling. */
    fun transliterate(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            val cons = CONSONANTS[ch]
            if (cons != null) {
                val next = text.getOrNull(i + 1)
                when {
                    next != null && VOWEL_SIGNS.containsKey(next) -> {
                        sb.append(cons).append(VOWEL_SIGNS.getValue(next))
                        i += 2
                    }
                    next == VIRAMA -> {
                        sb.append(cons)
                        i += 2 // drop the virama; next consonant joins with no vowel
                    }
                    else -> {
                        sb.append(cons).append("o")
                        i += 1
                    }
                }
                continue
            }
            val vow = INDEPENDENT_VOWELS[ch]
            if (vow != null) {
                sb.append(vow)
                i++
                continue
            }
            sb.append(ch)
            i++
        }
        return sb.toString()
    }
}

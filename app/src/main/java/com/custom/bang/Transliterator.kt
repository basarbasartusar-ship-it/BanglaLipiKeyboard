package com.custom.bang

/**
 * বাংলা ফোনেটিক ট্রান্সলিটারেশন ইঞ্জিন (অভ্র/Ridmik স্টাইল)
 *
 * ১) toBangla()   -> ইংরেজি বানান লিখলে বাংলা তৈরি করে ("kemne" -> "কেমনে")   [বাং মোড]
 * ২) romanFor()   -> একটি বাংলা অক্ষর/যুক্তাক্ষরের রোমান বানান বের করে       [ABC মোড]
 *
 * নোট: বড়হাতের অক্ষর (T, D, N, S, R) মূর্ধন্য/ট-বর্গীয় ধ্বনি বোঝায় (ট, ড, ণ, ষ, ড়)
 *      আর ছোটহাতের অক্ষর (t, d, n, s, r) দন্ত্য ধ্বনি বোঝায় (ত, দ, ন, স, র) — ঠিক অভ্রর মতো।
 */
object Transliterator {

    // ---- কিছু কমন/কথ্য শব্দ সরাসরি ম্যাপ করা আছে, যাতে নির্ভুল আউটপুট পাওয়া যায় ----
    private val wordDictionary = mapOf(
        "ami" to "আমি", "tumi" to "তুমি", "apni" to "আপনি", "amra" to "আমরা", "tara" to "তারা",
        "tomake" to "তোমাকে", "tomar" to "তোমার", "amar" to "আমার", "tader" to "তাদের",
        "bhalobasi" to "ভালোবাসি", "bhalobasha" to "ভালোবাসা", "bhalo" to "ভালো", "valo" to "ভালো",
        "kemon" to "কেমন", "kemne" to "কেমনে", "kivabe" to "কিভাবে", "kothay" to "কোথায়",
        "bolbo" to "বলবো", "korbo" to "করবো", "hobe" to "হবে", "jabo" to "যাবো", "asbo" to "আসবো",
        "acho" to "আছো", "achi" to "আছি", "ache" to "আছে", "chilo" to "ছিলো",
        "ki" to "কি", "na" to "না", "hae" to "হ্যাঁ", "hya" to "হ্যাঁ", "keno" to "কেন",
        "dhonnobad" to "ধন্যবাদ", "shuvo" to "শুভ", "jonmodin" to "জন্মদিন", "valobasha" to "ভালোবাসা",
        "bondhu" to "বন্ধু", "bhai" to "ভাই", "apu" to "আপু", "sokal" to "সকাল", "raat" to "রাত",
        "kotha" to "কথা", "bolo" to "বলো", "shono" to "শোনো", "dekho" to "দেখো", "chai" to "চাই",
        "onek" to "অনেক", "khub" to "খুব", "sundor" to "সুন্দর", "mishti" to "মিষ্টি"
    )

    // ---- ব্যঞ্জনবর্ণের ফোনেটিক ম্যাপ (দীর্ঘ মিল আগে চেক হয়) ----
    private val consonants = listOf(
        "kkh" to "ক্ষ", "gg" to "জ্ঞ",
        "kh" to "খ", "gh" to "ঘ", "Ng" to "ঙ", "ng" to "ঙ",
        "ch" to "চ", "Ch" to "ছ", "chh" to "ছ",
        "jh" to "ঝ", "NG" to "ঞ",
        "Th" to "ঠ", "Dh" to "ঢ", "th" to "থ", "dh" to "ধ",
        "ph" to "ফ", "bh" to "ভ", "sh" to "শ", "Rh" to "ঢ়",
        "k" to "ক", "g" to "গ", "c" to "চ", "j" to "জ", "z" to "জ",
        "T" to "ট", "D" to "ড", "N" to "ণ", "t" to "ত", "d" to "দ",
        "n" to "ন", "p" to "প", "f" to "ফ", "b" to "ব", "v" to "ভ",
        "m" to "ম", "r" to "র", "R" to "ড়", "l" to "ল",
        "S" to "ষ", "s" to "স", "h" to "হ", "y" to "য়", "Y" to "য়"
    ).sortedByDescending { it.first.length }

    // ---- স্বাধীন স্বরবর্ণ (শব্দের শুরুতে/আরেকটি স্বরের পরে) ----
    private val independentVowels = listOf(
        "rri" to "ঋ", "aa" to "আ", "A" to "আ", "ii" to "ঈ", "I" to "ঈ", "ee" to "ঈ",
        "uu" to "ঊ", "U" to "ঊ", "oo" to "ঊ", "oi" to "ঐ", "ou" to "ঔ", "ow" to "ঔ",
        "a" to "অ", "i" to "ই", "u" to "উ", "e" to "এ", "o" to "ও"
    ).sortedByDescending { it.first.length }

    // ---- কার চিহ্ন (ব্যঞ্জনবর্ণের ঠিক পরে বসে) ----
    private val karSigns = listOf(
        "rri" to "ৃ", "aa" to "া", "A" to "া", "ii" to "ী", "I" to "ী", "ee" to "ী",
        "uu" to "ূ", "U" to "ূ", "oo" to "ূ", "oi" to "ৈ", "ou" to "ৌ", "ow" to "ৌ",
        "a" to "", "i" to "ি", "u" to "ু", "e" to "ে", "o" to "ো"
    ).sortedByDescending { it.first.length }

    /** ইংরেজি ফোনেটিক লেখা → বাংলা (যেমন "kemne bolbo" এর প্রতিটি শব্দ) */
    fun toBangla(input: String): String {
        if (input.isEmpty()) return input
        wordDictionary[input.lowercase()]?.let { return it }

        val result = StringBuilder()
        var i = 0
        var lastConsonantRoman: String? = null

        while (i < input.length) {
            // প্রথমে ব্যঞ্জনবর্ণ মেলানোর চেষ্টা
            val cMatch = consonants.firstOrNull { input.startsWith(it.first, i) }
            if (cMatch != null) {
                val (roman, bangla) = cMatch
                // একই ব্যঞ্জনবর্ণ পরপর দুইবার এলে (kk, tt...) মাঝে হসন্ত বসে (যুক্তাক্ষর)
                if (lastConsonantRoman == roman) result.append("্")
                result.append(bangla)
                i += roman.length
                lastConsonantRoman = roman

                // ব্যঞ্জনবর্ণের পরপরই স্বরবর্ণ থাকলে সেটা কার-চিহ্ন হিসেবে বসে
                val vMatch = karSigns.firstOrNull { input.startsWith(it.first, i) }
                if (vMatch != null) {
                    result.append(vMatch.second)
                    i += vMatch.first.length
                    lastConsonantRoman = null
                }
                continue
            }

            // স্বাধীন স্বরবর্ণ (ব্যঞ্জনবর্ণ ছাড়া শব্দের শুরুতে বা আরেকটি স্বরের পরে)
            val vMatch = independentVowels.firstOrNull { input.startsWith(it.first, i) }
            if (vMatch != null) {
                result.append(vMatch.second)
                i += vMatch.first.length
                lastConsonantRoman = null
                continue
            }

            // অজানা ক্যারেক্টার (সংখ্যা/চিহ্ন) — যেমন আছে তেমনই রেখে দেওয়া
            result.append(input[i])
            i++
            lastConsonantRoman = null
        }
        return result.toString()
    }

    // ---- বাংলা অক্ষর/যুক্তাক্ষর → রোমান বানান (ABC মোডের জন্য বিপরীত ম্যাপ) ----
    private val banglaToRomanMap: Map<String, String> = mapOf(
        "অ" to "o", "আ" to "a", "ই" to "i", "ঈ" to "ee", "উ" to "u", "ঊ" to "oo",
        "এ" to "e", "ঐ" to "oi", "ও" to "o", "ঔ" to "ou",
        "ক" to "k", "খ" to "kh", "গ" to "g", "ঘ" to "gh", "ঙ" to "ng",
        "চ" to "ch", "ছ" to "chh", "জ" to "j", "ঝ" to "jh", "ঞ" to "NG",
        "ট" to "T", "ঠ" to "Th", "ড" to "D", "ঢ" to "Dh", "ণ" to "N",
        "ত" to "t", "থ" to "th", "দ" to "d", "ধ" to "dh", "ন" to "n",
        "প" to "p", "ফ" to "ph", "ব" to "b", "ভ" to "bh", "ম" to "m",
        "য" to "z", "র" to "r", "ল" to "l", "শ" to "sh", "ষ" to "S",
        "স" to "s", "হ" to "h", "ড়" to "R", "ঢ়" to "Rh", "য়" to "y",
        "ং" to "ng", "ঃ" to "h", "ঁ" to "N", "ৃ" to "ri",
        // দীর্ঘ যুক্তাক্ষর (লং-প্রেস থেকে আসা)
        "ক্ক" to "kko", "ক্ত" to "kto", "ক্র" to "kro", "ক্ষ" to "kkho", "ক্ল" to "klo",
        "খ্য" to "khyo", "খ্র" to "khro", "গ্গ" to "ggo", "গ্র" to "gro", "গ্ল" to "glo",
        "জ্ঞ" to "gg", "ঘ্র" to "ghro", "চ্চ" to "ccho", "চ্ছ" to "chchho",
        "জ্জ" to "jjo", "জ্র" to "jro", "ট্ট" to "TTo", "ট্র" to "Tro",
        "ড্ড" to "DDo", "ড্র" to "Dro", "ণ্ট" to "NTo", "ণ্ঠ" to "NTho", "ণ্ড" to "NDo",
        "ত্ত" to "tto", "ত্র" to "tro", "ত্ন" to "tno", "ত্ম" to "tmo",
        "দ্দ" to "ddo", "দ্ধ" to "ddho", "দ্ব" to "dbo", "দ্ম" to "dmo", "দ্র" to "dro",
        "ন্ত" to "nto", "ন্দ" to "ndo", "ন্ন" to "nno", "ন্ম" to "nmo", "ন্স" to "nso", "ন্ট" to "nTo",
        "প্প" to "ppo", "প্র" to "pro", "প্ল" to "plo", "প্ট" to "pTo",
        "ব্ব" to "bbo", "ব্র" to "bro", "ব্ল" to "blo", "ভ্র" to "bhro",
        "ম্ম" to "mmo", "ম্প" to "mpo", "ম্ব" to "mbo", "ম্র" to "mro", "ম্ল" to "mlo",
        "্য" to "y", "র্" to "r", "্র" to "r",
        "ল্ল" to "llo", "ল্প" to "lpo", "ল্ক" to "lko",
        "শ্চ" to "shcho", "শ্ব" to "shbo", "শ্র" to "shro",
        "ষ্ট" to "shTo", "ষ্ঠ" to "shTho", "ষ্ণ" to "shNo",
        "স্ত" to "sto", "স্ট" to "sTo", "স্প" to "spo", "স্ক" to "sko", "স্র" to "sro",
        "হ্ন" to "hno", "হ্ম" to "hmo", "হ্র" to "hro", "হ্ল" to "hlo"
    )

    /** একটি বাংলা key/যুক্তাক্ষরের রোমান বানান বের করে (না পেলে যা আছে তাই ফেরত দেয়) */
    fun romanFor(banglaKey: String): String = banglaToRomanMap[banglaKey] ?: banglaKey
}

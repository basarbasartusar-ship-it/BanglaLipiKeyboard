package com.banglakb.keyboard

import com.banglakb.keyboard.data.layouts.BanglishPhoneticEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class BanglishPhoneticEngineTest {

    @Test
    fun `special word ami transliterates correctly`() {
        assertEquals("আমি", BanglishPhoneticEngine.transliterateWord("ami"))
    }

    @Test
    fun `special word bangla transliterates correctly`() {
        assertEquals("বাংলা", BanglishPhoneticEngine.transliterateWord("bangla"))
    }

    @Test
    fun `simple consonant plus vowel forms correct syllable`() {
        // k + a -> ক + া = কা
        assertEquals("কা", BanglishPhoneticEngine.transliterateWord("ka"))
    }

    @Test
    fun `consonant cluster produces hasoont conjunct`() {
        // k + t (no vowel between) -> ক্ত
        val result = BanglishPhoneticEngine.transliterateWord("kt")
        assert(result.contains('্')) { "Expected hasoont in conjunct, got $result" }
    }

    @Test
    fun `sentence level transliteration splits on spaces`() {
        val result = BanglishPhoneticEngine.transliterate("ami bangla")
        assertEquals("আমি বাংলা", result)
    }

    @Test
    fun `unknown punctuation passes through unchanged`() {
        assertEquals("!", BanglishPhoneticEngine.transliterateWord("!"))
    }
}

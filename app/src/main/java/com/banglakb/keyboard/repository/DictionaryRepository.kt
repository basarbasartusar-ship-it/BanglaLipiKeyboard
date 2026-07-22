package com.banglakb.keyboard.repository

import com.banglakb.keyboard.db.WordDao
import com.banglakb.keyboard.db.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for word suggestions, incognito-aware learning, and the
 * user-managed Personal Dictionary. Incognito mode simply skips [learnWord].
 */
class DictionaryRepository(private val wordDao: WordDao) {

    fun personalDictionary(): Flow<List<WordEntity>> = wordDao.getPersonalDictionary()

    suspend fun suggestionsFor(prefix: String, language: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        return wordDao.getSuggestions(prefix, language).map { it.word }
    }

    /** Called after the user commits a word (types space/punctuation). Skipped in incognito mode. */
    suspend fun learnWord(word: String, language: String, incognito: Boolean) {
        if (incognito || word.isBlank() || word.length < 2) return
        val existing = wordDao.findWord(word, language)
        if (existing != null) {
            wordDao.update(existing.copy(frequency = existing.frequency + 1, lastUsed = System.currentTimeMillis()))
        } else {
            wordDao.insert(WordEntity(word = word, language = language, frequency = 1, isUserAdded = false))
        }
    }

    suspend fun addToPersonalDictionary(word: String, language: String) {
        val existing = wordDao.findWord(word, language)
        if (existing != null) {
            wordDao.update(existing.copy(isUserAdded = true))
        } else {
            wordDao.insert(WordEntity(word = word, language = language, frequency = 5, isUserAdded = true))
        }
    }

    suspend fun removeWord(word: WordEntity) = wordDao.delete(word)

    suspend fun clearLearnedWords() = wordDao.clearLearnedWords()

    /** Export the whole dictionary as plain lines "word\tlanguage\tfrequency\tisUserAdded". */
    suspend fun exportDictionary(): String =
        wordDao.getAllForExport().joinToString("\n") { "${it.word}\t${it.language}\t${it.frequency}\t${it.isUserAdded}" }

    /** Import from the format produced by [exportDictionary]. Returns count imported. */
    suspend fun importDictionary(content: String): Int {
        var count = 0
        content.lineSequence().forEach { line ->
            val parts = line.split("\t")
            if (parts.size >= 2) {
                val word = parts[0]
                val lang = parts[1]
                val freq = parts.getOrNull(2)?.toIntOrNull() ?: 1
                val userAdded = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: true
                wordDao.insert(WordEntity(word = word, language = lang, frequency = freq, isUserAdded = userAdded))
                count++
            }
        }
        return count
    }
}

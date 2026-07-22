package com.banglakb.keyboard

import com.banglakb.keyboard.db.WordDao
import com.banglakb.keyboard.db.WordEntity
import com.banglakb.keyboard.repository.DictionaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Minimal in-memory fake so repository logic can be tested without Room/Android. */
private class FakeWordDao : WordDao {
    val store = mutableListOf<WordEntity>()
    private var nextId = 1L

    override suspend fun getSuggestions(prefix: String, language: String, limit: Int): List<WordEntity> =
        store.filter { it.language == language && it.word.startsWith(prefix) }
            .sortedByDescending { it.frequency }
            .take(limit)

    override fun getPersonalDictionary(): Flow<List<WordEntity>> =
        flowOf(store.filter { it.isUserAdded })

    override suspend fun findWord(word: String, language: String): WordEntity? =
        store.find { it.word == word && it.language == language }

    override suspend fun insert(word: WordEntity): Long {
        val withId = word.copy(id = nextId++)
        store.add(withId)
        return withId.id
    }

    override suspend fun update(word: WordEntity) {
        val idx = store.indexOfFirst { it.id == word.id }
        if (idx >= 0) store[idx] = word
    }

    override suspend fun delete(word: WordEntity) {
        store.removeAll { it.id == word.id }
    }

    override suspend fun clearLearnedWords() {
        store.removeAll { !it.isUserAdded }
    }

    override suspend fun getAllForExport(): List<WordEntity> = store
}

class DictionaryRepositoryTest {

    @Test
    fun `learning a new word inserts it once`() = runBlocking {
        val dao = FakeWordDao()
        val repo = DictionaryRepository(dao)

        repo.learnWord("ami", "bn", incognito = false)

        assertEquals(1, dao.store.size)
        assertEquals("ami", dao.store[0].word)
    }

    @Test
    fun `learning the same word twice increments frequency instead of duplicating`() = runBlocking {
        val dao = FakeWordDao()
        val repo = DictionaryRepository(dao)

        repo.learnWord("ami", "bn", incognito = false)
        repo.learnWord("ami", "bn", incognito = false)

        assertEquals(1, dao.store.size)
        assertEquals(2, dao.store[0].frequency)
    }

    @Test
    fun `incognito mode skips learning entirely`() = runBlocking {
        val dao = FakeWordDao()
        val repo = DictionaryRepository(dao)

        repo.learnWord("secret", "en", incognito = true)

        assertEquals(0, dao.store.size)
        assertNull(dao.findWord("secret", "en"))
    }

    @Test
    fun `suggestions match by prefix and language`() = runBlocking {
        val dao = FakeWordDao()
        val repo = DictionaryRepository(dao)
        repo.learnWord("bhalo", "bn", incognito = false)
        repo.learnWord("bhalobasha", "bn", incognito = false)
        repo.learnWord("basha", "en", incognito = false)

        val results = repo.suggestionsFor("bha", "bn")

        assertEquals(2, results.size)
    }
}

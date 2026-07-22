package com.banglakb.keyboard.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE language = :language AND word LIKE :prefix || '%' ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    suspend fun getSuggestions(prefix: String, language: String, limit: Int = 10): List<WordEntity>

    @Query("SELECT * FROM words WHERE isUserAdded = 1 ORDER BY word ASC")
    fun getPersonalDictionary(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word = :word AND language = :language LIMIT 1")
    suspend fun findWord(word: String, language: String): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("DELETE FROM words WHERE isUserAdded = 0")
    suspend fun clearLearnedWords()

    @Query("SELECT * FROM words")
    suspend fun getAllForExport(): List<WordEntity>
}

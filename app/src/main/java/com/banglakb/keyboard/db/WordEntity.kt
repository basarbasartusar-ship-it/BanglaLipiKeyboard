package com.banglakb.keyboard.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val language: String, // "bn" or "en"
    val frequency: Int = 1,
    val isUserAdded: Boolean = false, // true if added via Personal Dictionary, false if auto-learned
    val lastUsed: Long = System.currentTimeMillis()
)

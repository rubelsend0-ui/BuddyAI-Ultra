package com.buddy.ultra.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_memory")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memoryKey: String,
    val memoryValue: String,
    val category: String = "PREFERENCE",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_history")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "USER" or "BUDDY"
    val text: String,
    val actionType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

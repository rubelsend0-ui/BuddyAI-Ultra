package com.buddy.ultra.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BuddyDao {
    @Query("SELECT * FROM conversation_history ORDER BY id DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity)

    @Query("DELETE FROM conversation_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM local_memory ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: MemoryEntity)

    @Query("DELETE FROM local_memory WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("SELECT * FROM personal_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: NoteEntity)
}

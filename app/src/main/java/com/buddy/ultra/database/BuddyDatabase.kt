package com.buddy.ultra.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MemoryEntity::class, MessageEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class BuddyDatabase : RoomDatabase() {
    abstract fun buddyDao(): BuddyDao
}

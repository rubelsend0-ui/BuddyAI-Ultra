package com.buddy.ultra.di

import android.content.Context
import androidx.room.Room
import com.buddy.ultra.database.BuddyDao
import com.buddy.ultra.database.BuddyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BuddyDatabase {
        return Room.databaseBuilder(context, BuddyDatabase::class.java, "buddy_ultra.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBuddyDao(db: BuddyDatabase): BuddyDao = db.buddyDao()
}

package com.example.myaccelerometer

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RecordEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordEntryDao(): RecordEntryDao
}

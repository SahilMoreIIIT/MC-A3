package com.example.myaccelerometer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

@Dao
interface RecordEntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: RecordEntry)

    @Query("DELETE FROM recorded_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT * FROM recorded_entries")
    suspend fun getAllEntries(): List<RecordEntry>

    @Query("SELECT * FROM recorded_entries")
    fun getAll(): Flow<List<RecordEntry>>

    suspend fun exportAllEntriesToCSV(outputStream: OutputStream) {
        val entries = getAllEntries()
        val csvHeader = "Time,X-Angle,Y-Angle,Z-Angle\n"
        outputStream.write(csvHeader.toByteArray())
        entries.forEach { entry ->
            val csvEntry = "${entry.time},${entry.xAngle},${entry.yAngle},${entry.zAngle}\n"
            outputStream.write(csvEntry.toByteArray())
        }
    }
}

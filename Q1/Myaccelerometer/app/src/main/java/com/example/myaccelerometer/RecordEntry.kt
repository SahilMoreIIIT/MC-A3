package com.example.myaccelerometer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recorded_entries")
data class RecordEntry(
    @PrimaryKey
    val time: Float, // Making time as primary key
    val xAngle: Float,
    val yAngle: Float,
    val zAngle: Float
)

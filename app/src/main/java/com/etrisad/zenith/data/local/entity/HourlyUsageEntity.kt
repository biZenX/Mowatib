package com.etrisad.zenith.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "hourly_usage",
    indices = [
        Index(value = ["date", "hour", "packageName"], unique = true),
        Index(value = ["packageName", "date", "hour"])
    ]
)
data class HourlyUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val hour: Int,
    val packageName: String,
    val usageTimeMillis: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

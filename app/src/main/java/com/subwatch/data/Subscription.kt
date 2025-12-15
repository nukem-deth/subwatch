package com.subwatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val domain: String?, // optional, for favicon
    val startDateEpochDay: Long,
    val endDateEpochDay: Long
)

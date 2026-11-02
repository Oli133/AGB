package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val preferredMode: String,
    val preferredDns: String,
    val targetFps: Int,
    val isFavorite: Boolean,
    val lastBoostTimestamp: Long = 0L
)

@Entity(tableName = "boost_sessions")
data class BoostSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val modeName: String,
    val gameTitle: String?,
    val freedRamMb: Long,
    val latencyBeforeMs: Int,
    val latencyAfterMs: Int
)

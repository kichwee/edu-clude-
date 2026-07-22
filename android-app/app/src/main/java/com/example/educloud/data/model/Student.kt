package com.example.educloud.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Student entity — mirrors PRD SQL schema, adapted for SQLite/Room.
 * Uses auto-generated Int PK instead of UUID to save storage on 2GB devices.
 * Sync API maps this to server-side UUID.
 */
@Entity(
    tableName = "students",
    indices = [Index(value = ["deviceId"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deviceId: String,
    val alias: String,
    val grade: Int,                        // 1–6 (CBC)
    val languagePref: String = "sw",       // "sw" | "en"
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val consentGiven: Boolean = false,
    val track: String = "A"
)

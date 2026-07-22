package com.example.educloud.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Records the version of the bundled search index without touching learner progress. */
@Entity(tableName = "content_pack_state")
data class ContentPackStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val version: String,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

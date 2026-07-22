package com.example.educloud.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContentPackStateDao {
    @Query("SELECT version FROM content_pack_state WHERE id = :id")
    suspend fun versionFor(id: Int = ContentPackStateEntity.SINGLETON_ID): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: ContentPackStateEntity)
}

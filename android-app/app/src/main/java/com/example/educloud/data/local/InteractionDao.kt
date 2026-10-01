package com.example.educloud.data.local

import androidx.room.*
import com.example.educloud.data.model.Interaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: Interaction): Long

    @Query("SELECT * FROM interactions WHERE studentId = :studentId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentInteractions(studentId: Int, limit: Int = 50): Flow<List<Interaction>>

    @Query("""
        SELECT * FROM interactions 
        WHERE studentId = :studentId AND subject = :subject
        ORDER BY createdAt DESC LIMIT :limit
    """)
    suspend fun getInteractionsBySubject(studentId: Int, subject: String, limit: Int = 20): List<Interaction>

    @Query("SELECT COUNT(*) FROM interactions WHERE studentId = :studentId AND subject = :subject AND strand = :strand")
    suspend fun countInteractions(studentId: Int, subject: String, strand: String): Int

    @Query("""
        SELECT AVG(difficulty) FROM interactions 
        WHERE studentId = :studentId AND subject = :subject AND strand = :strand 
        AND createdAt > :since
    """)
    suspend fun getAverageDifficulty(studentId: Int, subject: String, strand: String, since: Long): Float?

    @Query("SELECT COUNT(*) FROM interactions WHERE studentId = :studentId AND createdAt > :since")
    suspend fun countInteractionsSince(studentId: Int, since: Long): Int

    /** Reactive weekly aggregates for the honest Progress screen (plan §8). */
    @Query("SELECT COUNT(*) FROM interactions WHERE studentId = :studentId AND createdAt >= :since")
    fun observeInteractionsSince(studentId: Int, since: Long): Flow<Int>

    /** Quiz rows record isCorrect; tutor chats do not. Drives the featured daily goal. */
    @Query(
        """
        SELECT COUNT(*) FROM interactions
        WHERE studentId = :studentId AND createdAt >= :since AND isCorrect IS NOT NULL
        """
    )
    fun observeQuizAnswersSince(studentId: Int, since: Long): Flow<Int>

    @Query("SELECT SUM(timeTakenMs) FROM interactions WHERE studentId = :studentId AND createdAt >= :since")
    fun observeTimeLearnedSince(studentId: Int, since: Long): Flow<Long?>

    @Query(
        """
        SELECT COUNT(DISTINCT strand) FROM interactions
        WHERE studentId = :studentId AND createdAt >= :since AND strand IS NOT NULL
        """
    )
    fun observeTopicsSince(studentId: Int, since: Long): Flow<Int>

    /** Pending sync: interactions not yet synced to cloud */
    @Query("SELECT * FROM interactions WHERE studentId = :studentId ORDER BY createdAt ASC LIMIT 100")
    suspend fun getPendingSyncInteractions(studentId: Int): List<Interaction>

    @Query(
        """
        SELECT * FROM interactions
        WHERE studentId = :studentId AND subject = 'math'
          AND strand = 'two_digit_subtraction_regrouping'
          AND isCorrect = 0 AND remediationSyncedAt IS NULL
        ORDER BY createdAt ASC LIMIT 10
        """
    )
    suspend fun getUnsyncedRegroupingAttempts(studentId: Int): List<Interaction>

    @Query("UPDATE interactions SET remediationSyncedAt = :syncedAt WHERE id IN (:interactionIds)")
    suspend fun markRemediationSynced(interactionIds: List<Int>, syncedAt: Long)
}

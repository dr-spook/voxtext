package com.voctext.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptionDao {
    @Query("SELECT * FROM transcriptions ORDER BY created_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<TranscriptionEntity>>

    @Query("SELECT * FROM transcriptions WHERE id = :id")
    suspend fun getById(id: String): TranscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcription: TranscriptionEntity)

    @Update
    suspend fun update(transcription: TranscriptionEntity)

    @Delete
    suspend fun delete(transcription: TranscriptionEntity)

    @Query("SELECT COUNT(*) FROM transcriptions")
    suspend fun count(): Int

    @Query("DELETE FROM transcriptions WHERE id IN (SELECT id FROM transcriptions ORDER BY created_at ASC LIMIT 1)")
    suspend fun deleteOldest()
}
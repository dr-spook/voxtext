package com.voctext.app.data.repository

import com.voctext.app.data.local.TranscriptionDao
import com.voctext.app.data.local.TranscriptionEntity
import com.voctext.app.domain.model.Transcription
import com.voctext.app.domain.model.TranscriptionSource
import com.voctext.app.domain.model.TranscriptionStatus
import com.voctext.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(private val dao: TranscriptionDao) {

    fun getHistory(): Flow<List<Transcription>> {
        return dao.getRecent(Constants.MAX_HISTORY_ENTRIES).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getById(id: String): Transcription? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun insert(transcription: Transcription) {
        dao.insert(transcription.toEntity())
        // Maintain max entries
        val count = dao.count()
        if (count > Constants.MAX_HISTORY_ENTRIES) {
            dao.deleteOldest()
        }
    }

    suspend fun update(transcription: Transcription) {
        dao.update(transcription.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: String) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    private fun TranscriptionEntity.toDomain(): Transcription = Transcription(
        id = id,
        text = text,
        sourceType = TranscriptionSource.valueOf(sourceType),
        sourceName = sourceName,
        durationSeconds = durationSeconds,
        status = TranscriptionStatus.valueOf(status),
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun Transcription.toEntity(): TranscriptionEntity = TranscriptionEntity(
        id = id,
        text = text,
        sourceType = sourceType.name,
        sourceName = sourceName,
        durationSeconds = durationSeconds,
        status = status.name,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
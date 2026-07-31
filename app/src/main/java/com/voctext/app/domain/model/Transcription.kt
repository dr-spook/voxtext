package com.voctext.app.domain.model

import java.util.UUID

enum class TranscriptionSource { FILE, LINK }

enum class TranscriptionStatus { PENDING, PROCESSING, DONE, ERROR }

data class Transcription(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val sourceType: TranscriptionSource,
    val sourceName: String,
    val durationSeconds: Int = 0,
    val status: TranscriptionStatus = TranscriptionStatus.PENDING,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
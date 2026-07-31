package com.voctext.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcriptions")
data class TranscriptionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "source_type")
    val sourceType: String, // "FILE" or "LINK"

    @ColumnInfo(name = "source_name")
    val sourceName: String,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,

    @ColumnInfo(name = "status")
    val status: String, // "PENDING", "PROCESSING", "DONE", "ERROR"

    @ColumnInfo(name = "error_message")
    val errorMessage: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
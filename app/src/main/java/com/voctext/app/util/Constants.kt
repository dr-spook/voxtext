package com.voctext.app.util

object Constants {
    const val MAX_HISTORY_ENTRIES = 100
    const val MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024
    const val DICTIONARY_SIZE_MB = 75
    const val TARGET_TRANSCRIPTION_ACCURACY = 0.80
    const val TARGET_TRANSCRIPTION_SECONDS_PER_MINUTE = 15

    const val WEBVIEW_TIMEOUT_SECONDS = 30L
    const val DICTIONARY_DOWNLOAD_TIMEOUT_MINUTES = 5L

    // Audio processing
    const val TARGET_SAMPLE_RATE = 16000
    const val TARGET_CHANNELS = 1
    const val TARGET_BIT_RATE = 256000

    val SUPPORTED_AUDIO_MIME_TYPES = setOf(
        "audio/mpeg", "audio/wav", "audio/mp4", "audio/ogg",
        "audio/x-wav", "audio/m4a", "audio/x-m4a",
    )
    val SUPPORTED_VIDEO_MIME_TYPES = setOf(
        "video/mp4", "video/quicktime", "video/x-msvideo",
    )
    val SUPPORTED_LINK_DOMAINS = setOf(
        "youtube.com", "www.youtube.com", "youtu.be",
        "tiktok.com", "www.tiktok.com", "vm.tiktok.com",
        "instagram.com", "www.instagram.com",
    )
}
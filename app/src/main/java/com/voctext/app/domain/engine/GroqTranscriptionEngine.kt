package com.voctext.app.domain.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class GroqTranscriptionEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
) {

    private val supportedExtensions = setOf(
        "flac", "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "opus", "wav", "webm"
    )

    suspend fun transcribe(audioFile: File, apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                throw IllegalArgumentException("Fichier audio introuvable ou vide.")
            }
            if (apiKey.isBlank()) {
                throw IllegalStateException("Clé API Groq manquante. Veuillez la configurer dans les paramètres.")
            }

            val ext = audioFile.extension.lowercase()
            val validExtension = if (supportedExtensions.contains(ext)) ext else "wav"

            // Ensure filename parameter sent to Groq has a supported extension
            val multipartFileName = if (supportedExtensions.contains(ext)) {
                audioFile.name
            } else {
                "${audioFile.nameWithoutExtension}.wav"
            }

            val mimeType = when (validExtension) {
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                "m4a", "aac" -> "audio/m4a"
                "mp4" -> "video/mp4"
                "ogg" -> "audio/ogg"
                "opus" -> "audio/opus"
                "flac" -> "audio/flac"
                "webm" -> "audio/webm"
                else -> "audio/wav"
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    multipartFileName,
                    audioFile.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .addFormDataPart("model", "whisper-large-v3")
                .addFormDataPart("language", "fr")
                .addFormDataPart("response_format", "json")
                .build()

            val request = Request.Builder()
                .url(GROQ_TRANSCRIPTION_URL)
                .header("Authorization", "Bearer ${apiKey.trim()}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMessage = when (response.code) {
                        401 -> "Clé API Groq invalide ou expirée. Veuillez la vérifier dans les paramètres."
                        413 -> "Le fichier audio dépasse la limite maximale autorisée (25 Mo)."
                        429 -> "Limite de requêtes atteinte sur l'API Groq. Veuillez réessayer dans un instant."
                        else -> {
                            val errorJsonMsg = try {
                                JSONObject(responseBody).optJSONObject("error")?.optString("message")
                            } catch (_: Exception) { null }
                            errorJsonMsg ?: "Erreur de transcription Groq (Code HTTP ${response.code})."
                        }
                    }
                    throw IOException(errorMessage)
                }

                val json = JSONObject(responseBody)
                val text = json.optString("text", "").trim()
                if (text.isEmpty()) {
                    throw IllegalStateException("Aucune parole n'a été détectée dans ce contenu.")
                }
                text
            }
        }
    }

    companion object {
        private const val GROQ_TRANSCRIPTION_URL = "https://api.groq.com/openai/v1/audio/transcriptions"
    }
}

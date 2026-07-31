package com.voctext.app.domain.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class CobaltMediaExtractor(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {

    private val cobaltInstances = listOf(
        "https://co.wuk.sh/",
        "https://cobalt.stream/",
        "https://cobalt-api.kwippy.com/",
        "https://api.cobalt.tools/"
    )

    suspend fun extractAndDownloadAudio(webUrl: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            var directAudioUrl: String? = null
            var lastError: String? = null

            for (endpoint in cobaltInstances) {
                try {
                    val jsonPayload = JSONObject().apply {
                        put("url", webUrl.trim())
                        put("downloadMode", "audio")
                        put("audioFormat", "mp3")
                    }.toString()

                    val request = Request.Builder()
                        .url(endpoint)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .post(jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val json = JSONObject(responseBody)
                            val status = json.optString("status")

                            if (status != "error") {
                                directAudioUrl = json.optString("url").takeIf { it.isNotBlank() }
                                    ?: json.optJSONArray("picker")?.optJSONObject(0)?.optString("url")

                                if (!directAudioUrl.isNullOrBlank()) {
                                    return@use
                                }
                            } else {
                                lastError = json.optJSONObject("text")?.optString("error")
                            }
                        } else {
                            lastError = "Instance $endpoint a répondu HTTP ${response.code}"
                        }
                    }

                    if (!directAudioUrl.isNullOrBlank()) {
                        break
                    }
                } catch (e: Exception) {
                    lastError = e.message
                }
            }

            if (directAudioUrl.isNullOrBlank()) {
                throw IllegalStateException(lastError ?: "Impossible d'extraire le contenu de ce lien (serveur d'extraction indisponible).")
            }

            // Download direct audio file to cacheDir
            val audioFile = File(context.cacheDir, "web_audio_${System.currentTimeMillis()}.mp3")
            downloadAudioStream(directAudioUrl!!, audioFile)

            audioFile
        }
    }

    private fun downloadAudioStream(streamUrl: String, destinationFile: File) {
        val downloadRequest = Request.Builder()
            .url(streamUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .get()
            .build()

        client.newCall(downloadRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Échec du téléchargement du fichier audio (Code HTTP ${response.code}).")
            }
            val body = response.body ?: throw IOException("Le flux audio est vide.")

            body.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

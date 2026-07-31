package com.voctext.app.domain.engine
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VoskTranscriptionEngine(private val context: Context) : TranscriptionEngine {

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    override suspend fun isReady(): Boolean {
        return try {
            val modelDir = File(context.filesDir, "vosk-model-small-fr-0.22")
            modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun downloadDictionary(onProgress: (Int) -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val modelDir = File(context.filesDir, "vosk-model-small-fr-0.22")
                if (modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true) {
                    onProgress(100)
                    return@runCatching
                }

                modelDir.mkdirs()
                copyAssetFolder("vosk-model-small-fr-0.22", modelDir)
                onProgress(100)
            }
        }

    private fun copyAssetFolder(assetPath: String, targetDir: File) {
        val files = context.assets.list(assetPath) ?: return
        if (files.isEmpty()) {
            // It's a single file
            context.assets.open(assetPath).use { input ->
                targetDir.parentFile?.mkdirs()
                java.io.FileOutputStream(targetDir).use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            // It's a directory
            if (!targetDir.exists()) targetDir.mkdirs()
            for (file in files) {
                copyAssetFolder("$assetPath/$file", File(targetDir, file))
            }
        }
    }

    override suspend fun transcribe(pcmFilePath: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Initialize model once
                if (model == null) {
                    val modelDir = File(context.filesDir, "vosk-model-small-fr-0.22")
                    if (!modelDir.exists()) {
                        throw IllegalStateException("Dictionnaire non installé")
                    }
                    model = Model(modelDir.absolutePath)
                }

                // Create recognizer (16kHz mono)
                recognizer = Recognizer(model, 16000F)

                val file = File(pcmFilePath)
                if (!file.exists()) throw IllegalStateException("Fichier audio introuvable")

                // Read PCM file in chunks
                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        if (bytesRead > 0) {
                            // Convert to little-endian short array
                            val shortBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer()
                            val shorts = ShortArray(shortBuffer.remaining())
                            shortBuffer.get(shorts)
                            recognizer!!.acceptWaveForm(shorts, shorts.size)
                        }
                    }
                }

                // Get final result
                val resultJson = recognizer!!.finalResult
                val text = JSONObject(resultJson).optString("text", "")

                recognizer!!.close()
                recognizer = null

                text
            }.also {
                recognizer?.close()
                recognizer = null
            }
        }


}
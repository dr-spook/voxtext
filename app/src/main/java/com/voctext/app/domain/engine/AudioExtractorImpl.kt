package com.voctext.app.domain.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioExtractorImpl(private val context: Context) : AudioExtractor {

    private val supportedExtensions = setOf(
        "flac", "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "opus", "wav", "webm"
    )

    override suspend fun extractAudio(inputUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val extension = getExtensionFromUri(inputUri)

            // Strategy 1: Direct copy if format is already supported by Groq API (MP3, OPUS, MP4, M4A, OGG, WAV, etc.)
            if (extension != null && supportedExtensions.contains(extension.lowercase())) {
                val copiedFile = File(context.cacheDir, "input_${System.currentTimeMillis()}.${extension.lowercase()}")
                context.contentResolver.openInputStream(inputUri).use { input ->
                    if (input != null) {
                        FileOutputStream(copiedFile).use { output ->
                            input.copyTo(output)
                        }
                        if (copiedFile.exists() && copiedFile.length() > 0L) {
                            return@runCatching copiedFile.absolutePath
                        }
                    }
                }
            }

            // Strategy 2: MediaCodec decoding to 16kHz mono PCM + 44-byte RIFF WAV Header (.wav extension)
            val wavFile = File(context.cacheDir, "extracted_${System.currentTimeMillis()}.wav")
            val extractor = MediaExtractor()

            val pfd = context.contentResolver.openFileDescriptor(inputUri, "r")
                ?: throw IllegalStateException("Impossible d'ouvrir le fichier")

            try {
                extractor.setDataSource(pfd.fileDescriptor)
                var audioTrackIndex = -1
                var format: MediaFormat? = null

                for (i in 0 until extractor.trackCount) {
                    val trackFormat = extractor.getTrackFormat(i)
                    val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i
                        format = trackFormat
                        break
                    }
                }

                if (audioTrackIndex == -1 || format == null) {
                    throw IllegalStateException("Aucune piste audio trouvée dans ce fichier")
                }

                extractor.selectTrack(audioTrackIndex)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalStateException("MIME audio inconnu")
                val sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
                val channelCount = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1

                val decoder = MediaCodec.createDecoderByType(mime)
                decoder.configure(format, null, null, 0)
                decoder.start()

                val info = MediaCodec.BufferInfo()
                var isEOS = false
                val timeoutUs = 5000L
                var totalPcmBytes = 0L

                FileOutputStream(wavFile).use { output ->
                    // Write placeholder 44-byte WAV header
                    output.write(ByteArray(44))

                    while (!isEOS) {
                        val inIndex = decoder.dequeueInputBuffer(timeoutUs)
                        if (inIndex >= 0) {
                            val buffer = decoder.getInputBuffer(inIndex)
                            if (buffer != null) {
                                val sampleSize = extractor.readSampleData(buffer, 0)
                                if (sampleSize < 0) {
                                    decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                } else {
                                    decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                                    extractor.advance()
                                }
                            }
                        }

                        var outIndex = decoder.dequeueOutputBuffer(info, timeoutUs)
                        while (outIndex >= 0) {
                            val outBuffer = decoder.getOutputBuffer(outIndex)
                            if (outBuffer != null && info.size > 0) {
                                val pcmData = ByteArray(info.size)
                                outBuffer.position(info.offset)
                                outBuffer.get(pcmData)

                                val resampled = resampleTo16kMono(pcmData, sampleRate, channelCount)
                                output.write(resampled)
                                totalPcmBytes += resampled.size
                            }
                            decoder.releaseOutputBuffer(outIndex, false)
                            if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isEOS = true
                                break
                            }
                            outIndex = decoder.dequeueOutputBuffer(info, 0)
                        }
                    }
                }

                decoder.stop()
                decoder.release()
                extractor.release()
                pfd.close()

                // Fill actual WAV header in the file
                RandomAccessFile(wavFile, "rw").use { raf ->
                    writeWavHeader(raf, totalPcmBytes, 16000, 1, 16)
                }

                wavFile.absolutePath
            } catch (e: Exception) {
                extractor.release()
                pfd.close()
                wavFile.delete()
                throw e
            }
        }
    }

    private fun getExtensionFromUri(uri: Uri): String? {
        var name: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) { }

        if (name == null) {
            name = uri.lastPathSegment
        }

        val dotIndex = name?.lastIndexOf('.') ?: -1
        return if (dotIndex >= 0 && dotIndex < name!!.length - 1) {
            name!!.substring(dotIndex + 1)
        } else {
            context.contentResolver.getType(uri)?.substringAfterLast('/')
        }
    }

    private fun writeWavHeader(raf: RandomAccessFile, totalPcmBytes: Long, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val totalDataLen = totalPcmBytes + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalPcmBytes and 0xff).toByte()
        header[41] = (totalPcmBytes shr 8 and 0xff).toByte()
        header[42] = (totalPcmBytes shr 16 and 0xff).toByte()
        header[43] = (totalPcmBytes shr 24 and 0xff).toByte()

        raf.seek(0)
        raf.write(header, 0, 44)
    }

    private fun resampleTo16kMono(pcmData: ByteArray, sourceRate: Int, channels: Int): ByteArray {
        if (pcmData.isEmpty()) return ByteArray(0)
        val shorts = ShortArray(pcmData.size / 2)
        ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)

        val monoShorts = if (channels > 1) {
            ShortArray(shorts.size / channels) { i ->
                var sum = 0
                for (c in 0 until channels) {
                    val idx = i * channels + c
                    if (idx < shorts.size) sum += shorts[idx]
                }
                (sum / channels).toShort()
            }
        } else {
            shorts
        }

        val resampledMono = if (sourceRate != 16000 && sourceRate > 0) {
            val ratio = sourceRate.toDouble() / 16000.0
            val newSize = (monoShorts.size / ratio).toInt()
            ShortArray(newSize) { i ->
                val srcIndex = (i * ratio).toInt()
                if (srcIndex < monoShorts.size) monoShorts[srcIndex] else 0
            }
        } else {
            monoShorts
        }

        val outBytes = ByteArray(resampledMono.size * 2)
        ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(resampledMono)
        return outBytes
    }
}
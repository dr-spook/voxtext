package com.voctext.app.domain.engine

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.voctext.app.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class WebViewInterceptorImpl(private val context: Context) : WebViewInterceptor {

    private val mediaExtensions = listOf(
        ".mp4", ".m4a", ".mp3", ".webm", ".ogg",
        "videoplayback", "audio", "googlevideo.com/videoplayback",
    )

    override suspend fun extractAudioFromUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            var capturedMediaUrl: String? = null
            val semaphore = Semaphore(0)
            var webView: WebView? = null

            withContext(Dispatchers.Main) {
                webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.domStorageEnabled = true
                    settings.blockNetworkImage = true
                    settings.loadsImagesAutomatically = false

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest,
                        ): WebResourceResponse? {
                            val requestUrl = request.url.toString().lowercase()
                            if (mediaExtensions.any { requestUrl.contains(it) }) {
                                capturedMediaUrl = request.url.toString()
                                semaphore.release()
                            }
                            return null
                        }
                    }
                }
            }

            webView?.loadUrl(url)

            val success = withTimeoutOrNull(Constants.WEBVIEW_TIMEOUT_SECONDS * 1000L) {
                semaphore.acquire()
                capturedMediaUrl != null
            } ?: false

            if (!success || capturedMediaUrl == null) {
                webView?.destroy()
                throw IllegalStateException("Could not extract media URL from $url")
            }

            val audioFile = File(context.cacheDir, "web_audio_${System.currentTimeMillis()}.mp4")
            downloadFile(capturedMediaUrl!!, audioFile)

            webView?.destroy()
            audioFile.absolutePath
        }
    }

    private fun downloadFile(urlString: String, outputFile: File) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000

        try {
            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
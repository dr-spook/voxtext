package com.voctext.app.domain.engine

interface WebViewInterceptor {
    /**
     * Extrait la bande-son d'un lien web (YouTube, TikTok, Instagram)
     * via un WebView invisible.
     * @param url URL de la page
     * @return chemin absolu du fichier audio téléchargé
     */
    suspend fun extractAudioFromUrl(url: String): Result<String>
}
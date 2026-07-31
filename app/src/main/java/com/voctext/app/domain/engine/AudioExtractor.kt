package com.voctext.app.domain.engine

import android.net.Uri

interface AudioExtractor {
    /**
     * Extrait la piste audio d'un fichier audio ou vidéo et la convertit
     * en PCM 16kHz mono 16-bit pour le moteur de transcription.
     * @param inputUri URI du fichier source
     * @return chemin absolu du fichier PCM extrait
     */
    suspend fun extractAudio(inputUri: Uri): Result<String>
}
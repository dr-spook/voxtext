package com.voctext.app.domain.engine

interface TranscriptionEngine {
    /**
     * Vérifie si le dictionnaire français est installé et prêt.
     */
    suspend fun isReady(): Boolean

    /**
     * Télécharge le dictionnaire français.
     * @param onProgress callback (0..100)
     */
    suspend fun downloadDictionary(onProgress: (Int) -> Unit): Result<Unit>

    /**
     * Transcrit un fichier audio PCM 16kHz mono 16-bit en texte.
     * @param pcmFilePath chemin absolu vers le fichier PCM
     * @return texte transcrit
     */
    suspend fun transcribe(pcmFilePath: String): Result<String>
}
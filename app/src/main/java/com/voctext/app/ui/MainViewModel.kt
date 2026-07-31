package com.voctext.app.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voctext.app.data.local.AppDatabase
import com.voctext.app.data.repository.HistoryRepository
import com.voctext.app.data.repository.SettingsRepository
import com.voctext.app.domain.engine.AudioExtractorImpl
import com.voctext.app.domain.engine.CobaltMediaExtractor
import com.voctext.app.domain.engine.GroqTranscriptionEngine
import com.voctext.app.domain.model.*
import com.voctext.app.ui.screens.OnboardingStep
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class ToastType { SUCCESS, ERROR }

data class MainUiState(
    val onboardingComplete: Boolean = false,
    val onboardingStep: OnboardingStep = OnboardingStep.WELCOME,
    val dictionaryProgress: Int = 100,
    val isDictionaryDownloading: Boolean = false,
    val dictionaryReady: Boolean = true,
    val history: List<Transcription> = emptyList(),
    val linkInput: String = "",
    val linkInputError: String? = null,
    val showResultSheet: Boolean = false,
    val currentTranscription: Transcription? = null,
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.PENDING,
    val transcriptionText: String = "",
    val transcriptionError: String? = null,
    val toastMessage: String = "",
    val toastType: ToastType = ToastType.SUCCESS,
    val toastVisible: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val groqApiKey: String = "",
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val historyRepo = HistoryRepository(db.transcriptionDao())
    private val settingsRepo = SettingsRepository(application)
    private val groqEngine = GroqTranscriptionEngine()
    private val audioExtractor = AudioExtractorImpl(application)
    private val cobaltExtractor = CobaltMediaExtractor(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val storedKey = settingsRepo.getApiKey() ?: ""
        _uiState.update { it.copy(groqApiKey = storedKey, onboardingComplete = true, dictionaryReady = true) }

        viewModelScope.launch {
            historyRepo.getHistory().collect { list ->
                _uiState.update { it.copy(history = list) }
            }
        }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettingsDialog = true, groqApiKey = settingsRepo.getApiKey() ?: "") }
    }

    fun dismissSettings() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun saveGroqApiKey(apiKey: String) {
        settingsRepo.saveApiKey(apiKey)
        _uiState.update { it.copy(groqApiKey = apiKey.trim(), showSettingsDialog = false) }
        showToast("Clé API enregistrée !", ToastType.SUCCESS)
    }

    fun setOnboardingStep(step: OnboardingStep) {
        _uiState.update { it.copy(onboardingStep = step) }
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(onboardingComplete = true) }
    }

    fun updateLinkInput(value: String) {
        _uiState.update { it.copy(linkInput = value, linkInputError = null) }
    }

    fun clearLinkInput() {
        _uiState.update { it.copy(linkInput = "", linkInputError = null) }
    }

    fun submitLink() {
        val link = _uiState.value.linkInput.trim()
        if (link.isBlank()) return
        if (!isValidLink(link)) {
            _uiState.update { it.copy(linkInputError = "Ce type de lien n'est pas supporté.") }
            return
        }

        if (!ensureApiKeyPresent()) return

        val t = Transcription(sourceType = TranscriptionSource.LINK, sourceName = link, status = TranscriptionStatus.PROCESSING)
        _uiState.update { it.copy(currentTranscription = t, showResultSheet = true, transcriptionStatus = TranscriptionStatus.PROCESSING, transcriptionText = "", transcriptionError = null, linkInput = "", linkInputError = null) }
        transcribeFromLink(link, t.id)
    }

    fun importFile(uri: Uri) {
        if (!ensureApiKeyPresent()) return

        val name = getFileName(uri) ?: "Fichier inconnu"
        val t = Transcription(sourceType = TranscriptionSource.FILE, sourceName = name, status = TranscriptionStatus.PROCESSING)
        _uiState.update { it.copy(currentTranscription = t, showResultSheet = true, transcriptionStatus = TranscriptionStatus.PROCESSING, transcriptionText = "", transcriptionError = null) }
        transcribeFromFile(uri, t.id)
    }

    private fun ensureApiKeyPresent(): Boolean {
        if (!settingsRepo.hasApiKey()) {
            openSettings()
            showToast("Veuillez configurer votre clé API Groq gratuite pour commencer.", ToastType.ERROR)
            return false
        }
        return true
    }

    fun openHistoryTranscription(id: String) {
        viewModelScope.launch {
            historyRepo.getById(id)?.let {
                _uiState.update { s -> s.copy(currentTranscription = it, showResultSheet = true, transcriptionStatus = it.status, transcriptionText = it.text, transcriptionError = it.errorMessage) }
            }
        }
    }

    fun handleIncomingFile(uri: Uri?) = uri?.let { importFile(it) }
    fun handleIncomingLink(text: String?) {
        if (text != null && isValidLink(text)) {
            _uiState.update { it.copy(linkInput = text) }
            submitLink()
        }
    }

    fun dismissResult() = _uiState.update { it.copy(showResultSheet = false) }

    fun copyText(view: android.view.View) {
        val text = _uiState.value.transcriptionText
        if (text.isBlank()) return
        val cm = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Transcription", text))
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        showToast("Texte copié !", ToastType.SUCCESS)
    }

    fun shareText() = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, _uiState.value.transcriptionText)
    }

    private fun showToast(msg: String, type: ToastType) {
        _uiState.update { it.copy(toastVisible = true, toastMessage = msg, toastType = type) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            _uiState.update { it.copy(toastVisible = false) }
        }
    }

    private fun transcribeFromFile(uri: Uri, id: String) {
        val apiKey = settingsRepo.getApiKey() ?: ""
        viewModelScope.launch {
            var tempAudioPath: String? = null
            try {
                val extractResult = audioExtractor.extractAudio(uri)
                if (extractResult.isFailure) {
                    updateError(id, extractResult.exceptionOrNull()?.message ?: "Impossible de lire ce fichier.")
                    return@launch
                }
                tempAudioPath = extractResult.getOrThrow()
                val audioFile = File(tempAudioPath)

                val result = groqEngine.transcribe(audioFile, apiKey)
                if (result.isFailure) {
                    val ex = result.exceptionOrNull()
                    if (ex?.message?.contains("invalide", ignoreCase = true) == true) {
                        openSettings()
                    }
                    updateError(id, ex?.message ?: "Erreur de transcription Groq.")
                    return@launch
                }

                val text = result.getOrThrow()
                updateSuccess(id, text)
            } finally {
                // Automatic cache cleanup
                tempAudioPath?.let { path ->
                    try { File(path).delete() } catch (_: Exception) { }
                }
            }
        }
    }

    private fun transcribeFromLink(url: String, id: String) {
        val apiKey = settingsRepo.getApiKey() ?: ""
        viewModelScope.launch {
            var downloadedFile: File? = null
            try {
                val extractResult = cobaltExtractor.extractAndDownloadAudio(url)
                if (extractResult.isFailure) {
                    updateError(id, extractResult.exceptionOrNull()?.message ?: "Impossible d'extraire le contenu de ce lien.")
                    return@launch
                }
                downloadedFile = extractResult.getOrThrow()

                val result = groqEngine.transcribe(downloadedFile, apiKey)
                if (result.isFailure) {
                    val ex = result.exceptionOrNull()
                    if (ex?.message?.contains("invalide", ignoreCase = true) == true) {
                        openSettings()
                    }
                    updateError(id, ex?.message ?: "Erreur de transcription Groq.")
                    return@launch
                }

                val text = result.getOrThrow()
                updateSuccess(id, text)
            } finally {
                // Automatic cache cleanup
                downloadedFile?.let { file ->
                    try { file.delete() } catch (_: Exception) { }
                }
            }
        }
    }

    private fun updateSuccess(id: String, text: String) {
        val t = _uiState.value.currentTranscription?.copy(text = text, status = TranscriptionStatus.DONE, updatedAt = System.currentTimeMillis()) ?: return
        _uiState.update { it.copy(currentTranscription = t, transcriptionStatus = TranscriptionStatus.DONE, transcriptionText = text, transcriptionError = null) }
        viewModelScope.launch { historyRepo.insert(t) }
    }

    private fun updateError(id: String, msg: String) {
        val t = _uiState.value.currentTranscription?.copy(status = TranscriptionStatus.ERROR, errorMessage = msg, updatedAt = System.currentTimeMillis()) ?: return
        _uiState.update { it.copy(currentTranscription = t, transcriptionStatus = TranscriptionStatus.ERROR, transcriptionError = msg) }
        viewModelScope.launch { historyRepo.insert(t) }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst()) name = c.getString(i)
        }
        return name ?: uri.lastPathSegment
    }

    private fun isValidLink(text: String): Boolean {
        return try {
            val host = Uri.parse(text).host?.lowercase() ?: return false
            com.voctext.app.util.Constants.SUPPORTED_LINK_DOMAINS.any { host == it || host.endsWith(".$it") }
        } catch (_: Exception) { false }
    }
}
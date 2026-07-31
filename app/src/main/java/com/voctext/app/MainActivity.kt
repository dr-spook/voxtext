package com.voctext.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voctext.app.ui.MainViewModel
import com.voctext.app.ui.components.SettingsDialog
import com.voctext.app.ui.components.VoctextBottomSheet
import com.voctext.app.ui.components.VoctextInputState
import com.voctext.app.ui.components.VoctextToast
import com.voctext.app.ui.screens.HomeScreen
import com.voctext.app.ui.screens.OnboardingScreen
import com.voctext.app.ui.screens.OnboardingStep
import com.voctext.app.ui.screens.ResultContent
import com.voctext.app.ui.theme.VoctextTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        @Suppress("DEPRECATION")
        val incomingUri = intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val incomingText = intent?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            VoctextTheme {
                VoctextAppContent(
                    viewModel = viewModel,
                    onOpenFilePicker = {
                        filePickerLauncher.launch(arrayOf("audio/*", "video/*"))
                    },
                    incomingUri = incomingUri,
                    incomingText = incomingText,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        @Suppress("DEPRECATION")
        val newUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val newText = intent.getStringExtra(Intent.EXTRA_TEXT)
        viewModel.handleIncomingFile(newUri)
        viewModel.handleIncomingLink(newText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoctextAppContent(
    viewModel: MainViewModel,
    onOpenFilePicker: () -> Unit,
    incomingUri: Uri?,
    incomingText: String?,
) {
    val state by viewModel.uiState.collectAsState()
    val localView = androidx.compose.ui.platform.LocalView.current

    LaunchedEffect(incomingUri, incomingText) {
        viewModel.handleIncomingFile(incomingUri)
        viewModel.handleIncomingLink(incomingText)
    }

    if (!state.onboardingComplete) {
        OnboardingScreen(
            currentStep = state.onboardingStep,
            onNavigateToStep = { viewModel.setOnboardingStep(it) },
            onRequestPermission = { viewModel.setOnboardingStep(OnboardingStep.DICTIONARY) },
            dictionaryProgress = state.dictionaryProgress,
            isDictionaryDownloading = state.isDictionaryDownloading,
            dictionaryReady = state.dictionaryReady,
            onFinish = { viewModel.completeOnboarding() },
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                history = state.history,
                linkInputState = VoctextInputState(
                    value = state.linkInput,
                    isError = state.linkInputError != null,
                    errorMessage = state.linkInputError,
                ),
                onLinkInputChange = { viewModel.updateLinkInput(it) },
                onLinkInputClear = { viewModel.clearLinkInput() },
                onLinkSubmit = { viewModel.submitLink() },
                onImportFile = onOpenFilePicker,
                onTranscriptionClick = { viewModel.openHistoryTranscription(it) },
                onMenuSettings = { viewModel.openSettings() },
                onMenuAbout = { },
                onMenuRedownloadDict = { },
            )

            if (state.showSettingsDialog) {
                SettingsDialog(
                    initialApiKey = state.groqApiKey,
                    onDismiss = { viewModel.dismissSettings() },
                    onSave = { key -> viewModel.saveGroqApiKey(key) },
                )
            }

            if (state.showResultSheet && state.currentTranscription != null) {
                VoctextBottomSheet(onDismiss = { viewModel.dismissResult() }) {
                    ResultContent(
                        sourceName = state.currentTranscription!!.sourceName,
                        sourceType = state.currentTranscription!!.sourceType,
                        status = state.transcriptionStatus,
                        text = state.transcriptionText,
                        errorMessage = state.transcriptionError,
                        onCopy = { viewModel.copyText(localView) },
                        onShare = {
                            val shareIntent = viewModel.shareText()
                            localView.context.startActivity(Intent.createChooser(shareIntent, null))
                        },
                    )
                }
            }

            VoctextToast(
                message = state.toastMessage,
                type = state.toastType,
                visible = state.toastVisible,
                onDismiss = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
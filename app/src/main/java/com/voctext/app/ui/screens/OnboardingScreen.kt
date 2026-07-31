package com.voctext.app.ui.screens
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.voctext.app.R
import com.voctext.app.ui.components.ButtonSize
import com.voctext.app.ui.components.ButtonVariant
import com.voctext.app.ui.components.VoctextButton
import com.voctext.app.ui.theme.VoctextSpacing

enum class OnboardingStep(val index: Int) {
    WELCOME(0),
    PERMISSION(1),
    DICTIONARY(2),
}

@Composable
fun OnboardingScreen(
    currentStep: OnboardingStep,
    onNavigateToStep: (OnboardingStep) -> Unit,
    onRequestPermission: () -> Unit,
    dictionaryProgress: Int,
    isDictionaryDownloading: Boolean,
    dictionaryReady: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = VoctextSpacing.screenHorizontal)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val direction = if (targetState.index > initialState.index) 1 else -1
                slideInHorizontally { direction * it } + fadeIn() togetherWith
                        slideOutHorizontally { -direction * it } + fadeOut()
            },
            label = "onboarding_step",
        ) { step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (step) {
                    OnboardingStep.WELCOME -> {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                        Text(
                            text = stringResource(R.string.onboarding_welcome_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.md))
                        Text(
                            text = stringResource(R.string.onboarding_welcome_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                        VoctextButton(
                            label = stringResource(R.string.onboarding_welcome_cta),
                            onClick = { onNavigateToStep(OnboardingStep.PERMISSION) },
                            size = ButtonSize.LG,
                        )
                    }

                    OnboardingStep.PERMISSION -> {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                        Text(
                            text = stringResource(R.string.onboarding_permission_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.md))
                        Text(
                            text = stringResource(R.string.onboarding_permission_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                        Text(
                            text = stringResource(R.string.onboarding_permission_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                        VoctextButton(
                            label = stringResource(R.string.onboarding_permission_cta),
                            onClick = onRequestPermission,
                            size = ButtonSize.LG,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.md))
                        VoctextButton(
                            label = stringResource(R.string.onboarding_permission_skip),
                            onClick = { onNavigateToStep(OnboardingStep.DICTIONARY) },
                            variant = ButtonVariant.GHOST,
                            size = ButtonSize.MD,
                        )
                    }

                    OnboardingStep.DICTIONARY -> {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                        Text(
                            text = stringResource(R.string.onboarding_dict_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(VoctextSpacing.md))
                        Text(
                            text = stringResource(R.string.onboarding_dict_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        if (isDictionaryDownloading) {
                            Spacer(modifier = Modifier.height(VoctextSpacing.lg))
                            LinearProgressIndicator(
                                progress = { dictionaryProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                            Text(
                                text = stringResource(R.string.onboarding_dict_progress, dictionaryProgress),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                        Text(
                            text = stringResource(R.string.onboarding_dict_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )

                        if (dictionaryReady) {
                            Spacer(modifier = Modifier.height(VoctextSpacing.xl))
                            VoctextButton(
                                label = stringResource(R.string.onboarding_dict_cta),
                                onClick = onFinish,
                                size = ButtonSize.LG,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(VoctextSpacing.sm),
            modifier = Modifier.padding(bottom = VoctextSpacing.lg),
        ) {
            OnboardingStep.entries.forEach { step ->
                val isActive = step.index <= currentStep.index
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small,
                        ),
                )
            }
        }
    }
}
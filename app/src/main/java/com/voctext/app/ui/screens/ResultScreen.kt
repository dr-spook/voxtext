package com.voctext.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.voctext.app.R
import com.voctext.app.domain.model.TranscriptionSource
import com.voctext.app.domain.model.TranscriptionStatus
import com.voctext.app.ui.components.*
import com.voctext.app.ui.theme.VoctextSpacing

@Composable
fun ResultContent(
    sourceName: String,
    sourceType: TranscriptionSource,
    status: TranscriptionStatus,
    text: String,
    errorMessage: String?,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VoctextSpacing.screenHorizontal),
    ) {
        // Header
        Spacer(modifier = Modifier.height(VoctextSpacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (sourceType) {
                    TranscriptionSource.FILE -> Icons.Outlined.Description
                    TranscriptionSource.LINK -> Icons.Outlined.Link
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(VoctextSpacing.sm))
            Text(
                text = sourceName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(VoctextSpacing.md))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(VoctextSpacing.md))

        // Content area
        when (status) {
            TranscriptionStatus.PENDING, TranscriptionStatus.PROCESSING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VoctextSkeleton(lineCount = 8)
                        Spacer(modifier = Modifier.height(VoctextSpacing.md))
                        Text(
                            text = stringResource(R.string.result_loading_transcription),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            TranscriptionStatus.ERROR -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(VoctextSpacing.md))
                    Text(
                        text = errorMessage ?: "Une erreur est survenue.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                    Text(
                        text = stringResource(R.string.error_web_extraction_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            TranscriptionStatus.DONE -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (text.isBlank()) {
                        // No speech detected
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.MicOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(VoctextSpacing.md))
                                Text(
                                    text = stringResource(R.string.error_no_speech),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(VoctextSpacing.md))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = VoctextSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(VoctextSpacing.md),
                ) {
                    VoctextButton(
                        label = stringResource(R.string.result_copy),
                        onClick = onCopy,
                        size = ButtonSize.LG,
                        modifier = Modifier.weight(1f),
                    )
                    VoctextButton(
                        label = stringResource(R.string.result_share),
                        onClick = onShare,
                        variant = ButtonVariant.SECONDARY,
                        size = ButtonSize.LG,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
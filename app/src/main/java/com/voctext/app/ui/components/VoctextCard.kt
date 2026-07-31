package com.voctext.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voctext.app.domain.model.Transcription
import com.voctext.app.domain.model.TranscriptionSource
import com.voctext.app.ui.theme.VoctextRadius
import com.voctext.app.ui.theme.VoctextSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoctextHistoryCard(
    transcription: Transcription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VoctextRadius.md))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(VoctextRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 1.dp,
        ),
        border = if (isDark) androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline
        ) else null,
    ) {
        Column(
            modifier = Modifier.padding(VoctextSpacing.cardPadding),
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = when (transcription.sourceType) {
                        TranscriptionSource.FILE -> Icons.Outlined.Description
                        TranscriptionSource.LINK -> Icons.Outlined.Link
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(VoctextSpacing.sm))
                Text(
                    text = transcription.sourceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Metadata
            Row(
                horizontalArrangement = Arrangement.spacedBy(VoctextSpacing.sm),
            ) {
                Text(
                    text = formatDate(transcription.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDuration(transcription.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Preview text (first 2 lines)
            if (transcription.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(VoctextSpacing.sm))
                Text(
                    text = transcription.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}min ${seconds}s" else "${seconds}s"
}
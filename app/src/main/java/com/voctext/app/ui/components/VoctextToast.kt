package com.voctext.app.ui.components
import com.voctext.app.ui.ToastType
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.voctext.app.ui.theme.Success
import com.voctext.app.ui.theme.VoctextRadius
import com.voctext.app.ui.theme.VoctextSpacing
import kotlinx.coroutines.delay

enum class ToastType { SUCCESS, ERROR }

@Composable
fun VoctextToast(
    message: String,
    type: ToastType,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(2000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(VoctextRadius.full),
                )
                .padding(horizontal = VoctextSpacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val (icon, tint) = when (type) {
                ToastType.SUCCESS -> Icons.Outlined.CheckCircle to Success
                ToastType.ERROR -> Icons.Outlined.ErrorOutline to MaterialTheme.colorScheme.error
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tint,
            )
            Spacer(modifier = Modifier.width(VoctextSpacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}
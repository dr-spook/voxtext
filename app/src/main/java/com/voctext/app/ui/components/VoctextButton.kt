package com.voctext.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voctext.app.ui.theme.VoctextRadius
import com.voctext.app.ui.theme.VoctextSpacing

enum class ButtonVariant { PRIMARY, SECONDARY, GHOST, DESTRUCTIVE }
enum class ButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
    val radius: Dp,
) {
    SM(36.dp, 12.dp, 14.sp, 16.dp, VoctextRadius.sm),
    MD(44.dp, 20.dp, 16.sp, 20.dp, VoctextRadius.md),
    LG(52.dp, 24.dp, 16.sp, 24.dp, VoctextRadius.md),
}

@Composable
fun VoctextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ButtonSize = ButtonSize.MD,
    leadingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = when (variant) {
        ButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        )
        ButtonVariant.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
        ButtonVariant.GHOST -> ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        )
        ButtonVariant.DESTRUCTIVE -> ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.error,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
        )
    }

    val border = if (variant == ButtonVariant.SECONDARY) {
        ButtonDefaults.outlinedButtonBorder(enabled)
    } else {
        null
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(size.radius),
        colors = colors,
        border = border,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding, vertical = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                color = if (variant == ButtonVariant.PRIMARY) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize),
                )
                Spacer(modifier = Modifier.width(VoctextSpacing.xs))
            }
            Text(
                text = label,
                fontSize = size.fontSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = size.fontSize,
            )
        }
    }
}
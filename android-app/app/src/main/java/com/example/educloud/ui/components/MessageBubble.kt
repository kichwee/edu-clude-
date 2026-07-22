package com.example.educloud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chat message bubble — shows user questions and AI responses.
 * Animating text is handled by the parent screen via streaming state.
 */
@Composable
fun MessageBubble(
    text: String,
    isFromUser: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isFromUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isFromUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val alignment = if (isFromUser) Alignment.End else Alignment.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isFromUser) 20.dp else 4.dp,
                        bottomEnd = if (isFromUser) 4.dp else 20.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (isLoading) {
                LoadingDots(color = textColor)
            } else {
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingDots(color: androidx.compose.ui.graphics.Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color.copy(alpha = 0.6f), RoundedCornerShape(50))
            )
        }
    }
}

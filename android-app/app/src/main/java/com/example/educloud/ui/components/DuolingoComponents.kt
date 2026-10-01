package com.example.educloud.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Duolingo Palette Tokens
val DuoGreen = Color(0xFF58CC02)
val DuoGreenDark = Color(0xFF46A302)
val DuoBlue = Color(0xFF1CB0F6)
val DuoRed = Color(0xFFFFEBEE)
val DuoRedDark = Color(0xFFEA2B2B)
val DuoYellow = Color(0xFFFFC800)
val DuoCream = Color(0xFFFAF8F5)
val DuoCardBorder = Color(0xFFE5E5E5)

/**
 * Tactile 3D-style push button with bottom shadow/border.
 */
@Composable
fun DuolingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = DuoGreen,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(4.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = if (enabled) containerColor else Color(0xFFE0E0E0),
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Animated segmented progress bar with XP star indicators.
 */
@Composable
fun DuolingoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    streak: Int = 3,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = "XP", tint = DuoYellow, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = "$streak", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(50)),
            color = DuoGreen,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

/**
 * Speech bubble featuring Didi the EduCloud Tutor giving Grade 3 coaching tips.
 */
@Composable
fun MascotBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = DuoBlue,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🦉", fontSize = 24.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF0F8FF),
            border = BorderStroke(1.5.dp, DuoBlue.copy(alpha = 0.4f)),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0D47A1)
            )
        }
    }
}

/**
 * Slide-up Duolingo feedback bar for correct / retry states.
 */
@Composable
fun DuolingoFeedbackSheet(
    isCorrect: Boolean,
    explanation: String,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onExplainMyWay: (() -> Unit)? = null,
) {
    val backgroundColor = if (isCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0)
    val textColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
    val buttonColor = if (isCorrect) DuoGreen else DuoRedDark

    Surface(
        color = backgroundColor,
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isCorrect) "Awesome job! 🎉" else "Not quite yet — mistakes help you learn",
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            if (!isCorrect && onExplainMyWay != null) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onExplainMyWay) {
                    Text("Explain it my way", fontWeight = FontWeight.Bold, color = textColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            DuolingoButton(
                text = "CONTINUE",
                onClick = onNext,
                containerColor = buttonColor,
            )
        }
    }
}

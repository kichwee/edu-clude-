package com.example.educloud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.StreakGold
import com.example.educloud.theme.StreakOrange

/**
 * Streak badge — displays current day streak with a flame icon.
 * Shown in the HomeScreen top bar.
 */
@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    val isActive = streakDays > 0

    Row(
        modifier = modifier
            .background(
                color = if (isActive) StreakOrange.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Streak",
            tint = if (isActive) StreakOrange else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = if (streakDays > 0) "$streakDays" else "0",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) StreakOrange else Color.Gray
        )
    }
}

/**
 * Large milestone badge — shown on streak milestone achievements (7/14/30/90/365 days).
 */
@Composable
fun StreakMilestoneBadge(days: Int, modifier: Modifier = Modifier) {
    val label = when {
        days >= 365 -> "🎓 Mwanafunzi Bingwa!"
        days >= 90 -> "⭐ Hodari Sana!"
        days >= 30 -> "🌟 Shujaa wa Kujifunza!"
        days >= 14 -> "🔥 Moto wa Elimu!"
        days >= 7 -> "💪 Wiki Nzima!"
        else -> ""
    }
    if (label.isNotEmpty()) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = StreakGold.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = StreakOrange
            )
        }
    }
}

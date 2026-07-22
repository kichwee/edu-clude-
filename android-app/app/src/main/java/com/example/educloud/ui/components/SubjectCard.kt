package com.example.educloud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SubjectInfo(
    val id: String,
    val nameEn: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@Composable
fun SubjectCard(
    subject: SubjectInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = subject.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(subject.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = null,
                    tint = subject.color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = subject.nameEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = subject.color
                )
                Text(
                    text = subject.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subject.color.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

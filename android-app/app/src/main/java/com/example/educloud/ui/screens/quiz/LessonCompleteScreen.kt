package com.example.educloud.ui.screens.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainer
import com.example.educloud.theme.SurfaceContainerLow
import com.example.educloud.ui.components.TactileButton

/**
 * LessonCompleteScreen — adapts Great_Job!_27.html.
 *
 * Celebration screen shown after finishing a lesson activity.
 * Uses Canvas-drawn celebratory art (children + baskets) instead of remote images.
 */
@Composable
fun LessonCompleteScreen(
    subject: String,
    conceptNote: String = "Equal means the same amount in each group.",
    onContinue: () -> Unit,
) {
    // Bounce scale animation on mount
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Progress dots (3 of 5 complete, as example) ─────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    val active = i == 2
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (active) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) EduCloudLeaf else Color(0xFFDBDAD7)),
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Great job headline ───────────────────────────────────────────
            Text(
                text = "Great job! 🎉",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EduCloudInk,
                fontSize = 34.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You shared them equally.",
                style = MaterialTheme.typography.bodyLarge,
                color = EduCloudMutedInk,
            )

            Spacer(Modifier.height(32.dp))

            // ── Celebration illustration (Canvas-drawn) ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .scale(scale.value),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Warm background glow
                    drawCircle(
                        color = EduCloudLeaf.copy(alpha = .08f),
                        radius = size.minDimension * .55f,
                        center = Offset(size.width / 2f, size.height / 2f),
                    )
                    // Two baskets
                    listOf(.18f, .62f).forEach { xFrac ->
                        val bx = size.width * xFrac
                        val by = size.height * .55f
                        val bw = size.width * .20f
                        val bh = size.height * .30f
                        drawRoundRect(
                            color = Color(0xFFB97836),
                            topLeft = Offset(bx, by),
                            size = androidx.compose.ui.geometry.Size(bw, bh),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                        )
                        drawRoundRect(
                            color = Color(0xFF784518),
                            topLeft = Offset(bx, by),
                            size = androidx.compose.ui.geometry.Size(bw, bh),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                            style = Stroke(4f),
                        )
                        // 4 mangoes in each basket
                        repeat(4) { mi ->
                            val mx = bx + (mi % 2) * (bw * .45f) + bw * .12f
                            val my = by - (mi / 2) * (size.height * .095f) - size.height * .07f
                            drawCircle(EduCloudSun, radius = size.minDimension * .05f, center = Offset(mx, my))
                            drawCircle(Color(0xFFE79011), radius = size.minDimension * .05f, center = Offset(mx, my), style = Stroke(3f))
                            drawCircle(EduCloudLeaf, radius = size.minDimension * .010f,
                                center = Offset(mx + size.minDimension * .025f, my - size.minDimension * .040f))
                        }
                    }
                    // Child 1 (left)
                    drawCircle(Color(0xFFFFDCC5), radius = size.minDimension * .08f,
                        center = Offset(size.width * .28f, size.height * .32f))
                    // Child 2 (right)
                    drawCircle(Color(0xFFFFDCC5), radius = size.minDimension * .08f,
                        center = Offset(size.width * .72f, size.height * .32f))
                    // Confetti dots
                    val confettiColors = listOf(EduCloudLeaf, EduCloudSun, Color(0xFF6A1B9A), Color(0xFFAD1457))
                    repeat(20) { i ->
                        val cx = size.width * ((i * 37 % 100) / 100f)
                        val cy = size.height * ((i * 53 % 60) / 100f)
                        drawCircle(confettiColors[i % confettiColors.size].copy(.6f),
                            radius = size.minDimension * .015f, center = Offset(cx, cy))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── "Remember" card ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Small avatar circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SecondaryFixed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("😊", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "Remember",
                        style = MaterialTheme.typography.labelMedium,
                        color = EduCloudMutedInk,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = conceptNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EduCloudInk,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Continue button ──────────────────────────────────────────────
            TactileButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier.padding(bottom = 32.dp).navigationBarsPadding(),
            )
        }
    }
}

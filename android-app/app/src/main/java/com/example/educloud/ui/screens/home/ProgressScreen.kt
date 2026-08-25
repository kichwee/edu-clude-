package com.example.educloud.ui.screens.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.OnSecondaryContainer
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerLowest
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.StorybookPage

/**
 * ProgressScreen — adapts Your_Progress_15.html.
 *
 * Shows the learner's rolling-week stats (Answers / Time / Topics) and
 * a motivational banner, with a Canvas-drawn explorer illustration.
 * All numbers are Room-derived (plan §8); zeros mean "not yet", never placeholders.
 */
@Composable
fun ProgressScreen(
    learnerAlias: String,
    viewModel: ProgressViewModel,
    onNavigateTab: (EduNavTab) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val streak = state.streak
    val currentStreak = streak?.currentStreak ?: 0
    val weekly = state.weekly

    Scaffold(
        bottomBar = {
            EduBottomNav(activeTab = EduNavTab.Progress, onTabSelected = onNavigateTab)
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(16.dp))

                // ── Header ────────────────────────────────────────────────
                Text(
                    text = "Your progress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = EduCloudInk,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "You are making wonderful progress, $learnerAlias!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EduCloudMutedInk,
                )

                Spacer(Modifier.height(20.dp))

                // ── Illustration hero (Canvas-drawn explorer) ─────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Warm glow backdrop
                        drawCircle(EduCloudLeaf.copy(.10f), radius = size.width * .4f,
                            center = Offset(size.width * .25f, size.height * .5f))
                        // Map (rectangle)
                        drawRoundRect(Color(0xFFFFF9C4),
                            topLeft = Offset(size.width * .55f, size.height * .2f),
                            size = androidx.compose.ui.geometry.Size(size.width * .32f, size.height * .55f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f))
                        // Map lines
                        repeat(4) { i ->
                            drawLine(EduCloudLeaf.copy(.25f),
                                start = Offset(size.width * .58f, size.height * (.28f + i * .12f)),
                                end   = Offset(size.width * .84f, size.height * (.28f + i * .12f)),
                                strokeWidth = 2.5f)
                        }
                        // Explorer body (simplified)
                        drawCircle(Color(0xFFFFDCC5), radius = size.minDimension * .10f,
                            center = Offset(size.width * .28f, size.height * .35f))
                        drawRoundRect(EduCloudLeaf.copy(.6f),
                            topLeft = Offset(size.width * .18f, size.height * .5f),
                            size = androidx.compose.ui.geometry.Size(size.width * .2f, size.height * .35f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f))
                        // Hat
                        drawRoundRect(Color(0xFFB97836),
                            topLeft = Offset(size.width * .175f, size.height * .18f),
                            size = androidx.compose.ui.geometry.Size(size.width * .21f, size.height * .10f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
                        // Stars / sparkles
                        val starColor = EduCloudSun
                        listOf(Offset(.70f, .15f), Offset(.82f, .22f), Offset(.76f, .30f)).forEach { (fx, fy) ->
                            drawCircle(starColor.copy(.6f), radius = 5f, center = Offset(size.width * fx, size.height * fy))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── "This week" stats grid (rolling 7 days, real data) ──────
                Text(
                    text = "LAST 7 DAYS",
                    style = MaterialTheme.typography.labelMedium,
                    color = EduCloudMutedInk,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val minutes = weekly.minutesLearnedThisWeek.toInt()
                    val timeLabel = when {
                        minutes <= 0 -> "—"
                        minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"
                        else -> "${minutes}m"
                    }
                    StatCard(Modifier.weight(1f), "Answers", weekly.answersThisWeek.toString())
                    StatCard(Modifier.weight(1f), "Time",    timeLabel)
                    StatCard(Modifier.weight(1f), "Topics",  weekly.topicsTouchedThisWeek.toString())
                }

                Spacer(Modifier.height(16.dp))

                // ── Streak banner ────────────────────────────────────────────
                AmbientCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SecondaryFixed.copy(.35f),
                    cornerRadius = 20,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (currentStreak > 0) "Keep going! 💪" else "Start your streak today! 🌱",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer,
                        )
                        if (currentStreak > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", modifier = Modifier.padding(end = 4.dp))
                                Text(
                                    text = "$currentStreak day streak",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EduCloudLeaf,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String) {
    AmbientCard(modifier = modifier, containerColor = SurfaceContainerLowest) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = EduCloudMutedInk)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = EduCloudLeaf,
            )
        }
    }
}

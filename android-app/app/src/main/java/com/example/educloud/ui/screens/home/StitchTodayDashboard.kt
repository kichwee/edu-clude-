package com.example.educloud.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.data.model.Streak
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudLine
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudSurface
import com.example.educloud.theme.OnPrimaryContainer
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.OfflineReadyPill
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.components.TactileButton

/** Native Compose interpretation of the Stitch Today Dashboard — enhanced with
 *  EduBottomNav, learner avatar, animated offline dot, and Stitch hero card.
 */
@Composable
internal fun StitchTodayDashboard(
    learnerName: String,
    streak: Streak?,
    onOpenLearningJourney: () -> Unit,
    onOpenUssdSimulator: () -> Unit,
    onNavigateTab: (EduNavTab) -> Unit = {},
) {
    Scaffold(
        bottomBar = {
            EduBottomNav(
                activeTab = EduNavTab.Today,
                onTabSelected = onNavigateTab,
            )
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(18.dp))

                // ── Top app bar ─────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Learner avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EduCloudLeaf.copy(.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = learnerName.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                                style = MaterialTheme.typography.titleMedium,
                                color = EduCloudLeaf,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column {
                            Text("Good to see you,", style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
                            Text(
                                learnerName.ifBlank { "Learner" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EduCloudInk,
                            )
                        }
                    }
                    AnimatedOfflinePill()
                }

                // ── Hero: curriculum navigation card ───────────────────────
                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudLeaf, cornerRadius = 28) {
                    Column(Modifier.padding(20.dp)) {
                        Text("TODAY'S LESSON",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = .84f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Start a Maths adventure!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text("Pick a term, choose a topic, and solve a playful challenge.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = .9f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = .16f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                "Grade 3 · Terms 1, 2 and 3",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        TactileButton(
                            text = "Explore Grade 3 Maths",
                            onClick = onOpenLearningJourney,
                            containerColor = Color.White,
                            contentColor = EduCloudLeaf,
                        )
                    }
                }

                // ── Plan mini-cards ─────────────────────────────────────────
                Text("Your plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardMiniCard(Modifier.weight(1f), Icons.Filled.AutoStories, "1 lesson",   "about 10 min", EduCloudLake)
                    DashboardMiniCard(Modifier.weight(1f), Icons.Filled.CheckCircle, "Quick check", "3 questions",  EduCloudOrange)
                }

                // ── Offline info card ───────────────────────────────────────
                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 20) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = EduCloudLake.copy(alpha = .12f), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.OfflineBolt, null, tint = EduCloudLake,
                                modifier = Modifier.padding(10.dp).size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Learning from your local pack",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduCloudInk,
                            )
                            Text("Every answer shows its supporting lesson source.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EduCloudMutedInk,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TactileButton(
                        text = "Try the USSD simulator",
                        onClick = onOpenUssdSimulator,
                        containerColor = EduCloudLake,
                        contentColor = Color.White,
                    )
                }

                // ── Streak label ───────────────────────────────────────────
                streak?.takeIf { it.currentStreak > 0 }?.let {
                    Text("🔥 ${it.currentStreak}-day learning streak",
                        style = MaterialTheme.typography.labelLarge,
                        color = EduCloudLeaf,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Offline-ready pill with animated pulsing dot (green when online, grey when offline). */
@Composable
private fun AnimatedOfflinePill() {
    val infiniteTransition = rememberInfiniteTransition(label = "offlinePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    val dotColor by animateColorAsState(
        targetValue = EduCloudLeaf.copy(alpha = pulseAlpha),
        label = "dotColor",
    )

    Surface(color = Color.White.copy(alpha = .86f), shape = RoundedCornerShape(99.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EduCloudLine)) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
            Text("Offline ready", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EduCloudLeaf)
        }
    }
}

@Composable
private fun DashboardMiniCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    accent: Color,
) {
    AmbientCard(modifier = modifier, containerColor = EduCloudSurface, cornerRadius = 20) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(color = accent.copy(alpha = .14f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, null, tint = accent, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
        }
    }
}

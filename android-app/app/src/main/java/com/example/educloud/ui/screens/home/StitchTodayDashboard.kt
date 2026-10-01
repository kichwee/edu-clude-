package com.example.educloud.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.components.TactileButton

/** Featured home loop: daily goal, Grade 3 Maths check, tutor, optional class-code revision. */
@Composable
internal fun StitchTodayDashboard(
    learnerName: String,
    streak: Streak?,
    quizAnswersToday: Int,
    dailyGoalMet: Boolean,
    dailyGoalTarget: Int,
    onOpenLearningJourney: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: () -> Unit,
    onOpenTonightFromClass: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    pathNextTitle: String? = null,
    onNavigateTab: (EduNavTab) -> Unit = {},
) {
    val xp = streak?.totalXp?.toInt() ?: 0
    val currentStreak = streak?.currentStreak ?: 0
    val freezeReady = streak?.freezeAvailable != false
    val goalProgress = (quizAnswersToday.toFloat() / dailyGoalTarget.toFloat()).coerceIn(0f, 1f)

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

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HabitChip("🔥", if (currentStreak > 0) "$currentStreak-day streak" else "Start a streak")
                    HabitChip("⭐", "$xp XP")
                    HabitChip("🧊", if (freezeReady) "Freeze ready" else "Freeze used")
                }

                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudLeaf, cornerRadius = 28) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "TODAY'S GOAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = .84f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (dailyGoalMet) "Goal done — keep practising!" else "Finish today's Maths check",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            "Three regrouping questions. A perfect check earns mastery XP. Mistakes get a hint, never a heart.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = .9f),
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = .28f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "$quizAnswersToday / $dailyGoalTarget questions today",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = .9f),
                        )
                        Spacer(Modifier.height(18.dp))
                        TactileButton(
                            text = if (dailyGoalMet) "Practise again" else "Start today's check",
                            onClick = onOpenQuiz,
                            containerColor = Color.White,
                            contentColor = EduCloudLeaf,
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardMiniCard(
                        Modifier.weight(1f).clickable(onClick = onOpenTutor),
                        Icons.Filled.AutoStories,
                        "Ask the tutor",
                        "Explain it my way",
                        EduCloudLake,
                    )
                    DashboardMiniCard(
                        Modifier.weight(1f).clickable(onClick = onOpenLearningJourney),
                        Icons.Filled.CheckCircle,
                        "More lessons",
                        "Grade 3 catalogue",
                        EduCloudOrange,
                    )
                }

                if (!pathNextTitle.isNullOrBlank()) {
                    AmbientCard(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProgress),
                        containerColor = EduCloudSurface,
                        cornerRadius = 20,
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "YOUR PATH",
                                style = MaterialTheme.typography.labelMedium,
                                color = EduCloudLeaf,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                pathNextTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EduCloudInk,
                            )
                            Text(
                                "This week's chart and the Grade 3 path are on Progress.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EduCloudMutedInk,
                            )
                        }
                    }
                }

                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 20) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "OPTIONAL CLASS CODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = EduCloudOrange,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Tonight from class",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduCloudInk,
                        )
                        Text(
                            "If a teacher shared a class code, practise that skill here. No names, no cameras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduCloudMutedInk,
                        )
                        TactileButton(
                            text = "Enter a class code",
                            onClick = onOpenTonightFromClass,
                            containerColor = EduCloudOrange,
                        )
                    }
                }

                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 20) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = EduCloudLake.copy(alpha = .12f), shape = RoundedCornerShape(14.dp)) {
                            Icon(
                                Icons.Filled.OfflineBolt,
                                null,
                                tint = EduCloudLake,
                                modifier = Modifier.padding(10.dp).size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Works without internet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduCloudInk,
                            )
                            Text(
                                "The tutor uses the local Grade 3 pack. “Explain it my way” tries the cloud, then falls back to a story from that pack.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EduCloudMutedInk,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HabitChip(emoji: String, label: String) {
    Surface(color = Color.White.copy(alpha = .86f), shape = RoundedCornerShape(99.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EduCloudLine)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EduCloudInk)
        }
    }
}

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

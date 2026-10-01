package com.example.educloud.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.habit.WeekDayActivity
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.OnSecondaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerHighest
import com.example.educloud.theme.SurfaceContainerLowest
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.StorybookPage

/**
 * Progress: a 7-day activity chart, honest weekly counts, then the Grade 3
 * learning-path chart (moved here from the unused full-screen route).
 */
@Composable
fun ProgressScreen(
    learnerAlias: String,
    viewModel: ProgressViewModel,
    onNavigateTab: (EduNavTab) -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: (String) -> Unit,
    onOpenCatalogue: () -> Unit,
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

                Text(
                    text = "DAYS YOU PRACTISED",
                    style = MaterialTheme.typography.labelMedium,
                    color = EduCloudMutedInk,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                )
                Spacer(Modifier.height(8.dp))
                WeekActivityChart(days = state.weekDays)

                Spacer(Modifier.height(20.dp))

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
                    val answersLabel = if (weekly.answersThisWeek <= 0) "—" else weekly.answersThisWeek.toString()
                    val topicsLabel = if (weekly.topicsTouchedThisWeek <= 0) "—" else weekly.topicsTouchedThisWeek.toString()
                    StatCard(Modifier.weight(1f), "Answers", answersLabel)
                    StatCard(Modifier.weight(1f), "Time", timeLabel)
                    StatCard(Modifier.weight(1f), "Topics", topicsLabel)
                }

                if (weekly.answersThisWeek <= 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Not enough evidence yet. Finish today's Maths check to see this week.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EduCloudMutedInk,
                    )
                }

                Spacer(Modifier.height(16.dp))

                AmbientCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SecondaryFixed.copy(.35f),
                    cornerRadius = 20,
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${streak?.totalXp?.toInt() ?: 0} mastery XP  ·  freeze ${if (streak?.freezeAvailable != false) "ready" else "used"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduCloudMutedInk,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "LEARNING PATH",
                    style = MaterialTheme.typography.labelMedium,
                    color = EduCloudMutedInk,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Grade 3 Maths in this app. Topics not in the pack stay off this list.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduCloudMutedInk,
                )
                Spacer(Modifier.height(12.dp))
                AmbientCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SurfaceContainerLowest,
                    cornerRadius = 20,
                ) {
                    LearningPathSection(
                        items = state.pathItems,
                        onItemClick = { item ->
                            when (item.kind) {
                                PathKind.Quiz -> onOpenQuiz()
                                PathKind.Tutor -> onOpenTutor(item.tutorPrompt)
                            }
                        },
                        onSeeAllTopics = onOpenCatalogue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
internal fun WeekActivityChart(days: List<WeekDayActivity>, modifier: Modifier = Modifier) {
    AmbientCard(modifier = modifier.fillMaxWidth(), containerColor = SurfaceContainerLowest, cornerRadius = 20) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
            if (days.isEmpty()) {
                Text(
                    "Days light up only after you practise.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduCloudMutedInk,
                )
            } else {
                val practised = days.count { it.active }
                Text(
                    if (practised == 0) {
                        "No practice days recorded yet. Empty days stay empty."
                    } else {
                        "$practised of the last 7 days had practice."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = EduCloudMutedInk,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    days.forEach { day ->
                        WeekDayColumn(day = day, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDayColumn(day: WeekDayActivity, modifier: Modifier = Modifier) {
    val status = when {
        day.active && day.isToday -> "practised today"
        day.active -> "practised"
        day.isToday -> "today, no practice yet"
        else -> "no practice"
    }
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "${day.label} ${day.dayOfMonth}, $status"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val barHeight = if (day.active) 72.dp else 14.dp
            val barColor = when {
                day.active && day.isToday -> EduCloudLeaf
                day.active -> EduCloudLeaf.copy(alpha = 0.72f)
                else -> SurfaceContainerHighest
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (!day.active && day.isToday) {
                            Modifier.border(2.dp, EduCloudLeaf.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        } else {
                            Modifier
                        },
                    )
                    .background(barColor),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = day.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (day.isToday) EduCloudLeaf else EduCloudMutedInk,
        )
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = EduCloudInk,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
        )
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

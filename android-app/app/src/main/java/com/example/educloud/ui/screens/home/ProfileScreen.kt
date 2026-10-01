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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.PrimaryFixed
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerLowest
import com.example.educloud.theme.TertiaryFixed
import com.example.educloud.data.model.Streak
import com.example.educloud.habit.HabitEngine
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.InterestChipGrid
import com.example.educloud.ui.components.StorybookPage

data class Achievement(
    val title: String,
    val description: String,
    val earned: Boolean,
    val emoji: String,
)

/**
 * ProfileScreen — profile + achievements.
 * Adapts Achievements_17.html / the Profile tab concept.
 *
 * Honest by design (plan §8): XP is the mastery-only total (D11) and every
 * achievement comes from [HabitEngine] milestone tables over real Room data.
 */
@Composable
fun ProfileScreen(
    learnerAlias: String,
    grade: String = "Grade 3",
    streak: Streak? = null,
    viewModel: ProfileViewModel,
    onOpenUssdSimulator: () -> Unit = {},
    onNavigateTab: (EduNavTab) -> Unit,
) {
    val profileState by viewModel.state.collectAsState()
    val xpPoints = streak?.totalXp?.toInt() ?: 0
    val earnedIds = remember(streak) {
        HabitEngine.milestonesFor(
            streakDays = streak?.longestStreak ?: 0,
            totalXp = xpPoints,
            lessonsPassed = streak?.lessonsPassed ?: 0,
        ).map { it.id }.toSet()
    }
    val achievements = remember(earnedIds) {
        HabitEngine.catalogMilestones().map { m ->
            Achievement(
                title = m.title,
                description = achievementDescription(m.id),
                earned = m.id in earnedIds,
                emoji = m.emoji,
            )
        }
    }
    Scaffold(
        bottomBar = {
            EduBottomNav(activeTab = EduNavTab.Profile, onTabSelected = onNavigateTab)
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Avatar + name ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(EduCloudLeaf.copy(.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    // Storybook-style canvas portrait background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(EduCloudLeaf.copy(.08f), radius = size.minDimension / 2f)
                    }
                    Text(
                        text = learnerAlias.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                        style = MaterialTheme.typography.displaySmall,
                        color = EduCloudLeaf,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = learnerAlias.ifBlank { "Learner" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EduCloudInk,
                )
                Text(text = grade, style = MaterialTheme.typography.bodyMedium, color = EduCloudMutedInk)

                Spacer(Modifier.height(20.dp))

                // ── XP / points chip ────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(PrimaryFixed.copy(.6f))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = EduCloudLeaf, modifier = Modifier.size(20.dp))
                    Text(
                        text = "$xpPoints XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EduCloudLeaf,
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Achievements section ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
                    Text("${achievements.count { it.earned }} / ${achievements.size}",
                        style = MaterialTheme.typography.labelLarge, color = EduCloudMutedInk)
                }

                Spacer(Modifier.height(12.dp))

                achievements.forEach { a ->
                    AchievementRow(achievement = a)
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(8.dp))
                Text("What you love", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Hard lessons can be explained through these. Pick up to 3.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduCloudMutedInk,
                )
                Spacer(Modifier.height(12.dp))
                InterestChipGrid(
                    selected = profileState.interests,
                    onToggle = viewModel::toggleInterest,
                )

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onOpenUssdSimulator) {
                    Text("Open feature-phone demo", color = EduCloudMutedInk)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    AmbientCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (achievement.earned) PrimaryFixed.copy(.25f) else SurfaceContainerLowest,
        cornerRadius = 16,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (achievement.earned) PrimaryContainer else Color(0xFFE3E2E0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(achievement.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.earned) EduCloudInk else EduCloudMutedInk,
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = EduCloudMutedInk,
                )
            }
            if (achievement.earned) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EduCloudLeaf, modifier = Modifier.size(22.dp))
            }
        }
    }
}

/** Kind-level blurb for a milestone id (streak_7 → streak copy). Unknown ids stay generic. */
private fun achievementDescription(id: String): String = when {
    id.startsWith("streak") -> "Learning day after day"
    id.startsWith("xp")     -> "Mastery points earned"
    else                    -> "Lessons passed"
}

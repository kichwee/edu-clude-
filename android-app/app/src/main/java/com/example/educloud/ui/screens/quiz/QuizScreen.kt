package com.example.educloud.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.educloud.habit.HabitEngine
import com.example.educloud.ui.components.DuoGreen
import com.example.educloud.ui.components.DuoGreenDark
import com.example.educloud.ui.components.StorybookPage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    subject: String,
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onExplainMyWay: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(subject) {
        viewModel.init(subject)
    }

    if (state.isFinished) {
        val result = state.lessonResult
        QuizResultsScreen(
            score = state.score,
            total = state.totalAnswered,
            subject = subject,
            xpGained = result?.xpGained ?: 0,
            currentStreak = result?.currentStreak ?: 0,
            newMilestones = result?.newMilestones ?: emptyList(),
            canRequestPersonalisedRemediation = state.score < state.totalAnswered && state.subject == QuizSubjectCatalog.MATHEMATICS_ID,
            remediationMessage = state.remediationMessage,
            onRequestPersonalisedRemediation = viewModel::requestPersonalisedRemediation,
            onExplainMyWay = {
                val missed = "Help me with today's regrouping questions"
                onExplainMyWay(missed)
            },
            onDone = onBack
        )
        return
    }

    state.unavailableMessage?.let { message ->
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = { Text("Quick check unavailable") },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack) { Text("Go back") }
                }
            }
        }
        return
    }

    val question = state.currentQuestion

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    com.example.educloud.ui.components.DuolingoProgressBar(
                        progress = (state.questionIndex + 1) / 3f,
                        streak = state.score + 1,
                    )
                },
            )
        },
        bottomBar = {
            if (state.isAnswered) {
                com.example.educloud.ui.components.DuolingoFeedbackSheet(
                    isCorrect = state.isCorrect,
                    explanation = question?.explanation ?: "",
                    onNext = { viewModel.nextQuestion() },
                    onExplainMyWay = question?.text?.let { text ->
                        { onExplainMyWay(text) }
                    },
                )
            }
        }
    ) { paddingValues ->
        if (question != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Mascot Coaching Bubble
                com.example.educloud.ui.components.MascotBubble(
                    text = "Read the question carefully! You're doing awesome!"
                )

                Spacer(Modifier.height(24.dp))

                // Question Text
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // Options
                question.options.forEachIndexed { index, optionText ->
                    val isSelected = state.selectedOption == index
                    val isAnswered = state.isAnswered
                    val isCorrectOption = question.correctIndex == index

                    val containerColor = when {
                        !isAnswered && isSelected -> com.example.educloud.ui.components.DuoBlue.copy(alpha = 0.15f)
                        isAnswered && isCorrectOption -> Color(0xFFD7FFB8)
                        isAnswered && isSelected && !isCorrectOption -> Color(0xFFFFDFE0)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val borderColor = when {
                        isAnswered && isCorrectOption -> com.example.educloud.ui.components.DuoGreen
                        isAnswered && isSelected && !isCorrectOption -> com.example.educloud.ui.components.DuoRedDark
                        isSelected -> com.example.educloud.ui.components.DuoBlue
                        else -> com.example.educloud.ui.components.DuoCardBorder
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        onClick = { if (!isAnswered) viewModel.selectAnswer(index) },
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(2.5.dp, borderColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                color = if (isAnswered && isCorrectOption) com.example.educloud.ui.components.DuoGreenDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun QuizResultsScreen(
    score: Int,
    total: Int,
    subject: String,
    xpGained: Int = 0,
    currentStreak: Int = 0,
    newMilestones: List<HabitEngine.Milestone> = emptyList(),
    canRequestPersonalisedRemediation: Boolean = false,
    remediationMessage: String? = null,
    onRequestPersonalisedRemediation: () -> Unit = {},
    onExplainMyWay: () -> Unit = {},
    onDone: () -> Unit
) {
    val percentage = (score.toFloat() / total.toFloat()) * 100
    var showConsentDialog by remember { mutableStateOf(false) }

    StorybookPage {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏆", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Well done!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // ── Milestone ceremony: chips stagger in for each threshold just crossed ──
            if (newMilestones.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    newMilestones.forEachIndexed { index, milestone ->
                        MilestoneChip(milestone = milestone, index = index)
                    }
                }
            }

            if (canRequestPersonalisedRemediation) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onExplainMyWay,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Explain it my way")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showConsentDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create an offline practice lesson")
                }
                remediationMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You answered $score out of $total correctly",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "You scored ${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Habit earnings: mastery-only XP (D11) and the live streak (D10) ──
            if (xpGained > 0 || currentStreak > 0) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (xpGained > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(DuoGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "+$xpGained XP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = DuoGreenDark,
                            )
                        }
                    }
                    if (currentStreak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", modifier = Modifier.padding(end = 4.dp))
                            Text(
                                text = "$currentStreak-day streak",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Finish")
            }
        }
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Create a personal practice lesson?") },
            text = {
                Text(
                    "For this demo, EduCloud will send only these anonymous wrong Maths answers—never your name, messages, phone number, or device details—to prepare a review-required offline lesson.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsentDialog = false
                    onRequestPersonalisedRemediation()
                }) { Text("I agree") }
            },
            dismissButton = { TextButton(onClick = { showConsentDialog = false }) { Text("Keep it offline") } },
        )
    }
}

/** Spring-pops in after [index] * 150 ms so milestones arrive as a small ceremony. */
@Composable
private fun MilestoneChip(milestone: HabitEngine.Milestone, index: Int) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 150L)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Row(
        modifier = Modifier
            .scale(scale.value)
            .clip(RoundedCornerShape(20.dp))
            .background(DuoGreen.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(milestone.emoji)
        Spacer(Modifier.width(6.dp))
        Text(
            text = milestone.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DuoGreenDark,
        )
    }
}

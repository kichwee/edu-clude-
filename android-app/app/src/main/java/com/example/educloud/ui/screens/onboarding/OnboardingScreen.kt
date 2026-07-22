package com.example.educloud.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.PrimaryFixed
import com.example.educloud.theme.SurfaceContainerLow

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    if (state.isDone) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(modifier = Modifier.padding(top = 36.dp)) {
                Text(
                    text = "EduCloud",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = EduCloudLeaf,
                    fontSize = 32.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (state.step) {
                        0 -> "Let's personalise your space."
                        else -> "Choose your learning level."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = EduCloudMutedInk,
                )
            }

            // Step indicator — pill dots (mirrors LessonProgressDots)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(2) { i ->
                    val isActive = i == state.step
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) EduCloudLeaf else Color(0xFFDBDAD7)),
                    )
                }
            }

            // Step content
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { step ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (step) {
                        0 -> NameStep(
                            alias = state.alias,
                            onAliasChange = viewModel::setAlias,
                            onNext = {
                                keyboard?.hide()
                                if (state.alias.isNotBlank()) viewModel.nextStep()
                            }
                        )
                        1 -> GradeStep()
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step > 0) {
                    OutlinedButton(
                        onClick = viewModel::prevStep,
                        modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (state.step < 1) viewModel.nextStep()
                        else viewModel.completeOnboarding()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isLoading && (state.step != 0 || state.alias.isNotBlank())
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (state.step < 1) "Continue" else "Start learning")
                    }
                }
            }

            state.error?.let { error ->
                Snackbar(modifier = Modifier.padding(bottom = 8.dp)) { Text(error) }
            }
        }
    }
}

@Composable
private fun NameStep(alias: String, onAliasChange: (String) -> Unit, onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Emoji avatar slot
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(EduCloudLeaf.copy(.12f))
                .border(2.dp, EduCloudLeaf.copy(.20f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (alias.isNotBlank()) alias.first().uppercaseChar().toString() else "🌟",
                fontSize = 36.sp,
                color = EduCloudLeaf,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = EduCloudInk,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Your name stays only on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = EduCloudMutedInk,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = onAliasChange,
            label = { Text("Your name, e.g. Amina") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun GradeStep() {
    val grades = listOf(
        Triple("Grade 1", "Ages 6–7",  false),
        Triple("Grade 2", "Ages 7–8",  false),
        Triple("Grade 3", "Ages 8–9",  true),   // the MVP grade
        Triple("Grade 4", "Ages 9–10", false),
        Triple("Grade 5", "Ages 10–11",false),
    )
    Column {
        Text(
            "Your learning level",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = EduCloudInk,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Grade 3 Mathematics content is available in this demo.",
            style = MaterialTheme.typography.bodyMedium,
            color = EduCloudMutedInk,
        )
        Spacer(Modifier.height(20.dp))
        grades.forEach { (grade, ageRange, available) ->
            GradePill(grade = grade, ageRange = ageRange, available = available)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun GradePill(grade: String, ageRange: String, available: Boolean) {
    val bg = if (available) PrimaryFixed.copy(.5f) else SurfaceContainerLow
    val border = if (available) EduCloudLeaf.copy(.4f) else Color(0xFFDBDAD7)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                grade,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (available) FontWeight.Bold else FontWeight.Normal,
                color = if (available) EduCloudLeaf else EduCloudMutedInk,
            )
            Text(
                ageRange,
                style = MaterialTheme.typography.bodySmall,
                color = EduCloudMutedInk,
            )
        }
        if (available) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Available",
                tint = EduCloudLeaf,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = Color(0xFFDBDAD7),
            ) {
                Text(
                    "Coming soon",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = EduCloudMutedInk,
                )
            }
        }
    }
}

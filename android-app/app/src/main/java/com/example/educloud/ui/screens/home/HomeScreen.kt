package com.example.educloud.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.educloud.ui.components.EduNavTab

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenCatalogue: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: () -> Unit,
    onOpenTonightFromClass: () -> Unit = {},
    onNavigateTab: (EduNavTab) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    StitchTodayDashboard(
        learnerName = state.student?.alias.orEmpty(),
        streak = state.streak,
        quizAnswersToday = state.quizAnswersToday,
        dailyGoalMet = state.dailyGoalMet,
        dailyGoalTarget = state.dailyGoalTarget,
        onOpenLearningJourney = onOpenCatalogue,
        onOpenQuiz = onOpenQuiz,
        onOpenTutor = onOpenTutor,
        onOpenTonightFromClass = onOpenTonightFromClass,
        onOpenProgress = { onNavigateTab(EduNavTab.Progress) },
        pathNextTitle = state.pathNextTitle,
        onNavigateTab = onNavigateTab,
    )
}

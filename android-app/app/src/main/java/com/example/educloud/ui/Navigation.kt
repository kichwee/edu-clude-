package com.example.educloud.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educloud.EduCloudApp
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.screens.chat.ChatScreen
import com.example.educloud.ui.screens.chat.ChatViewModel
import com.example.educloud.ui.screens.catalog.GradePickerScreen
import com.example.educloud.ui.screens.catalog.MathCatalogueScreen
import com.example.educloud.ui.screens.catalog.MathTermScreen
import com.example.educloud.ui.screens.catalog.MathUnitScreen
import com.example.educloud.ui.screens.catalog.SubjectPickerScreen
import com.example.educloud.ui.screens.home.HomeScreen
import com.example.educloud.ui.screens.home.HomeViewModel
import com.example.educloud.ui.screens.home.LearningPathScreen
import com.example.educloud.ui.screens.home.ProfileScreen
import com.example.educloud.ui.screens.home.ProgressScreen
import com.example.educloud.ui.screens.home.StitchExperienceScreen
import com.example.educloud.ui.screens.featurephone.FeaturePhoneScreen
import com.example.educloud.ui.screens.featurephone.FeaturePhoneViewModel
import com.example.educloud.ui.screens.onboarding.OnboardingScreen
import com.example.educloud.ui.screens.onboarding.OnboardingViewModel
import com.example.educloud.ui.screens.onboarding.SplashScreen
import com.example.educloud.ui.screens.quiz.LessonCompleteScreen
import com.example.educloud.ui.screens.quiz.QuizScreen
import com.example.educloud.ui.screens.quiz.QuizViewModel
import com.example.educloud.ui.screens.subjecthub.StitchSubjectExplorer

@Composable
fun EduCloudNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as EduCloudApp
    val navController = rememberNavController()

    // Determine initial route based on active student
    val activeStudent by app.studentRepository.activeStudent.collectAsState(initial = null)
    // Use a basic loading state to wait for DB emission
    if (activeStudent == null && !app.studentRepository.hasCheckedInitialStudent) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             CircularProgressIndicator()
         }
         return
    }

    // Always start with splash; it will forward to onboarding or home.
    val startDestination = "splash"

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Splash ────────────────────────────────────────────────────────
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    val dest = if (activeStudent != null) "home" else "onboarding"
                    navController.navigate(dest) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            val viewModel: OnboardingViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return OnboardingViewModel(app.studentRepository) as T
                    }
                }
            )
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val viewModel: HomeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HomeViewModel(app.studentRepository, app.learningRepository) as T
                    }
                }
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenCatalogue = { navController.navigate("mathCatalogue") },
                onOpenUssdSimulator = { navController.navigate("featurePhone") },
                onNavigateTab = { tab ->
                    when (tab) {
                        EduNavTab.Today    -> { /* already here */ }
                        EduNavTab.Learn    -> navController.navigate("subjectExplorer")
                        EduNavTab.Progress -> navController.navigate("progress")
                        EduNavTab.Profile  -> navController.navigate("profile")
                    }
                },
            )
        }

        composable("grades") {
            GradePickerScreen(
                onGradeSelected = { grade -> navController.navigate("subjects/$grade") },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "subjects/{grade}",
            arguments = listOf(navArgument("grade") { type = NavType.IntType }),
        ) { backStackEntry ->
            val grade = backStackEntry.arguments?.getInt("grade") ?: 3
            SubjectPickerScreen(
                grade = grade,
                onMathSelected = { navController.navigate("mathCatalogue") },
                onBack = { navController.popBackStack() },
            )
        }

        composable("mathCatalogue") {
            MathCatalogueScreen(
                onTermSelected = { term -> navController.navigate("mathTerm/$term") },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "mathTerm/{term}",
            arguments = listOf(navArgument("term") { type = NavType.IntType }),
        ) { backStackEntry ->
            val term = backStackEntry.arguments?.getInt("term") ?: 0
            MathTermScreen(
                termIndex = term,
                onUnitSelected = { unit -> navController.navigate("mathUnit/$term/$unit") },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "mathUnit/{term}/{unit}",
            arguments = listOf(
                navArgument("term") { type = NavType.IntType },
                navArgument("unit") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val term = backStackEntry.arguments?.getInt("term") ?: 0
            val unit = backStackEntry.arguments?.getInt("unit") ?: 0
            MathUnitScreen(
                termIndex = term,
                unitIndex = unit,
                onOpenTutor = { navController.navigate("chat/math") },
                onBack = { navController.popBackStack() },
            )
        }

        composable("stitchExperience") {
            StitchExperienceScreen(
                learnerName = activeStudent?.alias.orEmpty(),
                onOpenMath = { navController.navigate("chat/math") },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "chat/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "math"
            val viewModel: ChatViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ChatViewModel(
                            context,
                            app.studentRepository,
                            app.learningRepository,
                            app.offlineLessonRepository,
                        ) as T
                    }
                }
            )
            ChatScreen(
                subject = subject,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("featurePhone") {
            val viewModel: FeaturePhoneViewModel = viewModel()
            FeaturePhoneScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        // ── New Stitch screens ────────────────────────────────────────────

        composable("subjectExplorer") {
            StitchSubjectExplorer(
                learnerAlias = activeStudent?.alias.orEmpty(),
                onSubjectSelected = { subject -> navController.navigate("quiz/$subject") },
                onNavigateTab = { tab ->
                    when (tab) {
                        EduNavTab.Today    -> navController.navigate("home") { popUpTo("home") { inclusive = false } }
                        EduNavTab.Learn    -> { /* already here */ }
                        EduNavTab.Progress -> navController.navigate("progress")
                        EduNavTab.Profile  -> navController.navigate("profile")
                    }
                },
            )
        }

        composable("learningPath") {
            LearningPathScreen(
                onBack = { navController.popBackStack() },
                onNavigateTab = { tab ->
                    when (tab) {
                        EduNavTab.Today    -> navController.navigate("home") { popUpTo("home") { inclusive = false } }
                        EduNavTab.Learn    -> navController.navigate("subjectExplorer")
                        EduNavTab.Progress -> { /* already showing progress context */ }
                        EduNavTab.Profile  -> navController.navigate("profile")
                    }
                },
            )
        }

        composable("progress") {
            ProgressScreen(
                learnerAlias = activeStudent?.alias.orEmpty(),
                onNavigateTab = { tab ->
                    when (tab) {
                        EduNavTab.Today    -> navController.navigate("home") { popUpTo("home") { inclusive = false } }
                        EduNavTab.Learn    -> navController.navigate("subjectExplorer")
                        EduNavTab.Progress -> { /* already here */ }
                        EduNavTab.Profile  -> navController.navigate("profile")
                    }
                },
            )
        }

        composable("profile") {
            ProfileScreen(
                learnerAlias = activeStudent?.alias.orEmpty(),
                onNavigateTab = { tab ->
                    when (tab) {
                        EduNavTab.Today    -> navController.navigate("home") { popUpTo("home") { inclusive = false } }
                        EduNavTab.Learn    -> navController.navigate("subjectExplorer")
                        EduNavTab.Progress -> navController.navigate("progress")
                        EduNavTab.Profile  -> { /* already here */ }
                    }
                },
            )
        }

        composable(
            route = "lessonComplete/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType }),
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "Math"
            LessonCompleteScreen(
                subject = subject,
                onContinue = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = "quiz/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "math"
            val viewModel: QuizViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return QuizViewModel(context, app.studentRepository, app.learningRepository) as T
                    }
                }
            )
            QuizScreen(
                subject = subject,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

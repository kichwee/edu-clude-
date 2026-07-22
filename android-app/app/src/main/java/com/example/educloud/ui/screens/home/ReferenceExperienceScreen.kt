package com.example.educloud.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudLine
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.ui.components.EqualSharesArt
import com.example.educloud.ui.components.OfflineReadyPill
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.components.StorybookPrimaryButton
import com.example.educloud.ui.components.StorybookSecondaryButton

/**
 * Native Compose adaptation of every supplied Stitch screen. Screens that promise an
 * unavailable MVP capability remain explicitly local/informational instead of faking it.
 */
@Composable
internal fun StitchExperienceScreen(learnerName: String, onOpenMath: () -> Unit, onBack: () -> Unit) {
    var page by remember { mutableStateOf(ReferencePage.Index) }
    var typedName by remember { mutableStateOf(learnerName) }
    StorybookPage {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            ReferenceHeader(page, onBack = { if (page == ReferencePage.Index) onBack() else page = ReferencePage.Index })
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    ReferencePage.Index -> StitchScreenIndex { page = it }
                    ReferencePage.Splash, ReferencePage.Welcome, ReferencePage.Name, ReferencePage.Grade,
                    ReferencePage.Picker, ReferencePage.Consent, ReferencePage.Pack -> SetupPages(page, typedName, { typedName = it }) { page = it }
                    ReferencePage.Today, ReferencePage.Plan, ReferencePage.Inbox, ReferencePage.Search -> TodayPages(page, learnerName) { page = it }
                    ReferencePage.Library, ReferencePage.Curriculum, ReferencePage.TopicLibrary, ReferencePage.TopicDetail,
                    ReferencePage.LessonPreview, ReferencePage.Saved -> LibraryPages(page) { page = it }
                    ReferencePage.Player, ReferencePage.Practice, ReferencePage.TutorAnswer, ReferencePage.QuickCheck,
                    ReferencePage.Feedback, ReferencePage.Summary, ReferencePage.Progress, ReferencePage.Settings -> LessonPages(page, learnerName, onOpenMath) { page = it }
                    ReferencePage.Achievements, ReferencePage.Story, ReferencePage.Streak, ReferencePage.Guardians -> ExtraPages(page) { page = it }
                }
                Spacer(Modifier.height(16.dp))
            }
            ReferenceBottomBar(page) { page = it }
        }
    }
}

private enum class ReferencePage(val title: String) {
    Index("All screens"),
    Splash(""), Welcome("Welcome"), Name("Your name"), Grade("Choose your grade"), Picker("Who is learning today?"), Consent("For guardians"), Pack("Offline pack"),
    Today("Today"), Plan("Your plan"), Inbox("Notifications"), Search("Find a lesson"),
    Library("Subjects"), Curriculum("Mathematics · Grade 3"), TopicLibrary("Numbers & operations"), TopicDetail("Equal shares"), LessonPreview("Lesson preview"), Saved("Saved lessons"),
    Player("Equal shares"), Practice("Guided practice"), TutorAnswer("Tutor answer"), QuickCheck("Quick check"), Feedback("Great work"), Summary("Lesson complete"), Progress("Your progress"), Settings("Profile & settings"),
    Achievements("Achievements"), Story("Story time"), Streak("Streak"), Guardians("For guardians")
}

private data class StitchScreen(val exportName: String, val destination: ReferencePage)

/** All 39 local Stitch exports have a visible native destination from this index. */
@Composable
private fun StitchScreenIndex(open: (ReferencePage) -> Unit) {
    val screens = listOf(
        StitchScreen("Achievements", ReferencePage.Achievements),
        StitchScreen("Choose Grade", ReferencePage.Grade),
        StitchScreen("Downloads", ReferencePage.Saved),
        StitchScreen("Equal Concept", ReferencePage.TutorAnswer),
        StitchScreen("Equal Shares Intro", ReferencePage.LessonPreview),
        StitchScreen("Explore Subjects", ReferencePage.Library),
        StitchScreen("For Guardians", ReferencePage.Guardians),
        StitchScreen("Great Job!", ReferencePage.Feedback),
        StitchScreen("Learning Journey", ReferencePage.Plan),
        StitchScreen("Learning Journey Refined", ReferencePage.TopicLibrary),
        StitchScreen("Learning Path", ReferencePage.Curriculum),
        StitchScreen("Lesson Activity", ReferencePage.Practice),
        StitchScreen("Lesson Complete", ReferencePage.Summary),
        StitchScreen("Library Story", ReferencePage.Story),
        StitchScreen("Name Setup", ReferencePage.Name),
        StitchScreen("Pick a Learner", ReferencePage.Picker),
        StitchScreen("Splash Screen", ReferencePage.Splash),
        StitchScreen("Splash Screen 2", ReferencePage.Splash),
        StitchScreen("Splash Screen Refined", ReferencePage.Splash),
        StitchScreen("Streak Tracker", ReferencePage.Streak),
        StitchScreen("Today Dashboard", ReferencePage.Today),
        StitchScreen("Prototype 1", ReferencePage.Splash),
        StitchScreen("Prototype 3", ReferencePage.Welcome),
        StitchScreen("Prototype 5", ReferencePage.Splash),
        StitchScreen("Prototype 8", ReferencePage.Welcome),
        StitchScreen("Prototype 11", ReferencePage.Splash),
        StitchScreen("Prototype 12", ReferencePage.Welcome),
        StitchScreen("Prototype 13", ReferencePage.Splash),
        StitchScreen("Prototype 16", ReferencePage.Welcome),
        StitchScreen("Prototype 20", ReferencePage.Welcome),
        StitchScreen("Prototype 28", ReferencePage.Welcome),
        StitchScreen("Prototype 31", ReferencePage.Welcome),
        StitchScreen("Prototype 32", ReferencePage.Welcome),
        StitchScreen("Prototype 34", ReferencePage.Welcome),
        StitchScreen("Prototype 35", ReferencePage.Splash),
        StitchScreen("Prototype 36", ReferencePage.Splash),
        StitchScreen("Prototype 37", ReferencePage.Welcome),
        StitchScreen("Welcome", ReferencePage.Welcome),
        StitchScreen("Your Progress", ReferencePage.Progress),
    )
    Text("Stitch screen library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
    Text("39 Android-adapted screens. The duplicate prototypes are retained as entry-screen visual variants.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    screens.forEach { screen ->
        ReferenceCard(screen.exportName, "Open Android adaptation", EduCloudLeaf) { open(screen.destination) }
    }
}

@Composable
private fun ReferenceHeader(page: ReferencePage, onBack: () -> Unit) {
    if (page != ReferencePage.Today && page != ReferencePage.Splash && page != ReferencePage.Welcome) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EduCloudInk), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
            Spacer(Modifier.width(10.dp))
            Text(page.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
        }
    }
}

@Composable
private fun SetupPages(page: ReferencePage, typedName: String, onName: (String) -> Unit, go: (ReferencePage) -> Unit) {
    when (page) {
        ReferencePage.Splash -> {
            Spacer(Modifier.height(80.dp)); Text("EduCloud", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("Preparing your Grade 3 Maths lessons", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp)); EqualSharesArt(Modifier.fillMaxWidth().height(250.dp)); Spacer(Modifier.height(24.dp)); OfflineReadyPill(); StorybookPrimaryButton("Start", { go(ReferencePage.Welcome) }, Modifier.padding(top = 20.dp))
        }
        ReferencePage.Welcome -> {
            Spacer(Modifier.height(40.dp)); Text("Learn in\nsmall steps.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("Lessons stay ready, even when the internet does not.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(24.dp)); EqualSharesArt(Modifier.fillMaxWidth().height(280.dp)); StorybookPrimaryButton("Start learning", { go(ReferencePage.Name) }, Modifier.padding(top = 24.dp))
        }
        ReferencePage.Name -> {
            Spacer(Modifier.height(26.dp)); Text("What should we call you?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("This name stays on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = typedName, onValueChange = onName, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(16.dp))
            ReferenceNote("This MVP does not create a cloud account or upload learner data."); StorybookPrimaryButton("Continue", { go(ReferencePage.Grade) }, Modifier.padding(top = 24.dp))
        }
        ReferencePage.Grade -> {
            Spacer(Modifier.height(18.dp)); Text("Choose the lessons\nthat fit you.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("Only Grade 3 Maths is installed in this offline MVP.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            GradeGrid(); StorybookPrimaryButton("Continue", { go(ReferencePage.Picker) }, Modifier.padding(top = 20.dp))
        }
        ReferencePage.Picker -> {
            Text("Who is learning today?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("This phone can be shared. Only one local learner profile is available in this MVP.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProfileRow(typedName.ifBlank { "Learner" }, "Grade 3 · Equal shares", true); ProfileRow("Add learner", "Not available in this MVP", false)
            StorybookPrimaryButton("Continue", { go(ReferencePage.Consent) }, Modifier.padding(top = 14.dp))
        }
        ReferencePage.Consent -> {
            Text("For guardians", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            ReferenceCard("A safe offline demo", "No phone number, guardian account, cloud profile, or real telephony is used.", EduCloudLeaf)
            ReferenceCard("Content status", "Original demo content; teacher review is still required before a real school rollout.", EduCloudSun)
            StorybookPrimaryButton("Continue", { go(ReferencePage.Pack) }, Modifier.padding(top = 14.dp))
        }
        ReferencePage.Pack -> {
            Text("Your offline lesson pack", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            ReferenceCard("Grade 3 Maths demo pack", "3 original lessons · available on this device", EduCloudLake)
            ReferenceCard("Downloads", "There is no live download service in this MVP.", EduCloudSun)
            StorybookPrimaryButton("Go to today", { go(ReferencePage.Today) }, Modifier.padding(top = 14.dp))
        }
        else -> Unit
    }
}

@Composable
private fun TodayPages(page: ReferencePage, learnerName: String, go: (ReferencePage) -> Unit) {
    when (page) {
        ReferencePage.Today -> {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) { Column { Text("Good afternoon,", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(learnerName.ifBlank { "Learner" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk) }; OfflineReadyPill() }
            ReferenceCard("Continue: Equal shares", "Mathematics · Grade 3 · Lesson 1 of 3", EduCloudLake, { go(ReferencePage.LessonPreview) })
            EqualSharesArt(Modifier.fillMaxWidth().height(190.dp)); TwoCards("Today’s plan", "One quick review", { go(ReferencePage.Plan) }, "Find a lesson", "Search local content", { go(ReferencePage.Search) })
            ReferenceCard("Notifications", "One lesson is ready to review", EduCloudSun, { go(ReferencePage.Inbox) })
        }
        ReferencePage.Plan -> { Text("Your plan for today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); PlanItem("One quick review", "5 min", true); PlanItem("Continue equal shares", "15 min", false); PlanItem("Try a quick check", "3 questions", false) }
        ReferencePage.Inbox -> { Text("Notifications", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Review reminder", "You have one local lesson to review.", EduCloudSun); ReferenceCard("Pack ready", "The Grade 3 Maths demo pack is available offline.", EduCloudLeaf); ReferenceCard("No cloud alerts", "This MVP does not receive remote notifications.", EduCloudLake) }
        ReferencePage.Search -> { var query by remember { mutableStateOf("") }; Text("Find a lesson", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); OutlinedTextField(query, { query = it }, label = { Text("Search local lessons") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Filled.Search, null) }, shape = RoundedCornerShape(16.dp)); ReferenceCard("Equal shares", "Mathematics · Grade 3 · 15 min", EduCloudLake, { go(ReferencePage.LessonPreview) }); ReferenceCard("Add within 100", "Mathematics · Grade 3 · coming next", EduCloudSun) }
        else -> Unit
    }
}

@Composable
private fun LibraryPages(page: ReferencePage, go: (ReferencePage) -> Unit) {
    when (page) {
        ReferencePage.Library -> { Text("Subjects", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Mathematics", "Grade 3 · installed", EduCloudLeaf, { go(ReferencePage.Curriculum) }); DisabledCard("English", "Not included in this MVP"); DisabledCard("Science & Technology", "Not included in this MVP"); DisabledCard("Kiswahili", "Not included in this MVP") }
        ReferencePage.Curriculum -> { Text("Grade 3 curriculum map", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("1 · Numbers and operations", "Start here", EduCloudOrange, { go(ReferencePage.TopicLibrary) }); DisabledCard("2 · Patterns and algebra", "Not included in this MVP"); DisabledCard("3 · Measurement", "Not included in this MVP") }
        ReferencePage.TopicLibrary -> { Text("Numbers and operations", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Equal shares", "1 lesson · 15 min", EduCloudLeaf, { go(ReferencePage.TopicDetail) }); ReferenceCard("Add within 100", "1 lesson · 15 min", EduCloudLake, { go(ReferencePage.TopicDetail) }); ReferenceCard("Groups & multiplication", "1 lesson · 15 min", EduCloudSun, { go(ReferencePage.TopicDetail) }) }
        ReferencePage.TopicDetail -> { Text("Equal shares", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); EqualSharesArt(Modifier.fillMaxWidth().height(220.dp)); ReferenceCard("Lesson 1 · What is sharing?", "Completed in the demo flow", EduCloudLeaf); ReferenceCard("Lesson 2 · Equal groups", "Start lesson", EduCloudOrange, { go(ReferencePage.LessonPreview) }); ReferenceCard("Lesson 3 · Sharing with more", "Locked in this demo", EduCloudLine) }
        ReferencePage.LessonPreview -> { Text("Equal groups", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); EqualSharesArt(Modifier.fillMaxWidth().height(245.dp)); ReferenceNote("Objective: share items into equal groups. This lesson uses original EduCloud demo content."); StorybookPrimaryButton("Start lesson", { go(ReferencePage.Player) }) }
        ReferencePage.Saved -> { Text("Saved on this device", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Equal shares", "Maths · Grade 3 · 12 MB demo pack", EduCloudLeaf); ReferenceCard("Add within 100", "Maths · Grade 3 · available offline", EduCloudLake); ReferenceNote("Storage and download controls are informational only in this MVP.") }
        else -> Unit
    }
}

@Composable
private fun LessonPages(page: ReferencePage, learnerName: String, onOpenMath: () -> Unit, go: (ReferencePage) -> Unit) {
    when (page) {
        ReferencePage.Player -> { Text("Equal shares", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); Text("Let’s share 8 mangoes equally.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); EqualSharesArt(Modifier.fillMaxWidth().height(280.dp)); StorybookPrimaryButton("Try it", { go(ReferencePage.Practice) }) }
        ReferencePage.Practice -> { Text("Put 8 mangoes into\n2 equal groups.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); EqualSharesArt(Modifier.fillMaxWidth().height(280.dp)); TwoCards("Show a hint", "Both groups need the same amount", { go(ReferencePage.TutorAnswer) }, "Check", "Use the quick check", { go(ReferencePage.QuickCheck) }) }
        ReferencePage.TutorAnswer -> { Text("What does equal mean?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Amina asked", "Equal means both groups have the same amount. Each basket has 4 mangoes.", EduCloudLake); EqualSharesArt(Modifier.fillMaxWidth().height(210.dp)); StorybookSecondaryButton("Ask the real offline tutor", onOpenMath) }
        ReferencePage.QuickCheck -> { Text("Which shows equal sharing?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); EqualSharesArt(Modifier.fillMaxWidth().height(230.dp)); ReferenceCard("Choose an answer", "The two baskets should have the same number of mangoes.", EduCloudLeaf); StorybookSecondaryButton("See feedback", { go(ReferencePage.Feedback) }) }
        ReferencePage.Feedback -> { Spacer(Modifier.height(30.dp)); Text("Great work, $learnerName!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); Text("You shared 8 mangoes into 2 equal groups.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); ReferenceCard("What you did well", "You put 4 mangoes into each group.", EduCloudSun); StorybookPrimaryButton("Continue", { go(ReferencePage.Summary) }) }
        ReferencePage.Summary -> { Spacer(Modifier.height(30.dp)); Text("Lesson complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Equal shares", "You learned how to share items equally. +10 practice points", EduCloudLeaf); StorybookPrimaryButton("Back to today", { go(ReferencePage.Today) }) }
        ReferencePage.Progress -> { Text("Your progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Mathematics", "1 / 3 demo lessons", EduCloudSun); ReferenceCard("Learning path", "Equal shares · current lesson", EduCloudLeaf); ReferenceCard("Streak", "Local demo: 1 day", EduCloudLake, { go(ReferencePage.Streak) }); ReferenceCard("Achievements", "6 local practice badges", EduCloudOrange, { go(ReferencePage.Achievements) }) }
        ReferencePage.Settings -> { Text("$learnerName · Grade 3", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); ReferenceCard("Offline lesson pack", "Included on this device", EduCloudLeaf, { go(ReferencePage.Saved) }); ReferenceCard("For guardians", "Local privacy and safety information", EduCloudLake, { go(ReferencePage.Guardians) }); ReferenceCard("View setup screens", "Reference-mapped first-run flow", EduCloudSun, { go(ReferencePage.Splash) }) }
        else -> Unit
    }
}

@Composable
private fun ExtraPages(page: ReferencePage, go: (ReferencePage) -> Unit) {
    when (page) {
        ReferencePage.Achievements -> {
            Text("Achievements", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("You have earned 6 badges", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ReferenceCard("First lesson", "Completed an offline Maths lesson", EduCloudSun)
            ReferenceCard("Equal sharer", "Made two groups with the same amount", EduCloudLeaf)
            ReferenceCard("Quick checker", "Completed a three-question review", EduCloudLake)
        }
        ReferencePage.Story -> {
            Text("The giraffe and the stars", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            EqualSharesArt(Modifier.fillMaxWidth().height(240.dp))
            ReferenceNote("Story Time is a visual direction from Stitch. English story content is not installed in the Grade 3 Maths MVP.")
            DisabledCard("Read story", "Not available in this Maths-only MVP")
        }
        ReferencePage.Streak -> {
            Text("Streak", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            ReferenceCard("1 day", "Current local learning streak", EduCloudLeaf)
            ReferenceCard("This week", "One learning day recorded on this device", EduCloudSun)
            ReferenceNote("Streaks remain on the device and are not sent to guardians or a cloud service.")
        }
        ReferencePage.Guardians -> {
            Text("For guardians", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text("A safe space to learn and grow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
            ReferenceCard("Age-appropriate flow", "Grade 3 Maths content only", EduCloudLeaf)
            ReferenceCard("No ads or real messaging", "The MVP does not send SMS, USSD, or notifications", EduCloudSun)
            ReferenceCard("Works offline", "Learner questions and progress remain on this device", EduCloudLake)
            StorybookSecondaryButton("View local progress", { go(ReferencePage.Progress) })
        }
        else -> Unit
    }
}

@Composable private fun GradeGrid() {
    val grades = listOf("Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 24.dp)) { grades.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { grade -> Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (grade == "Grade 3") Color(0xFFFFE9B0) else Color.White), border = BorderStroke(1.dp, if (grade == "Grade 3") EduCloudSun else EduCloudLine)) { Text(grade + if (grade == "Grade 3") "  ✓" else "", modifier = Modifier.padding(18.dp), fontWeight = FontWeight.Bold, color = if (grade == "Grade 3") EduCloudInk else MaterialTheme.colorScheme.onSurfaceVariant) } } } }
}
}

@Composable private fun ProfileRow(name: String, detail: String, enabled: Boolean) { Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .9f)), border = BorderStroke(1.dp, EduCloudLine)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = if (enabled) EduCloudLeaf.copy(alpha = .15f) else EduCloudLine.copy(alpha = .35f), shape = RoundedCornerShape(12.dp)) { Icon(if (enabled) Icons.Filled.Person else Icons.Filled.Lock, null, modifier = Modifier.padding(10.dp).size(25.dp), tint = if (enabled) EduCloudLeaf else EduCloudInk) }; Spacer(Modifier.width(12.dp)); Column { Text(name, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun ReferenceCard(title: String, detail: String, accent: Color, onClick: (() -> Unit)? = null) { Card(onClick = onClick ?: {}, enabled = onClick != null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .92f)), border = BorderStroke(1.dp, EduCloudLine)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = accent.copy(alpha = .16f), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Filled.AutoStories, null, tint = accent, modifier = Modifier.padding(10.dp).size(23.dp)) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun DisabledCard(title: String, detail: String) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F0E9)), border = BorderStroke(1.dp, EduCloudLine)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Lock, null, tint = EduCloudLine); Spacer(Modifier.width(12.dp)); Column { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun PlanItem(title: String, detail: String, complete: Boolean) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .9f)), border = BorderStroke(1.dp, EduCloudLine)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (complete) Icons.Filled.CheckCircle else Icons.Filled.AutoStories, null, tint = if (complete) EduCloudLeaf else EduCloudSun); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun TwoCards(firstTitle: String, firstDetail: String, firstClick: () -> Unit, secondTitle: String, secondDetail: String, secondClick: () -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Card(onClick = firstClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .9f)), border = BorderStroke(1.dp, EduCloudLine)) { Column(Modifier.padding(14.dp)) { Text(firstTitle, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(firstDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; Card(onClick = secondClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .9f)), border = BorderStroke(1.dp, EduCloudLine)) { Column(Modifier.padding(14.dp)) { Text(secondTitle, fontWeight = FontWeight.Bold, color = EduCloudInk); Text(secondDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun ReferenceNote(text: String) { Surface(color = Color.White.copy(alpha = .76f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, EduCloudLine), modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text(text, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun ReferenceBottomBar(page: ReferencePage, go: (ReferencePage) -> Unit) { Surface(color = Color.White.copy(alpha = .96f), shadowElevation = 8.dp) { Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 10.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceAround) { BottomItem("Today", page == ReferencePage.Today, { go(ReferencePage.Today) }); BottomItem("Learn", page in setOf(ReferencePage.Library, ReferencePage.Curriculum, ReferencePage.TopicLibrary, ReferencePage.TopicDetail, ReferencePage.LessonPreview, ReferencePage.Saved), { go(ReferencePage.Library) }); BottomItem("Progress", page == ReferencePage.Progress, { go(ReferencePage.Progress) }); BottomItem("Profile", page == ReferencePage.Settings, { go(ReferencePage.Settings) }) } } }
@Composable private fun BottomItem(label: String, selected: Boolean, click: () -> Unit) { Button(onClick = click, colors = ButtonDefaults.buttonColors(containerColor = if (selected) EduCloudOrange.copy(alpha = .14f) else Color.Transparent, contentColor = if (selected) EduCloudOrange else EduCloudInk), shape = RoundedCornerShape(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } }

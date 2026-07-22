package com.example.educloud.ui.screens.catalog

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudSurface
import com.example.educloud.theme.PrimaryFixed
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerLow
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.components.TactileButton

// ── Curriculum data ─────────────────────────────────────────────────────────

private data class MathUnit(val title: String, val bookPages: String, val demoAvailable: Boolean = true)
private data class MathTerm(val name: String, val emoji: String, val units: List<MathUnit>)

/** Route boundary for catalogue deep links; invalid indexes must never reach a list subscript. */
internal object CatalogueRoutes {
    fun hasTerm(termIndex: Int): Boolean = termIndex in mathTerms.indices

    fun hasUnit(termIndex: Int, unitIndex: Int): Boolean =
        unitIndex in mathTerms.getOrNull(termIndex)?.units.orEmpty().indices
}

private val mathTerms = listOf(
    MathTerm("Term One", "📘", listOf(
        MathUnit("Numbers and whole numbers",            "pages 3–21", demoAvailable = true),
        MathUnit("Fractions",                            "pages 22–29"),
        MathUnit("Addition",                             "pages 30–50"),
        MathUnit("Subtraction",                          "pages 51–64"),
        MathUnit("Multiplication",                       "pages 65–70"),
        MathUnit("Division",                             "pages 71–76"),
        MathUnit("Measurement",                          "pages 77–92"),
        MathUnit("Geometry, position and direction",     "pages 93–98"),
    )),
    MathTerm("Term Two", "📗", listOf(
        MathUnit("Numbers and whole numbers",            "pages 101–112"),
        MathUnit("Fractions",                            "pages 113–117"),
        MathUnit("Addition",                             "pages 118–133"),
        MathUnit("Subtraction",                          "pages 134–149"),
        MathUnit("Multiplication",                       "pages 150–157"),
        MathUnit("Division",                             "pages 158–163"),
        MathUnit("Measurement",                          "pages 164–185"),
        MathUnit("Geometry, position and direction",     "pages 186–188"),
    )),
    MathTerm("Term Three", "📙", listOf(
        MathUnit("Numbers and whole numbers",            "pages 191–200"),
        MathUnit("Fractions",                            "pages 201–203"),
        MathUnit("Addition",                             "pages 204–212"),
        MathUnit("Subtraction",                          "pages 213–222"),
        MathUnit("Multiplication",                       "pages 223–228"),
        MathUnit("Division",                             "pages 229–231"),
        MathUnit("Measurement",                          "pages 232–247"),
        MathUnit("Geometry, position and direction",     "pages 248–249"),
    )),
)

// ── 1. Grade Picker ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradePickerScreen(onGradeSelected: (Int) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Grade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = EduCloudLeaf,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EduCloudSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                TactileButton(
                    text = "Continue",
                    onClick = { onGradeSelected(3) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        containerColor = EduCloudSurface,
    ) { padding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // Intro text
                Text(
                    "Choose the lessons that fit you best. This helps us personalise your learning journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduCloudMutedInk,
                )

                // Pre-primary
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Early Years", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
                    GradeRowCard(
                        label = "Pre-primary",
                        detail = "Foundation skills & play",
                        icon = Icons.Filled.School,
                        enabled = false,
                        onClick = {},
                    )
                }

                // Primary
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Primary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk)
                    Text(
                        "Grade 3 is the working prototype. All other grades are planned catalogue paths.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EduCloudMutedInk,
                    )
                    // 2-column grid for grades
                    val grades = (1..8).toList()
                    val rows = grades.chunked(2)
                    rows.forEach { rowGrades ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowGrades.forEach { grade ->
                                GradeGridCell(
                                    grade = grade,
                                    isActive = grade == 3,
                                    modifier = Modifier.weight(1f),
                                    onClick = { if (grade == 3) onGradeSelected(grade) },
                                )
                            }
                            // Pad last row if odd
                            if (rowGrades.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                // Secondary
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Secondary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EduCloudInk.copy(.5f))
                    AmbientCard(
                        modifier = Modifier.fillMaxWidth().alpha(.6f),
                        containerColor = Color(0xFFE9E8E5),
                        cornerRadius = 20,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Filled.Schedule, null, tint = EduCloudMutedInk, modifier = Modifier.size(22.dp))
                            Column {
                                Text("Grades 7 & 8", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EduCloudMutedInk)
                                Text("Coming soon", style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GradeGridCell(grade: Int, isActive: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (isActive) EduCloudOrange.copy(.18f) else Color.White.copy(.85f)
    val borderColor = if (isActive) EduCloudOrange.copy(.5f) else Color(0xFFC2C9B8).copy(.5f)
    val textColor = if (isActive) EduCloudInk else EduCloudMutedInk

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(if (isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .scale(if (isActive) 1.05f else 1f)
            .clickable(enabled = isActive, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(EduCloudOrange, RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Current", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Grade $grade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                color = textColor,
            )
            if (isActive) {
                Icon(Icons.Filled.Check, null, tint = EduCloudOrange, modifier = Modifier.size(18.dp).padding(top = 4.dp))
            } else {
                Icon(Icons.Filled.Lock, null, tint = Color(0xFFC2C9B8), modifier = Modifier.size(16.dp).padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun GradeRowCard(label: String, detail: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    AmbientCard(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else .65f),
        containerColor = Color.White.copy(.9f),
        cornerRadius = 22,
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(color = EduCloudLeaf.copy(.12f), shape = RoundedCornerShape(14.dp)) {
                Icon(icon, null, tint = EduCloudLeaf, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
            }
            if (!enabled) Icon(Icons.Filled.Lock, null, tint = Color(0xFFC2C9B8), modifier = Modifier.size(18.dp))
        }
    }
}

// ── 2. Subject Picker ────────────────────────────────────────────────────────

private data class Subject(val name: String, val emoji: String, val available: Boolean)

private val subjects = listOf(
    Subject("Mathematics",        "🔢", true),
    Subject("English",            "🔤", false),
    Subject("Kiswahili",          "💬", false),
    Subject("Integrated Science", "🔬", false),
    Subject("Social Studies",     "🌍", false),
    Subject("Creative Arts",      "🎨", false),
    Subject("Religious Education","✨", false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectPickerScreen(grade: Int, onMathSelected: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade $grade Subjects", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = EduCloudLeaf) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        containerColor = EduCloudSurface,
    ) { padding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    if (grade == 3) "Mathematics has the local lesson pack ready. Other subjects will show their catalogue when content packs are installed."
                    else "No lesson packs are installed for this grade yet. Subjects are shown for catalogue planning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduCloudMutedInk,
                )
                val displaySubjects = if (grade == 3) subjects else subjects.map { it.copy(available = false) }
                displaySubjects.forEach { subject ->
                    SubjectCard(subject = subject, onClick = { if (subject.available) onMathSelected() })
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    val bg = if (subject.available) PrimaryFixed.copy(.4f) else Color.White.copy(.85f)
    val borderColor = if (subject.available) EduCloudLeaf.copy(.35f) else Color(0xFFC2C9B8).copy(.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(if (subject.available) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = subject.available, onClick = onClick)
            .padding(16.dp)
            .alpha(if (subject.available) 1f else .72f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Emoji icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (subject.available) EduCloudLeaf.copy(.14f) else Color(0xFFE9E8E5)),
            contentAlignment = Alignment.Center,
        ) {
            Text(subject.emoji, fontSize = 22.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(
                subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (subject.available) FontWeight.Bold else FontWeight.Normal,
                color = if (subject.available) EduCloudInk else EduCloudMutedInk,
            )
            Text(
                if (subject.available) "Open Terms and Units" else "Local pack not installed",
                style = MaterialTheme.typography.bodySmall,
                color = if (subject.available) EduCloudLeaf else EduCloudMutedInk,
            )
        }
        if (subject.available) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = EduCloudLeaf, modifier = Modifier.size(20.dp))
        } else {
            Surface(color = Color(0xFFE9E8E5), shape = RoundedCornerShape(99.dp)) {
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

// ── 3. Term Selection ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathCatalogueScreen(onTermSelected: (Int) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade 3 Mathematics", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = EduCloudLeaf) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        containerColor = EduCloudSurface,
    ) { padding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose a term and topic. The tutor uses its Grade 3 Maths learning pack to guide each adventure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduCloudMutedInk,
                )
                mathTerms.forEachIndexed { index, term ->
                    TermCard(term = term, onClick = { onTermSelected(index) })
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TermCard(term: MathTerm, onClick: () -> Unit) {
    val hasDemoLessons = term.units.any { it.demoAvailable }
    AmbientCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = EduCloudLeaf.copy(.07f),
        cornerRadius = 22,
    ) {
        Row(
            modifier = Modifier
                .alpha(if (hasDemoLessons) 1f else .62f)
                .clickable(enabled = hasDemoLessons, onClick = onClick)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EduCloudLeaf.copy(.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(term.emoji, fontSize = 26.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(term.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = EduCloudInk)
                Text(if (hasDemoLessons) "Maths adventures ready" else "Lessons coming soon", style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
            }
            if (hasDemoLessons) Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = EduCloudLeaf, modifier = Modifier.size(22.dp))
        }
    }
}

// ── 4. Unit Selection ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathTermScreen(termIndex: Int, onUnitSelected: (Int) -> Unit, onBack: () -> Unit) {
    val term = mathTerms.getOrNull(termIndex)
    if (term == null) {
        UnavailableCatalogueScreen(onBack = onBack)
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grade 3 Maths", style = MaterialTheme.typography.labelMedium, color = EduCloudLeaf)
                        Text(term.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EduCloudInk)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        containerColor = EduCloudSurface,
    ) { padding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tap a topic to open the tutor. Ask a question, get a hint, or play a short maths challenge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduCloudMutedInk,
                )
                term.units.forEachIndexed { index, unit ->
                    UnitCard(unit = unit, onClick = { onUnitSelected(index) })
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun UnitCard(unit: MathUnit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (unit.demoAvailable) Color.White.copy(.92f) else Color(0xFFE9E8E5))
            .border(1.dp, Color(0xFFC2C9B8).copy(.5f), RoundedCornerShape(18.dp))
            .alpha(if (unit.demoAvailable) 1f else .62f)
            .clickable(enabled = unit.demoAvailable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (unit.demoAvailable) EduCloudLeaf else EduCloudMutedInk),
        )
        Column(Modifier.weight(1f)) {
            Text(unit.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EduCloudInk)
            Text(if (unit.demoAvailable) "Ready to explore" else "Coming soon", style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
        }
        if (unit.demoAvailable) Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = EduCloudOrange, modifier = Modifier.size(18.dp))
    }
}

// ── 5. Unit Detail ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathUnitScreen(termIndex: Int, unitIndex: Int, onOpenTutor: () -> Unit, onBack: () -> Unit) {
    val term = mathTerms.getOrNull(termIndex)
    val unit = term?.units?.getOrNull(unitIndex)
    if (term == null || unit == null) {
        UnavailableCatalogueScreen(onBack = onBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Detail", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = EduCloudLeaf) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        containerColor = EduCloudSurface,
    ) { padding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // Grade + Term chip
                Surface(color = EduCloudLake.copy(.12f), shape = RoundedCornerShape(99.dp)) {
                    Text(
                        "Grade 3 Mathematics · ${term.name}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = EduCloudLake,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Unit name
                Text(unit.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = EduCloudInk)

                // Book pages card
                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 18) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Book, null, tint = EduCloudLake, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Reference page range", style = MaterialTheme.typography.labelSmall, color = EduCloudMutedInk)
                            Text(unit.bookPages, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EduCloudInk)
                        }
                    }
                }

                // Status badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE9E8E5))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Filled.Info,
                        null,
                        tint = EduCloudMutedInk,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            if (unit.demoAvailable) "Ready for your maths adventure" else "Lessons unavailable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduCloudMutedInk,
                        )
                        Text(
                            if (unit.demoAvailable) "Ask a question or choose a challenge. The tutor will use the closest Grade 3 Maths lesson it can find." else "Choose another topic to keep learning.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduCloudMutedInk,
                        )
                    }
                }

                if (unit.demoAvailable) {
                    TactileButton(
                        text = "Start this maths adventure",
                        onClick = onOpenTutor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnavailableCatalogueScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalogue unavailable", fontWeight = FontWeight.Bold, color = EduCloudLeaf) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EduCloudLeaf)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "That catalogue link is no longer available. Return to the Grade 3 Maths catalogue and choose a term.",
                style = MaterialTheme.typography.bodyLarge,
                color = EduCloudMutedInk,
            )
        }
    }
}

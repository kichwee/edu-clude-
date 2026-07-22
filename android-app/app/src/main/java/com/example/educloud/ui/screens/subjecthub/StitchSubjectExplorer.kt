package com.example.educloud.ui.screens.subjecthub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudPaper
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.OnPrimaryContainer
import com.example.educloud.theme.OnSecondaryContainer
import com.example.educloud.theme.OnSecondaryFixedVariant
import com.example.educloud.theme.OnTertiaryFixedVariant
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.PrimaryFixed
import com.example.educloud.theme.SecondaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.TertiaryFixed
import com.example.educloud.theme.TertiaryFixedDim
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.screens.quiz.QuizSubjectCatalog

data class SubjectCard(
    val id: String,
    val title: String,
    val available: Boolean,
    val headerColor: Color,
    val labelColor: Color,
    val textColor: Color,
    val drawArt: DrawScope.() -> Unit,
)

/**
 * StitchSubjectExplorer — adapts Explore_Subjects_6.html.
 *
 * 2-column grid of subject cards. Only Grade 3 Mathematics has a canonical
 * quiz ID and a local question bank in this MVP; the remaining cards state
 * their unavailable status instead of navigating with display labels.
 */
@Composable
fun StitchSubjectExplorer(
    learnerAlias: String,
    onSubjectSelected: (String) -> Unit,
    onNavigateTab: (EduNavTab) -> Unit,
) {
    val subjects = listOf(
        SubjectCard(QuizSubjectCatalog.MATHEMATICS_ID, "Mathematics", true, TertiaryFixed, PrimaryContainer, OnPrimaryContainer, mathArt),
        SubjectCard("english", "English", false, PrimaryFixed, EduCloudLeaf, Color.White, englishArt),
        SubjectCard("kiswahili", "Kiswahili", false, SecondaryFixed, SecondaryContainer, OnSecondaryContainer, kiswahiliArt),
        SubjectCard("science", "Science & Tech", false, TertiaryFixedDim, Color(0xFF004C6C), Color.White, scienceArt),
        SubjectCard("social-studies", "Social Studies", false, PrimaryFixed, PrimaryContainer, OnPrimaryContainer, socialArt),
        SubjectCard("creative-arts", "Creative Arts", false, SecondaryFixed, SecondaryFixed, OnSecondaryFixedVariant, artsArt),
    )

    Scaffold(
        bottomBar = {
            EduBottomNav(activeTab = EduNavTab.Learn, onTabSelected = onNavigateTab)
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                // ── Top app bar ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Learner avatar (initials circle)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EduCloudLeaf.copy(.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = learnerAlias.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                            style = MaterialTheme.typography.titleMedium,
                            color = EduCloudLeaf,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "EduCloud",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduCloudLeaf,
                    )
                    Spacer(Modifier.width(40.dp))
                }

                // ── Section header ─────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Where shall we explore today?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduCloudInk,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Mathematics is ready now. Other subjects are not available in this MVP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EduCloudMutedInk,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Subject grid ────────────────────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(subjects) { subject ->
                        SubjectCardItem(
                            subject = subject,
                            onClick = { onSubjectSelected(subject.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCardItem(subject: SubjectCard, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = EduCloudLeaf.copy(.06f),
                spotColor = EduCloudLeaf.copy(.06f),
            )
            .background(EduCloudPaper)
            .clickable(enabled = subject.available, onClick = onClick)
            .alpha(if (subject.available) 1f else 0.65f),
    ) {
        // Illustration area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(subject.headerColor),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                subject.drawArt(this)
                // Bottom gradient overlay (matches HTML `from-black/40`)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(.25f)),
                        startY = size.height * .5f,
                        endY   = size.height,
                    ),
                    size = size,
                )
            }
        }
        // Label strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(subject.labelColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = subject.title,
                style = MaterialTheme.typography.labelLarge,
                color = subject.textColor,
                fontWeight = FontWeight.Bold,
            )
            if (!subject.available) {
                Text(
                    text = "Unavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = subject.textColor,
                )
            }
        }
    }
}

// ── Canvas art lambdas (offline illustrations) ─────────────────────────────

private val mathArt: DrawScope.() -> Unit = {
    // Mango counting scene — circles arranged in groups
    repeat(6) { i ->
        val x = size.width * (.15f + i * .14f)
        drawCircle(EduCloudSun, radius = size.minDimension * .08f, center = Offset(x, size.height * .45f))
        drawCircle(Color(0xFFE79011), radius = size.minDimension * .08f, center = Offset(x, size.height * .45f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
    }
    // Basket
    drawRoundRect(Color(0xFFB97836), topLeft = Offset(size.width * .3f, size.height * .62f),
        size = androidx.compose.ui.geometry.Size(size.width * .4f, size.height * .25f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f))
}

private val englishArt: DrawScope.() -> Unit = {
    // Open book pages
    val cx = size.width / 2f
    val cy = size.height * .5f
    drawRoundRect(Color.White.copy(.9f), topLeft = Offset(cx - size.width * .32f, cy - size.height * .25f),
        size = androidx.compose.ui.geometry.Size(size.width * .3f, size.height * .5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    drawRoundRect(Color.White.copy(.9f), topLeft = Offset(cx + size.width * .02f, cy - size.height * .25f),
        size = androidx.compose.ui.geometry.Size(size.width * .3f, size.height * .5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    // Spine line
    drawLine(Color(0xFFB97836), start = Offset(cx, cy - size.height * .28f), end = Offset(cx, cy + size.height * .28f), strokeWidth = 3f)
    // Lines of text (dots)
    repeat(3) { row ->
        repeat(4) { col ->
            drawCircle(EduCloudLeaf.copy(.3f), radius = 3f,
                center = Offset(cx - size.width * .28f + col * (size.width * .06f), cy - size.height * .1f + row * (size.height * .12f)))
        }
    }
}

private val kiswahiliArt: DrawScope.() -> Unit = {
    // Two children talking (simplified silhouettes)
    listOf(.3f, .7f).forEachIndexed { i, x ->
        drawCircle(Color(0xFFFFDCC5), radius = size.minDimension * .10f,
            center = Offset(size.width * x, size.height * .38f))
        drawRoundRect(if (i == 0) Color(0xFFAD1457).copy(.6f) else Color(0xFF6A1B9A).copy(.6f),
            topLeft = Offset(size.width * x - size.minDimension * .12f, size.height * .5f),
            size = androidx.compose.ui.geometry.Size(size.minDimension * .24f, size.height * .3f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f))
    }
    // Picture card between them
    drawRoundRect(Color.White.copy(.85f), topLeft = Offset(size.width * .42f, size.height * .38f),
        size = androidx.compose.ui.geometry.Size(size.width * .16f, size.height * .22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
}

private val scienceArt: DrawScope.() -> Unit = {
    // Microscope simplified
    val cx = size.width * .5f
    val cy = size.height * .55f
    drawLine(Color.White.copy(.8f), start = Offset(cx, cy - size.height * .3f), end = Offset(cx, cy), strokeWidth = 8f)
    drawCircle(Color.White.copy(.6f), radius = size.minDimension * .12f, center = Offset(cx, cy - size.height * .3f))
    drawRoundRect(Color.White.copy(.5f), topLeft = Offset(cx - size.width * .15f, cy),
        size = androidx.compose.ui.geometry.Size(size.width * .3f, size.height * .12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
    // Decorative plant
    drawCircle(EduCloudLeaf.copy(.7f), radius = size.minDimension * .07f,
        center = Offset(size.width * .25f, size.height * .45f))
}

private val socialArt: DrawScope.() -> Unit = {
    // Savanna sunset — horizon line
    drawRect(Color(0xFFFFD600).copy(.3f), topLeft = Offset(0f, size.height * .55f), size = androidx.compose.ui.geometry.Size(size.width, size.height * .5f))
    // Sun
    drawCircle(EduCloudSun, radius = size.minDimension * .13f, center = Offset(size.width * .75f, size.height * .38f))
    // Tree silhouette
    drawLine(Color(0xFF5D4037), start = Offset(size.width * .3f, size.height * .55f), end = Offset(size.width * .3f, size.height * .3f), strokeWidth = 6f)
    drawCircle(EduCloudLeaf.copy(.7f), radius = size.minDimension * .13f, center = Offset(size.width * .3f, size.height * .27f))
}

private val artsArt: DrawScope.() -> Unit = {
    // Palette blobs
    val colors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), EduCloudSun, Color(0xFF8E24AA))
    colors.forEachIndexed { i, c ->
        val angle = (i * 72f) * (Math.PI / 180f)
        val cx = size.width * .5f + (size.width * .25f * Math.cos(angle)).toFloat()
        val cy = size.height * .5f + (size.height * .25f * Math.sin(angle)).toFloat()
        drawCircle(c.copy(.7f), radius = size.minDimension * .10f, center = Offset(cx, cy))
    }
    // Palette outline
    drawCircle(Color.White.copy(.4f), radius = size.minDimension * .35f,
        center = Offset(size.width * .5f, size.height * .5f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
}

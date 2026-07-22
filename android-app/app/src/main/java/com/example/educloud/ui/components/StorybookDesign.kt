package com.example.educloud.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudLine
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudPaper
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.OnSecondaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerLowest

// ─────────────────────────────────────────────────────────────────────────────
//  StorybookPage  — warm paper background with ambient canvas blobs
// ─────────────────────────────────────────────────────────────────────────────
/** Shared reference-inspired chrome for the truthful, offline Grade 3 Maths MVP. */
@Composable
fun StorybookPage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EduCloudPaper)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(EduCloudSun.copy(alpha = .13f),  radius = size.minDimension * .18f, center = Offset(size.width * .92f, size.height * .08f))
            drawCircle(EduCloudLake.copy(alpha = .06f), radius = size.minDimension * .34f, center = Offset(size.width * .04f, size.height * .92f))
            drawCircle(EduCloudLeaf.copy(alpha = .07f), radius = size.minDimension * .14f, center = Offset(size.width * .94f, size.height * .72f))
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  EduBottomNav  — 4-tab nav matching every Stitch screen's <nav> element
// ─────────────────────────────────────────────────────────────────────────────
enum class EduNavTab { Today, Learn, Progress, Profile }

@Composable
fun EduBottomNav(
    activeTab: EduNavTab,
    onTabSelected: (EduNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        Triple(EduNavTab.Today,    Icons.Filled.Home,        Icons.Outlined.Home),
        Triple(EduNavTab.Learn,    Icons.Filled.MenuBook,    Icons.Outlined.MenuBook),
        Triple(EduNavTab.Progress, Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
        Triple(EduNavTab.Profile,  Icons.Filled.Person,      Icons.Outlined.Person),
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainerLowest,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                val isActive = tab == activeTab
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) SecondaryFixed else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tab_bg_${tab.name}"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isActive) OnSecondaryContainer else EduCloudMutedInk,
                    label = "tab_color_${tab.name}"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(tab) }
                        .padding(vertical = 4.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(bgColor),
                    ) {
                        Icon(
                            imageVector = if (isActive) filledIcon else outlinedIcon,
                            contentDescription = tab.name,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = tab.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = contentColor,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LessonProgressDots  — pill-dot progress indicator from Lesson_Activity_26
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LessonProgressDots(total: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val isActive = i == current
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) EduCloudLeaf else Color(0xFFDBDAD7)),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AmbientCard  — card with Stitch's green ambient shadow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AmbientCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceContainerLowest,
    cornerRadius: Int = 20,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(cornerRadius.dp),
            ambientColor = EduCloudLeaf.copy(alpha = .08f),
            spotColor   = EduCloudLeaf.copy(alpha = .08f),
        ),
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TactileButton  — primary pill button with 3-D bottom shadow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TactileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = EduCloudLeaf,
    contentColor: Color = Color.White,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .offset(y = if (pressed) 3.dp else 0.dp)
            .shadow(
                elevation = if (pressed) 0.dp else 4.dp,
                shape = RoundedCornerShape(99.dp),
                ambientColor = EduCloudLeaf.copy(.25f),
                spotColor   = EduCloudLeaf.copy(.25f),
            ),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(99.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Legacy reusable components (preserved)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OfflineReadyPill() {
    Surface(color = Color.White.copy(alpha = .86f), shape = RoundedCornerShape(99.dp), border = androidx.compose.foundation.BorderStroke(1.dp, EduCloudLine)) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.WifiOff, contentDescription = null, modifier = Modifier.size(14.dp), tint = EduCloudLeaf)
            Text("Offline ready", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EduCloudLeaf)
        }
    }
}

@Composable
fun StorybookPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EduCloudOrange, contentColor = Color.White)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun StorybookSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EduCloudLake, contentColor = Color.White)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

/** Native, small-footprint lesson art: twelve mangoes shared across two baskets. */
@Composable
fun EqualSharesArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        drawRoundRect(Color(0xFFFFF0CD), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f))
        val basketWidth  = size.width  * .30f
        val basketHeight = size.height * .25f
        val basketTop    = size.height * .59f
        listOf(size.width * .13f, size.width * .57f).forEach { left ->
            drawRoundRect(Color(0xFFB97836), topLeft = Offset(left, basketTop), size = Size(basketWidth, basketHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f))
            drawRoundRect(Color(0xFF784518), topLeft = Offset(left, basketTop), size = Size(basketWidth, basketHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f), style = Stroke(5f))
        }
        repeat(12) { index ->
            val column = index % 6
            val row    = index / 6
            val x = size.width  * (.15f + column * .14f)
            val y = size.height * (.22f + row    * .18f)
            drawCircle(EduCloudSun,         radius = size.minDimension * .055f, center = Offset(x, y))
            drawCircle(Color(0xFFE79011),   radius = size.minDimension * .055f, center = Offset(x, y), style = Stroke(3f))
            drawCircle(EduCloudLeaf,        radius = size.minDimension * .012f, center = Offset(x + size.minDimension * .028f, y - size.minDimension * .045f))
        }
        drawCircle(Color.White.copy(alpha = .72f), radius = size.minDimension * .09f, center = Offset(size.width * .84f, size.height * .16f))
    }
}

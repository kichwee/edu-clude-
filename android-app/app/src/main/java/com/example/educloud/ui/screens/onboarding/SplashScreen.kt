package com.example.educloud.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrangeLight
import com.example.educloud.theme.EduCloudPaper
import com.example.educloud.theme.EduCloudSun
import com.example.educloud.theme.PrimaryFixed
import kotlinx.coroutines.delay

/**
 * Splash screen — adapts Splash_Screen_21.html.
 *
 * Shows the EduCloud brand on a warm paper background with decorative canvas blobs
 * (replacing the online background image for offline-first operation).
 * Auto-navigates after 1.5 s.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    // Animate a simple fade-in for polish
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(600))
        delay(1500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EduCloudPaper),
        contentAlignment = Alignment.TopCenter,
    ) {
        // ── Decorative illustration layer (Canvas-drawn, offline-safe) ─────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Large warm meadow circle — base "ground"
            drawCircle(
                color = EduCloudLeaf.copy(alpha = .08f),
                radius = size.width * .9f,
                center = Offset(size.width / 2f, size.height * 1.15f),
            )
            // Golden sun glow — top right
            drawCircle(
                color = EduCloudSun.copy(alpha = .18f),
                radius = size.width * .35f,
                center = Offset(size.width * .85f, size.height * .12f),
            )
            // Inner sun
            drawCircle(
                color = EduCloudSun.copy(alpha = .28f),
                radius = size.width * .18f,
                center = Offset(size.width * .85f, size.height * .12f),
            )
            // Sky-blue tertiary blob — bottom left
            drawCircle(
                color = EduCloudLake.copy(alpha = .06f),
                radius = size.width * .5f,
                center = Offset(0f, size.height * .85f),
            )
            // Peach organic blob — mid right
            drawCircle(
                color = EduCloudOrangeLight.copy(alpha = .30f),
                radius = size.width * .22f,
                center = Offset(size.width * .92f, size.height * .50f),
            )
            // Leaf-green small blob — bottom centre
            drawCircle(
                color = PrimaryFixed.copy(alpha = .25f),
                radius = size.width * .30f,
                center = Offset(size.width * .45f, size.height * .78f),
            )
            // Top gradient fade (simulates the HTML from-surface-bright/80 gradient)
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(EduCloudPaper.copy(alpha = .80f), Color.Transparent),
                    startY = 0f,
                    endY = size.height * .25f,
                ),
                size = size,
            )
        }

        // ── Content overlay ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(top = 140.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "EduCloud",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                color = EduCloudInk,
                letterSpacing = (-0.8).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Learn. Grow. Shine.",
                style = MaterialTheme.typography.bodyLarge,
                color = EduCloudMutedInk,
                fontSize = 18.sp,
            )
        }
    }
}

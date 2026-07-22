package com.example.educloud.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.OnSecondaryContainer
import com.example.educloud.theme.PrimaryContainer
import com.example.educloud.theme.SecondaryContainer
import com.example.educloud.theme.SecondaryFixed
import com.example.educloud.theme.SurfaceContainerHighest
import com.example.educloud.theme.SurfaceContainerLowest
import com.example.educloud.ui.components.EduBottomNav
import com.example.educloud.ui.components.EduNavTab
import com.example.educloud.ui.components.StorybookPage

/** Learning path node state */
enum class PathNodeState { Completed, InProgress, Locked }

data class PathItem(
    val title: String,
    val state: PathNodeState,
)

/**
 * LearningPathScreen — adapts Learning_Path_4.html.
 *
 * Shows a vertical lesson path with colour-coded connector lines and node states
 * (completed → green check, in-progress → orange play, locked → grey lock).
 */
@Composable
fun LearningPathScreen(
    onBack: () -> Unit,
    onNavigateTab: (EduNavTab) -> Unit,
    items: List<PathItem> = samplePathItems,
) {
    Scaffold(
        bottomBar = {
            EduBottomNav(
                activeTab = EduNavTab.Progress,
                onTabSelected = onNavigateTab,
            )
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
            ) {
                // ── Top app bar ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Back",
                            modifier = Modifier.size(20.dp), tint = EduCloudLeaf)
                    }
                    Text(
                        text = "Learning path",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EduCloudInk,
                    )
                    // Spacer for balance
                    Spacer(Modifier.width(48.dp))
                }

                // ── Path items ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items.forEachIndexed { index, item ->
                        PathItemRow(
                            item = item,
                            showConnector = index < items.lastIndex,
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // "See all topics" pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SecondaryFixed)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "See all topics",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PathItemRow(item: PathItem, showConnector: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // ── Node + connector column ────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val (nodeBg, nodeIcon, nodeIconTint) = when (item.state) {
                PathNodeState.Completed  -> Triple(PrimaryContainer, Icons.Filled.Check, Color.White)
                PathNodeState.InProgress -> Triple(SecondaryContainer, Icons.Filled.PlayArrow, Color.White)
                PathNodeState.Locked     -> Triple(SurfaceContainerHighest, Icons.Filled.Lock, EduCloudMutedInk)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(nodeBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(nodeIcon, contentDescription = null, tint = nodeIconTint, modifier = Modifier.size(22.dp))
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(
                            if (item.state == PathNodeState.Completed) PrimaryContainer
                            else SurfaceContainerHighest,
                        ),
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // ── Text content ───────────────────────────────────────────────────
        val alpha = if (item.state == PathNodeState.Locked) 0.55f else 1f
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.state == PathNodeState.InProgress) FontWeight.Bold else FontWeight.Normal,
                color = EduCloudInk.copy(alpha = alpha),
            )
            val stateLabel = when (item.state) {
                PathNodeState.Completed  -> "Completed"
                PathNodeState.InProgress -> "In progress"
                PathNodeState.Locked     -> "Locked"
            }
            val stateLabelColor = when (item.state) {
                PathNodeState.Completed  -> EduCloudLeaf
                PathNodeState.InProgress -> OnSecondaryContainer
                PathNodeState.Locked     -> EduCloudMutedInk.copy(alpha = 0.55f)
            }
            Text(
                text = stateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = stateLabelColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val samplePathItems = listOf(
    PathItem("Equal shares",          PathNodeState.Completed),
    PathItem("Add within 100",        PathNodeState.Completed),
    PathItem("Groups & multiplication", PathNodeState.InProgress),
    PathItem("Fractions",             PathNodeState.Locked),
    PathItem("Geometry",              PathNodeState.Locked),
)

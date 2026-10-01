package com.example.educloud.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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

@Composable
internal fun LearningPathSection(
    items: List<PathItem>,
    onItemClick: (PathItem) -> Unit,
    onSeeAllTopics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            PathItemRow(
                item = item,
                showConnector = index < items.lastIndex,
                onClick = { onItemClick(item) },
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SecondaryFixed)
                    .clickable(onClick = onSeeAllTopics)
                    .semantics {
                        role = Role.Button
                        contentDescription = "See all Grade 3 Maths topics"
                    }
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
    }
}

@Composable
internal fun PathItemRow(item: PathItem, showConnector: Boolean, onClick: () -> Unit = {}) {
    val stateLabel = when (item.state) {
        PathNodeState.Completed -> "Completed"
        PathNodeState.InProgress -> if (item.kind == PathKind.Quiz) "Today's check" else "Up next"
        PathNodeState.Locked -> "Later"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${item.title}, $stateLabel"
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val (nodeBg, nodeIcon, nodeIconTint) = when (item.state) {
                PathNodeState.Completed -> Triple(PrimaryContainer, Icons.Filled.Check, Color.White)
                PathNodeState.InProgress -> Triple(SecondaryContainer, Icons.Filled.PlayArrow, Color.White)
                PathNodeState.Locked -> Triple(SurfaceContainerHighest, Icons.Filled.Lock, EduCloudMutedInk)
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
            val stateLabelColor = when (item.state) {
                PathNodeState.Completed -> EduCloudLeaf
                PathNodeState.InProgress -> OnSecondaryContainer
                PathNodeState.Locked -> EduCloudMutedInk.copy(alpha = 0.55f)
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

package com.example.educloud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.educloud.sync.ANALOGY_DOMAINS
import com.example.educloud.sync.INTEREST_CHIP_LABELS
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.PrimaryFixed
import com.example.educloud.theme.SurfaceContainerLow

@Composable
fun InterestChipGrid(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    domains: List<String> = ANALOGY_DOMAINS,
) {
    Column {
        domains.chunked(2).forEach { rowDomains ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowDomains.forEach { domain ->
                    InterestChip(
                        label = INTEREST_CHIP_LABELS[domain] ?: domain,
                        selected = domain in selected,
                        onClick = { onToggle(domain) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowDomains.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.size(10.dp))
        }
    }
}

@Composable
fun InterestChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) PrimaryFixed.copy(.5f) else SurfaceContainerLow
    val border = if (selected) EduCloudLeaf.copy(.4f) else Color(0xFFDBDAD7)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) EduCloudLeaf else EduCloudInk,
        )
        if (selected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = EduCloudLeaf,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

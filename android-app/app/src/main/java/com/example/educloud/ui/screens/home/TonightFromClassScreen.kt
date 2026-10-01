package com.example.educloud.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educloud.sync.DEMO_HOMEWORK_CLASS_CODE
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudSurface
import com.example.educloud.ui.components.AmbientCard
import com.example.educloud.ui.components.StorybookPage
import com.example.educloud.ui.components.TactileButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonightFromClassScreen(
    viewModel: TonightFromClassViewModel,
    onBack: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTutor: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Tonight from class") },
            )
        },
    ) { innerPadding ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 20) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Contract prototype. Not Athena production. Not facial analysis.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduCloudOrange,
                        )
                        Text(
                            "Enter the class code from the teacher page, or use the bundled Grade 3 regrouping assignment. No names or cameras.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EduCloudMutedInk,
                        )
                    }
                }

                OutlinedTextField(
                    value = state.classCode,
                    onValueChange = viewModel::onClassCodeChange,
                    label = { Text("Class code") },
                    placeholder = { Text(DEMO_HOMEWORK_CLASS_CODE) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                TactileButton(
                    text = if (state.isLoading) "Opening…" else "Open class code",
                    onClick = { if (!state.isLoading) viewModel.openClassCode() },
                    containerColor = EduCloudLeaf,
                )
                TactileButton(
                    text = "Use demo assignment",
                    onClick = { if (!state.isLoading) viewModel.useDemoFixture() },
                    containerColor = EduCloudInk,
                )

                state.error?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = EduCloudOrange)
                }

                state.pack?.let { pack ->
                    AmbientCard(modifier = Modifier.fillMaxWidth(), containerColor = EduCloudSurface, cornerRadius = 20) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Tonight’s skill", style = MaterialTheme.typography.labelLarge, color = EduCloudMutedInk)
                            Text(
                                pack.label ?: "Grade 3 regrouping",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduCloudInk,
                            )
                            Text("Code ${pack.classCode}", style = MaterialTheme.typography.bodySmall, color = EduCloudMutedInk)
                            if (state.isBundledSample) {
                                Text(
                                    "This is a practice sample that came with the app. It is not tonight's work from your class.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EduCloudOrange,
                                )
                            }
                        }
                    }
                    TactileButton(
                        text = "Do the quiz",
                        onClick = onOpenQuiz,
                        containerColor = EduCloudLeaf,
                    )
                    TactileButton(
                        text = "Ask the tutor · Explain it my way",
                        onClick = onOpenTutor,
                        containerColor = EduCloudOrange,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

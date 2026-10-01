package com.example.educloud.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educloud.sync.HOMEWORK_TUTOR_PROMPT
import com.example.educloud.sync.INTEREST_CHIP_LABELS
import com.example.educloud.theme.EduCloudInk
import com.example.educloud.theme.EduCloudLake
import com.example.educloud.theme.EduCloudLeaf
import com.example.educloud.theme.EduCloudLine
import com.example.educloud.theme.EduCloudMutedInk
import com.example.educloud.theme.EduCloudOrange
import com.example.educloud.theme.EduCloudSurface
import com.example.educloud.theme.SurfaceContainerLow
import com.example.educloud.ui.components.MessageBubble
import com.example.educloud.ui.components.StorybookPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    subject: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    fromClass: Boolean = false,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(subject, fromClass) {
        viewModel.init(
            subject,
            openingPrompt = if (fromClass) HOMEWORK_TUTOR_PROMPT else "",
        )
    }

    // Auto-scroll to latest message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val subjectLabel = when (subject) {
        "math" -> "Tutor · Maths"
        else -> "Tutor · Grade 3"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EduCloudLeaf)
                    }
                },
                title = {
                    Text(
                        text = subjectLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = EduCloudLeaf
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (state.isOpenAiConnected) com.example.educloud.ui.components.DuoGreen.copy(alpha = 0.15f) else Color(0xFFE9E8E5))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (state.isOpenAiConnected) "Cloud re-explain on" else "Works offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isOpenAiConnected) com.example.educloud.ui.components.DuoGreenDark else EduCloudMutedInk
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduCloudSurface),
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(EduCloudSurface)) {
                
                // OOM warning banner
                if (state.isOomMode || state.forceRetrievalOnly) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFDAD6))) {
                        Text(
                            text = "Low-memory mode: Teaching directly from local source excerpts.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93000A),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }

                Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Input bar
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = viewModel::onInputChange,
                                placeholder = { Text("Ask the tutor...", color = EduCloudMutedInk.copy(.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 4,
                                isError = state.inputError != null,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    keyboard?.hide()
                                    viewModel.sendMessage()
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EduCloudLeaf,
                                    unfocusedBorderColor = EduCloudLine,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = EduCloudInk,
                                    unfocusedTextColor = EduCloudInk,
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (state.inputText.isNotBlank() && !state.isThinking) EduCloudOrange else Color(0xFFE9E8E5))
                                    .clickable(enabled = state.inputText.isNotBlank() && !state.isThinking) {
                                        keyboard?.hide()
                                        viewModel.sendMessage()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (state.inputText.isNotBlank() && !state.isThinking) Color.White else Color(0xFFC2C9B8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        state.inputError?.let { inputError ->
                            Text(
                                text = inputError,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                color = Color(0xFFBA1A1A),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        },
        containerColor = EduCloudSurface
    ) { paddingValues ->
        StorybookPage {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Proof reference bar (Stitch prompt requirement)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EduCloudLake.copy(.08f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.LibraryBooks, null, tint = EduCloudLake, modifier = Modifier.size(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val navText = listOf("Local rule pack", "Grade 3 Maths", "Source shown in each answer")
                            navText.forEachIndexed { index, text ->
                                Text(text, style = MaterialTheme.typography.labelSmall, color = EduCloudLake)
                                if (index < navText.size - 1) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = EduCloudLake.copy(.5f), modifier = Modifier.size(8.dp))
                                }
                            }
                        }
                    }
                }

                // Chat list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AssistChip(
                                    onClick = { viewModel.sendSuggestedMessage("Give me a Grade 3 Maths challenge") },
                                    label = { Text("🎲 Challenge") },
                                )
                                AssistChip(
                                    onClick = { viewModel.sendSuggestedMessage("Give me a hint for counting in twos") },
                                    label = { Text("💡 Hint") },
                                )
                                AssistChip(
                                    onClick = { viewModel.sendSuggestedMessage("Tell me a story about counting in twos") },
                                    label = { Text("✨ Story") },
                                )
                                AssistChip(
                                    onClick = viewModel::explainMyWay,
                                    enabled = state.canExplainMyWay && !state.isExplainingMyWay,
                                    label = { Text("🔁 Explain it my way") },
                                )
                            }
                            if (state.interestDomains.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    state.interestDomains.forEach { domain ->
                                        FilterChip(
                                            selected = domain == state.selectedAnalogyDomain,
                                            onClick = { viewModel.selectAnalogyDomain(domain) },
                                            label = { Text(INTEREST_CHIP_LABELS[domain] ?: domain) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    items(state.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            text = msg.text,
                            isFromUser = msg.isFromUser,
                            isLoading = msg.isStreaming && msg.text.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

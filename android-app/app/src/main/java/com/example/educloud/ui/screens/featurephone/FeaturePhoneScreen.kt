package com.example.educloud.ui.screens.featurephone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturePhoneScreen(viewModel: FeaturePhoneViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USSD simulator") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
        bottomBar = {
            UssdInputBar(state = state, viewModel = viewModel)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Simulation only — no phone number is collected and no real USSD or SMS is sent.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            UssdReply(state)
        }
    }
}

@Composable
private fun UssdReply(state: FeaturePhoneState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)) {
        Text(
            state.ussdReply,
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun UssdInputBar(state: FeaturePhoneState, viewModel: FeaturePhoneViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
    OutlinedTextField(
        value = state.ussdInput,
        onValueChange = viewModel::onUssdInput,
        label = { Text("Enter option: 1, 2, or 3") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { viewModel.submitUssd() }),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = viewModel::submitUssd, enabled = state.ussdInput.isNotBlank()) { Text("Send") }
        Button(onClick = viewModel::resetUssd) { Text("Restart") }
    }
    }
}

package com.buddy.ultra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buddy.ultra.ui.MainViewModel
import com.buddy.ultra.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var apiUrl by remember { mutableStateOf(viewModel.securityManager.getApiUrl()) }
    var apiToken by remember { mutableStateOf(viewModel.securityManager.getApiToken()) }
    var wakePhrase by remember { mutableStateOf(viewModel.securityManager.getWakeWord()) }

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = { Text("System Configuration", color = LightText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavySurface)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(20.dp)
        ) {
            Text("BUDDY AI CORE URL", color = ElectricBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it; viewModel.securityManager.setApiUrl(it) },
                label = { Text("Base URL", color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("X-BUDDY-TOKEN CREDENTIAL", color = ElectricBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it; viewModel.securityManager.setApiToken(it) },
                label = { Text("Security Token", color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("WAKE WORD TRIGGER", color = ElectricBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = wakePhrase,
                onValueChange = { wakePhrase = it; viewModel.securityManager.setWakeWord(it) },
                label = { Text("Phrase (e.g. hey buddy)", color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.checkBackendHealth() },
                colors = ButtonDefaults.buttonColors(containerColor = MidnightCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ping Buddy AI Core Health", color = ElectricBlue)
            }
        }
    }
}

package com.buddy.ultra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    onNavigateBack: () -> Unit,
    onOpenAccessibility: () -> Unit = {},
    onDisableBatteryOpt: () -> Unit = {}
) {
    var apiUrl by remember { mutableStateOf(viewModel.securityManager.getApiUrl()) }
    var apiToken by remember { mutableStateOf(viewModel.securityManager.getApiToken()) }
    var wakePhrase by remember { mutableStateOf(viewModel.securityManager.getWakeWord()) }

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = { Text("System & Background Engine", color = LightText) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text("PERMANENT BACKGROUND PERMISSIONS", color = NeonCyan, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenAccessibility,
                colors = ButtonDefaults.buttonColors(containerColor = MidnightCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. Enable Accessibility (Auto-Click WhatsApp)", color = ElectricBlue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDisableBatteryOpt,
                colors = ButtonDefaults.buttonColors(containerColor = MidnightCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("2. Disable Battery Optimization (24/7 Run)", color = EmeraldGreen)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("BUDDY AI CORE CLOUDFLARE URL", color = ElectricBlue, fontSize = 12.sp)
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

            Spacer(modifier = Modifier.height(14.dp))

            Text("SECURITY TOKEN", color = ElectricBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it; viewModel.securityManager.setApiToken(it) },
                label = { Text("X-Buddy-Token", color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("WAKE WORD", color = ElectricBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = wakePhrase,
                onValueChange = { wakePhrase = it; viewModel.securityManager.setWakeWord(it) },
                label = { Text("Activation Phrase", color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.checkBackendHealth() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ping Server Health", color = ElectricBlue)
            }
        }
    }
}

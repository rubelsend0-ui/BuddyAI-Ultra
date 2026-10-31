package com.buddy.ultra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buddy.ultra.ui.MainViewModel
import com.buddy.ultra.ui.components.JarvisOrb
import com.buddy.ultra.ui.theme.*
import com.buddy.ultra.voice.OrbState

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateSettings: () -> Unit
) {
    val orbState by viewModel.speechEngine.orbState.collectAsState()
    val isServerUp by viewModel.isServerUp.collectAsState()
    val history by viewModel.conversationHistory.collectAsState(initial = emptyList())
    var textInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ BUDDY AI ULTRA",
                    color = ElectricBlue,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isServerUp) EmeraldGreen else CoralRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServerUp) "CORE ONLINE" else "OFFLINE",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MutedText)
                    }
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Jarvis Animated Orb
            JarvisOrb(state = orbState) {
                viewModel.toggleVoiceOrb()
            }

            Text(
                text = when (orbState) {
                    OrbState.LISTENING -> "Listening to command..."
                    OrbState.THINKING, OrbState.PROCESSING -> "Synthesizing with Buddy AI Core..."
                    OrbState.SPEAKING -> "Responding & Relaying..."
                    OrbState.EXECUTING -> "Automating Device Action..."
                    OrbState.OFFLINE -> "Offline Mode Active"
                    OrbState.ERROR -> "Execution Exception Encountered"
                    OrbState.IDLE -> "Tap Orb or Say 'Hey Buddy'"
                },
                color = MutedText,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 14.dp)
            )

            // Conversation Log
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MidnightCard.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                reverseLayout = true
            ) {
                items(history) { item ->
                    val isUser = item.role == "USER"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) ElectricBlue else DarkNavySurface
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = item.text,
                                color = if (isUser) PureBlack else LightText,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Input & Submit Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Command: whatsapp open, call, volume...", color = MutedText) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkNavySurface,
                        unfocusedContainerColor = DarkNavySurface,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText,
                        cursorColor = ElectricBlue,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.submitCommand(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(ElectricBlue, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Execute", tint = PureBlack)
                }
            }
        }
    }
}

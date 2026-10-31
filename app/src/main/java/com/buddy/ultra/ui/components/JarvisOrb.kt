package com.buddy.ultra.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.buddy.ultra.ui.theme.*
import com.buddy.ultra.voice.OrbState

@Composable
fun JarvisOrb(
    state: OrbState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = when (state) {
            OrbState.LISTENING -> 1.35f
            OrbState.THINKING, OrbState.PROCESSING -> 1.15f
            OrbState.SPEAKING -> 1.25f
            OrbState.EXECUTING -> 1.4f
            else -> 1.05f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == OrbState.THINKING) 400 else 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val coreColor by animateColorAsState(
        targetValue = when (state) {
            OrbState.LISTENING -> NeonCyan
            OrbState.THINKING -> ElectricBlue
            OrbState.SPEAKING -> EmeraldGreen
            OrbState.EXECUTING -> Color(0xFFA855F7) // Purple
            OrbState.OFFLINE, OrbState.ERROR -> CoralRed
            else -> ElectricBlue.copy(alpha = 0.8f)
        },
        label = "color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .clickable { onClick() }
    ) {
        // Outer Glow Aura
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(listOf(coreColor.copy(alpha = 0.5f), Color.Transparent)),
                    shape = CircleShape
                )
                .blur(24.dp)
        )

        // Middle Geometric Ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(2.dp, coreColor.copy(alpha = 0.7f), CircleShape)
                .background(DarkNavySurface, CircleShape)
        )

        // Inner Core & Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .background(
                    brush = Brush.linearGradient(listOf(coreColor, MidnightCard)),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (state == OrbState.SPEAKING) Icons.Default.VolumeUp else Icons.Default.Mic,
                contentDescription = "Voice Assistant Orb",
                tint = PureBlack,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

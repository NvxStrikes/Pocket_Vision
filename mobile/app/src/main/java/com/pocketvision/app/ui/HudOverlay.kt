package com.pocketvision.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketvision.app.ui.theme.AccentCyan
import com.pocketvision.app.ui.theme.AccentGreen
import com.pocketvision.app.ui.theme.CardDark
import com.pocketvision.app.ui.theme.DarkBackground
import com.pocketvision.app.ui.theme.HudBorder
import com.pocketvision.app.ui.theme.OverlayScrim
import com.pocketvision.app.ui.theme.TextPrimary
import com.pocketvision.app.ui.theme.TextSecondary

@Composable
fun HudOverlay(
    isTorchOn: Boolean,
    isBackCamera: Boolean,
    onToggleTorch: () -> Unit,
    onSwitchCamera: () -> Unit,
    onSmartScanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Futuristic Corner Reticles in Center
        ViewfinderReticles(
            modifier = Modifier.fillMaxSize()
        )

        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title + Live Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkBackground.copy(alpha = 0.75f))
                    .border(1.dp, HudBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "POCKET VISION",
                    color = AccentCyan,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .alpha(livePulse)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    color = AccentGreen,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp
                )
            }

            // Quick Control Buttons (Torch & Camera Flip)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isBackCamera) {
                    IconButton(
                        onClick = onToggleTorch,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkBackground.copy(alpha = 0.75f))
                            .border(1.dp, if (isTorchOn) AccentCyan else HudBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isTorchOn) AccentCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkBackground.copy(alpha = 0.75f))
                        .border(1.dp, HudBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Controls: Smart Scan Button + Settings
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkBackground.copy(alpha = 0.75f))
                        .border(1.dp, HudBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Smart Scan Hero Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00B0FF),
                                    Color(0xFF00E5FF),
                                    Color(0xFF1DE9B6)
                                )
                            )
                        )
                        .clickable { onSmartScanClick() }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ SMART SCAN",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.5.sp
                    )
                }

                // Spacer for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun ViewfinderReticles(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val boxWidth = w * 0.72f
        val boxHeight = h * 0.45f
        val left = (w - boxWidth) / 2f
        val top = (h - boxHeight) / 2f
        val right = left + boxWidth
        val bottom = top + boxHeight
        val cornerLen = 28.dp.toPx()
        val strokeW = 2.dp.toPx()
        val color = Color(0x6600E5FF)

        // Top-Left Corner
        drawLine(color, Offset(left, top), Offset(left + cornerLen, top), strokeW)
        drawLine(color, Offset(left, top), Offset(left, top + cornerLen), strokeW)

        // Top-Right Corner
        drawLine(color, Offset(right, top), Offset(right - cornerLen, top), strokeW)
        drawLine(color, Offset(right, top), Offset(right, top + cornerLen), strokeW)

        // Bottom-Left Corner
        drawLine(color, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
        drawLine(color, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)

        // Bottom-Right Corner
        drawLine(color, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
        drawLine(color, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

        // Subtle center crosshair
        val cx = w / 2f
        val cy = h / 2f
        val chLen = 8.dp.toPx()
        drawLine(Color(0x3300E5FF), Offset(cx - chLen, cy), Offset(cx + chLen, cy), 1.5.dp.toPx())
        drawLine(Color(0x3300E5FF), Offset(cx, cy - chLen), Offset(cx, cy + chLen), 1.5.dp.toPx())
    }
}

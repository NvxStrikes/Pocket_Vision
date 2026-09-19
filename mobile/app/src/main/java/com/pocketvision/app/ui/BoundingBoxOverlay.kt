package com.pocketvision.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketvision.app.ui.theme.AccentAmber
import com.pocketvision.app.ui.theme.AccentCyan
import com.pocketvision.app.ui.theme.AccentGreen
import com.pocketvision.app.ui.theme.DarkBackground
import com.pocketvision.app.ui.theme.HudBorder
import com.pocketvision.app.vision.VisionFrameResult

@Composable
fun BoundingBoxOverlay(
    visionResult: VisionFrameResult,
    isBackCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Draw Bounding Boxes on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenW = size.width
            val screenH = size.height
            val imgW = if (visionResult.imageWidth > 0) visionResult.imageWidth.toFloat() else screenW
            val imgH = if (visionResult.imageHeight > 0) visionResult.imageHeight.toFloat() else screenH

            val scaleX = screenW / imgW
            val scaleY = screenH / imgH

            // 1. Draw Object Boxes
            for (obj in visionResult.objects) {
                val box = obj.boundingBox
                val left = if (isBackCamera) box.left * scaleX else (imgW - box.right) * scaleX
                val top = box.top * scaleY
                val right = if (isBackCamera) box.right * scaleX else (imgW - box.left) * scaleX
                val bottom = box.bottom * scaleY

                val strokeW = 2.5.dp.toPx()
                val cornerR = 12.dp.toPx()
                val boxColor = AccentCyan

                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(cornerR, cornerR),
                    style = Stroke(width = strokeW)
                )
            }

            // 2. Draw Face Boxes
            for (face in visionResult.faces) {
                val box = face.boundingBox ?: continue
                val left = if (isBackCamera) box.left * scaleX else (imgW - box.right) * scaleX
                val top = box.top * scaleY
                val right = if (isBackCamera) box.right * scaleX else (imgW - box.left) * scaleX
                val bottom = box.bottom * scaleY

                val strokeW = 2.dp.toPx()
                val cornerR = 16.dp.toPx()
                val boxColor = AccentGreen

                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(cornerR, cornerR),
                    style = Stroke(width = strokeW)
                )
            }
        }

        // Live Recognition HUD Badges (Floating Gestures & Expressions)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Prominent Detected Gesture Banner
            val activeGesture = visionResult.gestures.firstOrNull()
            AnimatedVisibility(
                visible = activeGesture != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeGesture != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkBackground.copy(alpha = 0.85f))
                            .border(1.5.dp, AccentAmber, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = activeGesture.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeGesture.gesture.uppercase()} ${(activeGesture.score * 100).toInt()}%",
                            color = AccentAmber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Detected Face Expression Banner
            val activeFace = visionResult.faces.firstOrNull()
            AnimatedVisibility(
                visible = activeFace != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeFace != null) {
                    val emoji = when (activeFace.expression) {
                        "Smiling" -> "😊"
                        "Surprised-looking" -> "😮"
                        "Frowning" -> "🙁"
                        else -> "😐"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkBackground.copy(alpha = 0.80f))
                            .border(1.dp, AccentGreen, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Expression: ${activeFace.expression}",
                            color = AccentGreen,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Left Detected Objects Roster
        if (visionResult.objects.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                visionResult.objects.take(4).forEach { obj ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground.copy(alpha = 0.80f))
                            .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = obj.label.uppercase(),
                            color = AccentCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(obj.score * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Bottom Right Real-time Stats Badge
        if (visionResult.fps > 0 || visionResult.inferenceTimeMs > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground.copy(alpha = 0.75f))
                    .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${visionResult.fps} FPS · ${visionResult.inferenceTimeMs}ms",
                    color = Color(0xFF90A4AE),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}

package com.pocketvision.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pocketvision.app.ui.theme.AccentCyan
import com.pocketvision.app.ui.theme.HudBorder
import com.pocketvision.app.ui.theme.TextPrimary
import com.pocketvision.app.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    confidenceThreshold: Float,
    onConfidenceChange: (Float) -> Unit,
    speechEnabled: Boolean,
    onSpeechToggle: (Boolean) -> Unit,
    backendUrl: String,
    onBackendUrlChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF101720),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HudBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        color = AccentCyan,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Confidence Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Confidence Threshold",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(confidenceThreshold * 100).toInt()}%",
                            color = AccentCyan,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = confidenceThreshold,
                        onValueChange = onConfidenceChange,
                        valueRange = 0.30f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan
                        )
                    )
                }

                // Speech Results Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Speak Results (TTS)",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Announce detected objects & gestures",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Switch(
                        checked = speechEnabled,
                        onCheckedChange = onSpeechToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AccentCyan
                        )
                    )
                }

                // Backend Server URL
                Column {
                    Text(
                        text = "Smart Scan Backend URL",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = backendUrl,
                        onValueChange = onBackendUrlChange,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = HudBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

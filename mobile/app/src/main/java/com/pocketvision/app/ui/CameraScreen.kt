package com.pocketvision.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pocketvision.app.camera.CameraManager
import com.pocketvision.app.camera.CameraPreview
import com.pocketvision.app.network.SmartScanClient
import com.pocketvision.app.speech.SpeechAnnouncer
import com.pocketvision.app.ui.theme.AccentCyan
import com.pocketvision.app.ui.theme.DarkBackground
import com.pocketvision.app.ui.theme.HudBorder
import com.pocketvision.app.ui.theme.TextSecondary
import com.pocketvision.app.vision.SmartScanResult
import com.pocketvision.app.vision.VisionCoordinator
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for Pocket Vision", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        val cameraManager = remember { CameraManager(context) }
        val visionCoordinator = remember { VisionCoordinator(context) }
        val smartScanClient = remember { SmartScanClient() }
        val speechAnnouncer = remember { SpeechAnnouncer(context) }

        var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
        val isTorchOn by cameraManager.isTorchOn.collectAsState()
        val isBackCamera by cameraManager.isBackCamera.collectAsState()
        val visionResult by visionCoordinator.visionResult.collectAsState()

        // Smart Scan Dialog State
        var isScanning by remember { mutableStateOf(false) }
        var smartScanResult by remember { mutableStateOf<SmartScanResult?>(null) }

        // Settings Dialog State
        var showSettings by remember { mutableStateOf(false) }
        var confidenceThreshold by remember { mutableFloatStateOf(0.45f) }
        var speechEnabled by remember { mutableStateOf(false) }
        var backendUrl by remember { mutableStateOf("http://10.0.2.2:8000") }

        DisposableEffect(lifecycleOwner) {
            onDispose {
                cameraManager.shutdown()
                visionCoordinator.close()
                speechAnnouncer.shutdown()
            }
        }

        // Trigger TTS announcements when enabled
        LaunchedEffect(visionResult) {
            if (speechEnabled) {
                val gesture = visionResult.gestures.firstOrNull()
                if (gesture != null) {
                    speechAnnouncer.speak("${gesture.gesture} gesture detected")
                } else {
                    val topObj = visionResult.objects.firstOrNull()
                    if (topObj != null) {
                        speechAnnouncer.speak(topObj.label)
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Full-screen Camera Feed
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onPreviewViewCreated = { view ->
                    previewViewRef = view
                    cameraManager.startCamera(
                        lifecycleOwner = lifecycleOwner,
                        previewView = view,
                        onFrame = { bitmap ->
                            visionCoordinator.processFrame(bitmap)
                        }
                    )
                }
            )

            // Dynamic Bounding Boxes, Gestures & Expression Tags
            BoundingBoxOverlay(
                visionResult = visionResult,
                isBackCamera = isBackCamera,
                modifier = Modifier.fillMaxSize()
            )

            // Sci-fi HUD Overlay Controls
            HudOverlay(
                isTorchOn = isTorchOn,
                isBackCamera = isBackCamera,
                onToggleTorch = { cameraManager.toggleTorch() },
                onSwitchCamera = {
                    previewViewRef?.let { view ->
                        cameraManager.switchCamera(lifecycleOwner, view) { bitmap ->
                            visionCoordinator.processFrame(bitmap)
                        }
                    }
                },
                onSmartScanClick = {
                    val frame = cameraManager.latestBitmap
                    if (frame != null) {
                        isScanning = true
                        coroutineScope.launch {
                            val res = smartScanClient.analyzeFrame(
                                bitmap = frame,
                                backendUrl = backendUrl,
                                latestLocalResult = visionResult
                            )
                            smartScanResult = res
                            isScanning = false
                        }
                    } else {
                        Toast.makeText(context, "Capturing frame...", Toast.LENGTH_SHORT).show()
                    }
                },
                onSettingsClick = { showSettings = true }
            )

            // Smart Scan Results Modal
            if (isScanning || smartScanResult != null) {
                SmartScanDialog(
                    isLoading = isScanning,
                    result = smartScanResult,
                    onDismiss = {
                        isScanning = false
                        smartScanResult = null
                    }
                )
            }

            // Settings Modal
            if (showSettings) {
                SettingsDialog(
                    confidenceThreshold = confidenceThreshold,
                    onConfidenceChange = {
                        confidenceThreshold = it
                        visionCoordinator.updateConfidence(it)
                    },
                    speechEnabled = speechEnabled,
                    onSpeechToggle = {
                        speechEnabled = it
                        speechAnnouncer.isEnabled = it
                    },
                    backendUrl = backendUrl,
                    onBackendUrlChange = { backendUrl = it },
                    onDismiss = { showSettings = false }
                )
            }
        }
    } else {
        // Fallback Camera Permission Prompt
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131922))
                    .border(1.dp, HudBorder, RoundedCornerShape(16.dp))
                    .padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CAMERA ACCESS NEEDED",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pocket Vision uses your camera in real time to recognize objects, gestures, and expressions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("ALLOW CAMERA", color = Color.Black)
                }
            }
        }
    }
}

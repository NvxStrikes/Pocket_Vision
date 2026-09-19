package com.pocketvision.app.vision

import android.graphics.RectF

data class DetectedObject(
    val label: String,
    val score: Float,
    val boundingBox: RectF
)

data class DetectedGesture(
    val gesture: String,
    val score: Float,
    val emoji: String
)

data class DetectedFace(
    val expression: String,
    val score: Float,
    val boundingBox: RectF? = null
)

data class VisionFrameResult(
    val objects: List<DetectedObject> = emptyList(),
    val gestures: List<DetectedGesture> = emptyList(),
    val faces: List<DetectedFace> = emptyList(),
    val fps: Int = 0,
    val inferenceTimeMs: Long = 0L,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)

data class SmartScanResult(
    val primarySubject: String,
    val specificType: String,
    val confidence: Float,
    val description: String,
    val additionalObjects: List<String>
)

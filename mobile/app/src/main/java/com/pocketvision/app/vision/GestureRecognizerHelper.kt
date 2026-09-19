package com.pocketvision.app.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import kotlin.math.hypot

class GestureRecognizerHelper(
    private val context: Context,
    var minConfidence: Float = 0.50f
) {
    private var gestureRecognizer: GestureRecognizer? = null

    init {
        setupRecognizer()
    }

    private fun setupRecognizer() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("gesture_recognizer.task")
                .build()

            val options = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMinHandDetectionConfidence(minConfidence)
                .setMinHandPresenceConfidence(minConfidence)
                .setMinTrackingConfidence(minConfidence)
                .setNumHands(2)
                .build()

            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun recognize(bitmap: Bitmap): List<DetectedGesture> {
        val recognizer = gestureRecognizer ?: return emptyList()
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = recognizer.recognize(mpImage)

            val gesturesList = mutableListOf<DetectedGesture>()

            result.gestures().forEachIndexed { handIndex, categories ->
                val topCategory = categories.firstOrNull()
                val landmarks = result.landmarks().getOrNull(handIndex)

                // First check custom landmark geometry rules
                val customGesture = landmarks?.let { evaluateCustomLandmarks(it) }

                if (customGesture != null) {
                    gesturesList.add(customGesture)
                } else if (topCategory != null && topCategory.score() >= minConfidence && topCategory.categoryName() != "None") {
                    val (friendlyName, emoji) = mapBuiltInGesture(topCategory.categoryName())
                    gesturesList.add(
                        DetectedGesture(
                            gesture = friendlyName,
                            score = topCategory.score(),
                            emoji = emoji
                        )
                    )
                }
            }
            gesturesList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun mapBuiltInGesture(categoryName: String): Pair<String, String> {
        return when (categoryName) {
            "Thumb_Up" -> "Thumbs Up" to "👍"
            "Thumb_Down" -> "Thumbs Down" to "👎"
            "Victory" -> "Peace / Victory" to "✌️"
            "Open_Palm" -> "Open Palm" to "✋"
            "Closed_Fist" -> "Closed Fist" to "✊"
            "Pointing_Up" -> "Pointing Up" to "☝️"
            "ILoveYou" -> "I Love You" to "🤟"
            else -> categoryName to "👋"
        }
    }

    private fun evaluateCustomLandmarks(landmarks: List<NormalizedLandmark>): DetectedGesture? {
        if (landmarks.size < 21) return null

        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        val indexPip = landmarks[6]
        val middleTip = landmarks[12]
        val middlePip = landmarks[10]
        val ringTip = landmarks[16]
        val ringPip = landmarks[14]
        val pinkyTip = landmarks[20]
        val pinkyPip = landmarks[18]
        val wrist = landmarks[0]

        // OK Sign: Thumb tip and Index tip touching/close, other three fingers extended
        val thumbIndexDist = distance(thumbTip, indexTip)
        val middleExtended = distance(middleTip, wrist) > distance(middlePip, wrist) * 1.15f
        val ringExtended = distance(ringTip, wrist) > distance(ringPip, wrist) * 1.15f
        val pinkyExtended = distance(pinkyTip, wrist) > distance(pinkyPip, wrist) * 1.15f

        if (thumbIndexDist < 0.065f && middleExtended && ringExtended && pinkyExtended) {
            return DetectedGesture("OK Sign", 0.90f, "👌")
        }

        // Rock / Horns: Index and Pinky extended, Middle and Ring curled
        val indexExtended = distance(indexTip, wrist) > distance(indexPip, wrist) * 1.15f
        val middleCurled = distance(middleTip, wrist) < distance(middlePip, wrist)
        val ringCurled = distance(ringTip, wrist) < distance(ringPip, wrist)

        if (indexExtended && pinkyExtended && middleCurled && ringCurled) {
            return DetectedGesture("Rock / Horns", 0.92f, "🤘")
        }

        return null
    }

    private fun distance(a: NormalizedLandmark, b: NormalizedLandmark): Float =
        hypot(a.x() - b.x(), a.y() - b.y())

    fun close() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }
}

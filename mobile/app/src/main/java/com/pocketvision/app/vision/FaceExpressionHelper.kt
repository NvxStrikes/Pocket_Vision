package com.pocketvision.app.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker

class FaceExpressionHelper(
    private val context: Context,
    var minConfidence: Float = 0.50f
) {
    private var faceLandmarker: FaceLandmarker? = null

    init {
        setupLandmarker()
    }

    private fun setupLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMinFaceDetectionConfidence(minConfidence)
                .setMinFacePresenceConfidence(minConfidence)
                .setMinTrackingConfidence(minConfidence)
                .setOutputFaceBlendshapes(true)
                .setNumFaces(2)
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<DetectedFace> {
        val landmarker = faceLandmarker ?: return emptyList()
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detect(mpImage)
            val facesList = mutableListOf<DetectedFace>()

            val blendshapesList = result.faceBlendshapes()
            val allLandmarks = result.faceLandmarks()

            blendshapesList.ifPresent { blendshapesGroup ->
                blendshapesGroup.forEachIndexed { index, blendshapeList ->
                    val blendshapeMap = blendshapeList.associate { it.categoryName() to it.score() }

                    val smileLeft = blendshapeMap["mouthSmileLeft"] ?: 0f
                    val smileRight = blendshapeMap["mouthSmileRight"] ?: 0f
                    val smileAvg = (smileLeft + smileRight) / 2f

                    val jawOpen = blendshapeMap["jawOpen"] ?: 0f

                    val browDownLeft = blendshapeMap["browDownLeft"] ?: 0f
                    val browDownRight = blendshapeMap["browDownRight"] ?: 0f
                    val browDownAvg = (browDownLeft + browDownRight) / 2f

                    val (expression, confidence) = when {
                        smileAvg > 0.40f -> "Smiling" to smileAvg
                        jawOpen > 0.40f -> "Surprised-looking" to jawOpen
                        browDownAvg > 0.40f -> "Frowning" to browDownAvg
                        else -> "Neutral" to 0.85f
                    }

                    // Compute bounding box from landmarks if available
                    val faceBox = allLandmarks.getOrNull(index)?.let { landmarks ->
                        var minX = Float.MAX_VALUE
                        var minY = Float.MAX_VALUE
                        var maxX = Float.MIN_VALUE
                        var maxY = Float.MIN_VALUE
                        for (lm in landmarks) {
                            if (lm.x() < minX) minX = lm.x()
                            if (lm.y() < minY) minY = lm.y()
                            if (lm.x() > maxX) maxX = lm.x()
                            if (lm.y() > maxY) maxY = lm.y()
                        }
                        RectF(minX * bitmap.width, minY * bitmap.height, maxX * bitmap.width, maxY * bitmap.height)
                    }

                    facesList.add(DetectedFace(expression, confidence, faceBox))
                }
            }

            facesList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun close() {
        faceLandmarker?.close()
        faceLandmarker = null
    }
}

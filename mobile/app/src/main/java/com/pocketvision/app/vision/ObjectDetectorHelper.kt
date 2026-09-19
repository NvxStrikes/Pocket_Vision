package com.pocketvision.app.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector

class ObjectDetectorHelper(
    private val context: Context,
    var minConfidence: Float = 0.45f
) {
    private var objectDetector: ObjectDetector? = null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("efficientdet_lite0.tflite")
                .build()

            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMaxResults(6)
                .setScoreThreshold(minConfidence)
                .build()

            objectDetector = ObjectDetector.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<DetectedObject> {
        val detector = objectDetector ?: return emptyList()
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = detector.detect(mpImage)
            result.detections().mapNotNull { detection ->
                val category = detection.categories().firstOrNull() ?: return@mapNotNull null
                val box = detection.boundingBox()
                val rectF = RectF(box.left, box.top, box.right, box.bottom)
                DetectedObject(
                    label = category.categoryName().capitalizeWords(),
                    score = category.score(),
                    boundingBox = rectF
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}

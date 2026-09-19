package com.pocketvision.app.vision

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisionCoordinator(context: Context) {

    private val objectHelper = ObjectDetectorHelper(context)
    private val gestureHelper = GestureRecognizerHelper(context)
    private val faceHelper = FaceExpressionHelper(context)
    private val smoother = TemporalSmoother()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _visionResult = MutableStateFlow(VisionFrameResult())
    val visionResult: StateFlow<VisionFrameResult> = _visionResult

    @Volatile
    private var isBusy = false

    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var currentFps = 0

    fun updateConfidence(threshold: Float) {
        objectHelper.minConfidence = threshold
        gestureHelper.minConfidence = threshold
        faceHelper.minConfidence = threshold
    }

    fun processFrame(bitmap: Bitmap) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            val startTime = System.currentTimeMillis()
            try {
                // Interleave or combine detection pipelines
                val objects = smoother.smooth(objectHelper.detect(bitmap))
                val gestures = gestureHelper.recognize(bitmap)
                val faces = faceHelper.detect(bitmap)

                frameCount++
                val now = System.currentTimeMillis()
                if (now - lastFpsTimestamp >= 1000) {
                    currentFps = frameCount
                    frameCount = 0
                    lastFpsTimestamp = now
                }

                val inferenceMs = now - startTime

                val result = VisionFrameResult(
                    objects = objects,
                    gestures = gestures,
                    faces = faces,
                    fps = currentFps,
                    inferenceTimeMs = inferenceMs,
                    imageWidth = bitmap.width,
                    imageHeight = bitmap.height
                )

                withContext(Dispatchers.Main) {
                    _visionResult.value = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isBusy = false
            }
        }
    }

    fun close() {
        objectHelper.close()
        gestureHelper.close()
        faceHelper.close()
    }
}

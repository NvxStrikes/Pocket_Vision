package com.pocketvision.app.vision

import android.graphics.RectF

class TemporalSmoother(
    private val alpha: Float = 0.6f,
    private val historyLength: Int = 4
) {
    private val objectHistory = mutableMapOf<String, SmoothState>()

    private data class SmoothState(
        var box: RectF,
        var hits: Int,
        var lastSeenFrame: Long
    )

    private var currentFrameId = 0L

    fun smooth(detectedObjects: List<DetectedObject>): List<DetectedObject> {
        currentFrameId++
        val smoothed = mutableListOf<DetectedObject>()

        for (obj in detectedObjects) {
            val key = obj.label
            val existing = objectHistory[key]

            if (existing != null) {
                val smoothedBox = RectF(
                    alpha * obj.boundingBox.left + (1 - alpha) * existing.box.left,
                    alpha * obj.boundingBox.top + (1 - alpha) * existing.box.top,
                    alpha * obj.boundingBox.right + (1 - alpha) * existing.box.right,
                    alpha * obj.boundingBox.bottom + (1 - alpha) * existing.box.bottom
                )
                existing.box = smoothedBox
                existing.hits = minOf(existing.hits + 1, historyLength)
                existing.lastSeenFrame = currentFrameId

                smoothed.add(obj.copy(boundingBox = smoothedBox))
            } else {
                objectHistory[key] = SmoothState(
                    box = RectF(obj.boundingBox),
                    hits = 1,
                    lastSeenFrame = currentFrameId
                )
                smoothed.add(obj)
            }
        }

        // Clean up objects not seen in last 3 frames
        val iterator = objectHistory.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (currentFrameId - entry.value.lastSeenFrame > 3) {
                iterator.remove()
            }
        }

        return smoothed
    }
}

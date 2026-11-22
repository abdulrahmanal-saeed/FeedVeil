package com.abdulrahmanalsaeed.feedveil.detection

import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver

/** Ephemeral, privacy-reduced structural metadata. Never retain Facebook text or identifiers. */
data class ScreenRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
    val area: Long get() = width.toLong() * height
    fun contains(other: ScreenRect, tolerance: Int = 4): Boolean =
        left - tolerance <= other.left && top - tolerance <= other.top &&
            right + tolerance >= other.right && bottom + tolerance >= other.bottom

    fun intersection(other: ScreenRect): Long =
        (minOf(right, other.right) - maxOf(left, other.left)).coerceAtLeast(0).toLong() *
            (minOf(bottom, other.bottom) - maxOf(top, other.top)).coerceAtLeast(0)

    fun nearSamePlacement(other: ScreenRect): Boolean {
        if (area == 0L || other.area == 0L) return false
        val common = intersection(other).toDouble()
        return common / minOf(area, other.area) >= 0.82 &&
            common / maxOf(area, other.area) >= 0.72
    }
}

enum class SurfaceHint { FEED, REELS, UNKNOWN }

/**
 * A selected-tab hint is not proof of surface identity (fullscreen videos may be UNKNOWN).
 * Values come from exact UI semantics and are never user text in storage.
 */
data class UiNodeFeature(
    val id: Int,
    val parentId: Int?,
    val depth: Int,
    val rect: ScreenRect,
    val marker: AdLabelObserver.Kind,
    val hasOtherReadableLabel: Boolean,
    val selectedSurfaceHint: SurfaceHint = SurfaceHint.UNKNOWN
)

enum class CandidateConfidence { PROVISIONAL, UNRESOLVED }

data class AdCandidate(
    val containerId: Int?,
    val marker: AdLabelObserver.Kind,
    val rect: ScreenRect?,
    val confidence: CandidateConfidence,
    val surfaceHint: SurfaceHint
)

data class CandidateScan(
    val surfaceHint: SurfaceHint,
    val candidates: List<AdCandidate>,
    val unresolvedMarkerNodes: Int
) {
    val provisionalCount: Int get() = candidates.count { it.confidence == CandidateConfidence.PROVISIONAL }
}

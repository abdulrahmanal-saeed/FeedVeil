package com.abdulrahmanalsaeed.feedveil.autoskip

import com.abdulrahmanalsaeed.feedveil.detection.CandidateConfidence
import com.abdulrahmanalsaeed.feedveil.detection.CandidateScan
import com.abdulrahmanalsaeed.feedveil.detection.ScreenRect
import com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint
import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode

/**
 * Experimental, user-armed policy, never a proof of advertisement identity.
 *
 * A marker must remain inside a plausible container at nearly the same placement
 * across two samples. UNKNOWN automatic surface cannot authorize a mode on its own:
 * the owner must select the Feed or Reels *test mode* explicitly.
 */
class AutoSkipPolicy {
    private var pendingRect: ScreenRect? = null
    private var pendingMarker: AdLabelObserver.Kind? = null
    private var firstObservedMs = 0L
    private var cooldownUntilMs = 0L
    private var needsEmptyFrame = false

    fun clear() {
        pendingRect = null
        pendingMarker = null
        firstObservedMs = 0L
        cooldownUntilMs = 0L
        needsEmptyFrame = false
    }

    fun afterCompletedSkip(nowMs: Long) {
        pendingRect = null
        pendingMarker = null
        firstObservedMs = 0L
        needsEmptyFrame = true // don't repeatedly skip a still-visible item
        cooldownUntilMs = nowMs + COOLDOWN_MS
    }

    fun maySkip(scan: CandidateScan, mode: ManualTestMode, viewport: ScreenRect, nowMs: Long): Boolean {
        if (mode == ManualTestMode.UNSPECIFIED || viewport.width < 200 || viewport.height < 400) {
            forgetCandidate()
            return false
        }
        if (nowMs < cooldownUntilMs) {
            forgetCandidate()
            return false
        }
        if ((mode == ManualTestMode.FEED && scan.surfaceHint == SurfaceHint.REELS) ||
            (mode == ManualTestMode.REELS && scan.surfaceHint == SurfaceHint.FEED)) {
            forgetCandidate()
            return false
        }

        val eligible = scan.candidates.filter {
            it.confidence == CandidateConfidence.PROVISIONAL &&
                markerAllowed(it.marker, mode) &&
                it.rect != null && safePlacement(it.rect, viewport, mode)
        }
        if (scan.unresolvedMarkerNodes > 0 || scan.candidates.size != 1 || eligible.size != 1) {
            if (scan.candidates.isEmpty()) needsEmptyFrame = false
            forgetCandidate()
            return false
        }
        if (needsEmptyFrame) return false

        val candidate = eligible.single()
        val bounds = candidate.rect ?: return false
        if (pendingMarker == candidate.marker && pendingRect?.nearSamePlacement(bounds) == true) {
            val gap = nowMs - firstObservedMs
            if (gap >= STABLE_MS && gap <= MAX_STABLE_GAP_MS) {
                forgetCandidate()
                return true
            }
            if (gap > MAX_STABLE_GAP_MS) {
                firstObservedMs = nowMs
                pendingRect = bounds
                pendingMarker = candidate.marker
            }
            return false
        }
        firstObservedMs = nowMs
        pendingRect = bounds
        pendingMarker = candidate.marker
        return false
    }

    private fun forgetCandidate() {
        pendingRect = null
        pendingMarker = null
        firstObservedMs = 0L
    }

    private fun markerAllowed(marker: AdLabelObserver.Kind, mode: ManualTestMode): Boolean =
        when (mode) {
            ManualTestMode.FEED -> marker == AdLabelObserver.Kind.AD_METADATA_SEGMENT ||
                marker == AdLabelObserver.Kind.SPONSORED_BADGE
            ManualTestMode.REELS -> marker == AdLabelObserver.Kind.AD_BADGE ||
                marker == AdLabelObserver.Kind.SPONSORED_BADGE
            ManualTestMode.UNSPECIFIED -> false
        }

    private fun safePlacement(r: ScreenRect, viewport: ScreenRect, mode: ManualTestMode): Boolean {
        if (r.area <= 0L || viewport.area <= 0L) return false
        // Full-screen Reels candidates can legitimately be taller than Feed cards.
        // Keep the policy below the Phase 1 container resolver's max area.
        val maxHeightRatio = if (mode == ManualTestMode.REELS) 0.96 else 0.83
        val maxAreaRatio = if (mode == ManualTestMode.REELS) 0.88 else 0.85
        if (r.width < viewport.width * 0.60 ||
            r.height < viewport.height * 0.12 ||
            r.height > viewport.height * maxHeightRatio ||
            r.area > viewport.area * maxAreaRatio) return false
        val overlap = r.intersection(viewport).toDouble()
        if (overlap / r.area < 0.85) return false
        val centerY = (r.top.toLong() + r.bottom.toLong()) / 2
        return centerY >= viewport.top + viewport.height * 0.20 &&
            centerY <= viewport.top + viewport.height * 0.85
    }

    companion object {
        private const val STABLE_MS = 1200L
        private const val MAX_STABLE_GAP_MS = 5000L
        const val COOLDOWN_MS = 15000L
    }
}

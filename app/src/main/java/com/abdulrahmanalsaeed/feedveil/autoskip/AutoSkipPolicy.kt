package com.abdulrahmanalsaeed.feedveil.autoskip

import com.abdulrahmanalsaeed.feedveil.detection.CandidateConfidence
import com.abdulrahmanalsaeed.feedveil.detection.CandidateScan
import com.abdulrahmanalsaeed.feedveil.detection.ScreenRect
import com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint
import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode

/**
 * Opt-in 3-gesture experiment ONLY. No candidate is a verified ad. Never allow
 * a label by itself to authorize an automatic action.
 *
 * lastReason contains a SYMBOLIC code, never Facebook user text.
 */
class AutoSkipPolicy {
    enum class Reason {
        WAITING, MODE_UNKNOWN, VIEWPORT_INVALID, COOLDOWN, SURFACE_MISMATCH,
        NO_BOUNDARY, UNRESOLVED_MARKER, MULTIPLE_CANDIDATES,
        BAD_LABEL_FOR_MODE, BOUNDS_REJECTED, AWAIT_DIFFERENT_ITEM,
        WAIT_STABLE, READY
    }

    var lastReason: Reason = Reason.WAITING
        private set

    private var pendingRect: ScreenRect? = null
    private var pendingMarker: AdLabelObserver.Kind? = null
    private var firstObservedMs = 0L
    private var cooldownUntilMs = 0L
    private var needsEmptyFrame = false

    fun clear() {
        forgetCandidate()
        cooldownUntilMs = 0L
        needsEmptyFrame = false
        lastReason = Reason.WAITING
    }

    fun afterCompletedSkip(nowMs: Long) {
        forgetCandidate()
        needsEmptyFrame = true
        cooldownUntilMs = nowMs + COOLDOWN_MS
        lastReason = Reason.COOLDOWN
    }

    fun maySkip(scan: CandidateScan, mode: ManualTestMode, viewport: ScreenRect, nowMs: Long): Boolean {
        if (mode == ManualTestMode.UNSPECIFIED) return refuse(Reason.MODE_UNKNOWN)
        if (viewport.width < 200 || viewport.height < 400) return refuse(Reason.VIEWPORT_INVALID)
        if (nowMs < cooldownUntilMs) return refuse(Reason.COOLDOWN)
        if ((mode == ManualTestMode.FEED && scan.surfaceHint == SurfaceHint.REELS) ||
            (mode == ManualTestMode.REELS && scan.surfaceHint == SurfaceHint.FEED)
        ) return refuse(Reason.SURFACE_MISMATCH)

        if (scan.candidates.isEmpty()) {
            needsEmptyFrame = false
            return refuse(if (scan.unresolvedMarkerNodes > 0)
                Reason.UNRESOLVED_MARKER else Reason.NO_BOUNDARY)
        }
        if (scan.unresolvedMarkerNodes > 0) return refuse(Reason.UNRESOLVED_MARKER)
        if (scan.candidates.size != 1) return refuse(Reason.MULTIPLE_CANDIDATES)

        val candidate = scan.candidates.single()
        if (candidate.confidence != CandidateConfidence.PROVISIONAL ||
            !markerAllowed(candidate.marker, mode)
        ) return refuse(Reason.BAD_LABEL_FOR_MODE)
        val r = candidate.rect
        if (r == null || !safePlacement(r, viewport, mode)) return refuse(Reason.BOUNDS_REJECTED)
        if (needsEmptyFrame) return refuse(Reason.AWAIT_DIFFERENT_ITEM)

        if (pendingMarker == candidate.marker && pendingRect?.nearSamePlacement(r) == true) {
            val gap = nowMs - firstObservedMs
            if (gap in STABLE_MS..MAX_STABLE_GAP_MS) {
                forgetCandidate()
                lastReason = Reason.READY
                return true
            }
            if (gap > MAX_STABLE_GAP_MS) {
                firstObservedMs = nowMs
                pendingRect = r
                pendingMarker = candidate.marker
            }
            lastReason = Reason.WAIT_STABLE
            return false
        }
        firstObservedMs = nowMs
        pendingRect = r
        pendingMarker = candidate.marker
        lastReason = Reason.WAIT_STABLE
        return false
    }

    private fun refuse(reason: Reason): Boolean {
        forgetCandidate()
        lastReason = reason
        return false
    }

    private fun forgetCandidate() {
        pendingRect = null
        pendingMarker = null
        firstObservedMs = 0L
    }

    private fun markerAllowed(kind: AdLabelObserver.Kind, mode: ManualTestMode): Boolean =
        when (mode) {
            ManualTestMode.FEED -> kind == AdLabelObserver.Kind.AD_METADATA_SEGMENT ||
                kind == AdLabelObserver.Kind.SPONSORED_BADGE
            ManualTestMode.REELS -> kind == AdLabelObserver.Kind.AD_BADGE ||
                kind == AdLabelObserver.Kind.SPONSORED_BADGE
            ManualTestMode.UNSPECIFIED -> false
        }

    private fun safePlacement(r: ScreenRect, viewport: ScreenRect, mode: ManualTestMode): Boolean {
        if (r.area <= 0L || viewport.area <= 0L) return false
        val maxHeightRatio = if (mode == ManualTestMode.REELS) 0.96 else 0.83
        val maxAreaRatio = if (mode == ManualTestMode.REELS) 0.88 else 0.85
        if (r.width < viewport.width * 0.60 ||
            r.height < viewport.height * 0.12 ||
            r.height > viewport.height * maxHeightRatio ||
            r.area > viewport.area * maxAreaRatio) return false
        if (r.intersection(viewport).toDouble() / r.area < 0.85) return false
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

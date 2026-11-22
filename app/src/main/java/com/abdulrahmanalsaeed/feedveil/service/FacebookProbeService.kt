package com.abdulrahmanalsaeed.feedveil.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.abdulrahmanalsaeed.feedveil.detection.FeedContainerResolver
import com.abdulrahmanalsaeed.feedveil.detection.RecentCandidateDeduper
import com.abdulrahmanalsaeed.feedveil.detection.ScreenRect
import com.abdulrahmanalsaeed.feedveil.detection.SurfaceHintResolver
import com.abdulrahmanalsaeed.feedveil.detection.UiNodeFeature
import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ProbeScope
import com.abdulrahmanalsaeed.feedveil.probe.ScreenObservation

/**
 * Phase 1: privacy-minimized, read-only screen STRUCTURE experiment.
 * No gestures, taps, masks, OCR, network or content persistence. A plausible container is
 * never equivalent to a verified ad and may NOT be used to authorize automatic actions.
 */
class FacebookProbeService : AccessibilityService() {
    private var lastScannedElapsedMs = 0L
    private var lastSessionRevision: Long? = null
    private val resolver = FeedContainerResolver()
    private val deduper = RecentCandidateDeduper()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !ProbeScope.mayRead(
                packageName = event.packageName?.toString(),
                consented = LocalProbeStore.isConsented(this),
                active = LocalProbeStore.isActive(this)
            )
        ) {
            deduper.clear()
            return
        }

        val root = rootInActiveWindow ?: return
        if (!ProbeScope.isTargetPackage(root.packageName?.toString())) return

        val revision = LocalProbeStore.currentSessionRevision(this)
        if (lastSessionRevision != revision) {
            deduper.clear()
            lastSessionRevision = revision
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastScannedElapsedMs < MIN_SCAN_INTERVAL_MS) return
        lastScannedElapsedMs = now

        // Never use the returned text for logging, persistence, telemetry or account identity.
        LocalProbeStore.recordEvent(this)
        val observation = inspectVisibleNodes(root, now)
        LocalProbeStore.recordScan(this, observation)
    }

    private data class Pending(
        val node: AccessibilityNodeInfo,
        val parentId: Int?,
        val depth: Int
    )

    private fun inspectVisibleNodes(root: AccessibilityNodeInfo, now: Long): ScreenObservation {
        val work = ArrayDeque<Pending>()
        work.addLast(Pending(root, null, 0))
        val features = ArrayList<UiNodeFeature>(100)
        var markers = 0
        var adBadges = 0
        var adMetadata = 0
        var textNodes = 0
        var descriptionNodes = 0

        while (work.isNotEmpty() && features.size < MAX_NODES) {
            val (node, parentId, depth) = work.removeFirst()
            if (!node.isVisibleToUser) continue

            // Read and immediately discard raw text; retain only minimal structural features.
            val text = node.text?.toString()
            val description = node.contentDescription?.toString()
            if (!text.isNullOrBlank()) textNodes++
            if (!description.isNullOrBlank()) descriptionNodes++
            val marker = AdLabelObserver.combine(
                AdLabelObserver.classify(text),
                AdLabelObserver.classify(description)
            )
            when (marker) {
                AdLabelObserver.Kind.SPONSORED_BADGE -> markers++
                AdLabelObserver.Kind.AD_BADGE -> adBadges++
                AdLabelObserver.Kind.AD_METADATA_SEGMENT -> adMetadata++
                AdLabelObserver.Kind.NONE -> Unit
            }

            val rect = Rect()
            node.getBoundsInScreen(rect)
            val id = features.size
            features.add(
                UiNodeFeature(
                    id = id,
                    parentId = parentId,
                    depth = depth,
                    rect = ScreenRect(rect.left, rect.top, rect.right, rect.bottom),
                    marker = marker,
                    hasOtherReadableLabel = marker == AdLabelObserver.Kind.NONE &&
                        (!text.isNullOrBlank() || !description.isNullOrBlank()),
                    selectedSurfaceHint = if (node.isSelected) {
                        val a = SurfaceHintResolver.selectedTabHint(text, true)
                        if (a != com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.UNKNOWN) a
                        else SurfaceHintResolver.selectedTabHint(description, true)
                    } else com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.UNKNOWN
                )
            )
            if (depth < MAX_DEPTH) {
                for (i in 0 until minOf(node.childCount, MAX_CHILDREN_PER_NODE)) {
                    node.getChild(i)?.let { work.addLast(Pending(it, id, depth + 1)) }
                }
            }
        }

        val rootRect = features.firstOrNull()?.rect ?: ScreenRect(0, 0, 0, 0)
        val analysis = resolver.analyze(features, rootRect)
        val dedupe = deduper.observe(analysis, now)

        return ScreenObservation(
            nodeCount = features.size,
            exactMarkerCount = markers,
            adBadgeCount = adBadges,
            adMetadataCount = adMetadata,
            textNodeCount = textNodes,
            descriptionNodeCount = descriptionNodes,
            provisionalContainers = analysis.provisionalCount,
            unresolvedMarkers = analysis.unresolvedMarkerNodes,
            placementClusters = dedupe.newPlacementClusters,
            repeatedPlacements = dedupe.repeatsSuppressed,
            surfaceHint = analysis.surfaceHint
        )
    }

    override fun onInterrupt() {
        deduper.clear()
    }

    override fun onDestroy() {
        deduper.clear()
        super.onDestroy()
    }

    companion object {
        private const val MIN_SCAN_INTERVAL_MS = 750L
        private const val MAX_NODES = 250
        private const val MAX_DEPTH = 14
        private const val MAX_CHILDREN_PER_NODE = 30
    }
}

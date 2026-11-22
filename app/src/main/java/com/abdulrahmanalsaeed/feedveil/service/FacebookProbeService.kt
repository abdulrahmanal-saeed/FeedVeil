package com.abdulrahmanalsaeed.feedveil.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ProbeScope
import com.abdulrahmanalsaeed.feedveil.probe.ScreenObservation
import com.abdulrahmanalsaeed.feedveil.probe.SponsoredLabelMatcher

/**
 * Phase 0 ONLY: passive, read-only and Facebook-only.
 *
 * This service intentionally contains NO click actions, gesture dispatch, scroll requests,
 * accessibility overlays, screenshots, network calls, or content persistence.
 */
class FacebookProbeService : AccessibilityService() {
    private var lastScannedElapsedMs = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!ProbeScope.mayRead(
                packageName = event.packageName?.toString(),
                consented = LocalProbeStore.isConsented(this),
                active = LocalProbeStore.isActive(this)
            )
        ) return

        // Do not consume events from non-Facebook roots or unrelated windows.
        val root = rootInActiveWindow ?: return
        if (!ProbeScope.isTargetPackage(root.packageName?.toString())) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastScannedElapsedMs < MIN_SCAN_INTERVAL_MS) return
        lastScannedElapsedMs = now

        LocalProbeStore.recordEvent(this)
        val observation = inspectVisibleNodes(root)
        LocalProbeStore.recordScan(this, observation)
    }

    private fun inspectVisibleNodes(root: AccessibilityNodeInfo): ScreenObservation {
        // Iterative, bounded walk: no raw content is written anywhere.
        val work = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        work.addLast(root to 0)
        var inspected = 0
        var markers = 0

        while (work.isNotEmpty() && inspected < MAX_NODES) {
            val (node, depth) = work.removeFirst()
            if (!node.isVisibleToUser) continue
            inspected += 1

            val exactMatch = SponsoredLabelMatcher.isExactMarker(node.text?.toString()) ||
                SponsoredLabelMatcher.isExactMarker(node.contentDescription?.toString())
            if (exactMatch) markers += 1

            if (depth < MAX_DEPTH) {
                for (i in 0 until minOf(node.childCount, MAX_CHILDREN_PER_NODE)) {
                    node.getChild(i)?.let { work.addLast(it to depth + 1) }
                }
            }
        }
        // The screen labels/children are not retained, logged, uploaded, or stored.
        return ScreenObservation(nodeCount = inspected, exactMarkerCount = markers)
    }

    override fun onInterrupt() {
        // Nothing to cancel: there are no gestures, overlays or background tasks in Phase 0.
    }

    companion object {
        private const val MIN_SCAN_INTERVAL_MS = 750L
        private const val MAX_NODES = 250
        private const val MAX_DEPTH = 14
        private const val MAX_CHILDREN_PER_NODE = 30
    }
}

package com.abdulrahmanalsaeed.feedveil.detection

import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver

/** In-memory short-window placement clusters, NOT unique ad or post identities. */
class RecentCandidateDeduper {
    private data class Entry(
        val rect: ScreenRect,
        val kind: AdLabelObserver.Kind,
        val surface: SurfaceHint,
        val lastSeenMs: Long
    )
    private val recent = ArrayDeque<Entry>()

    data class Delta(val newPlacementClusters: Int, val repeatsSuppressed: Int)

    fun observe(scan: CandidateScan, elapsedRealtimeMs: Long): Delta {
        while (recent.isNotEmpty() &&
            elapsedRealtimeMs - recent.first().lastSeenMs > WINDOW_MS
        ) recent.removeFirst()
        var fresh = 0
        var repeated = 0
        for (candidate in scan.candidates) {
            val r = candidate.rect ?: continue
            val match = recent.any {
                it.kind == candidate.marker && it.surface == candidate.surfaceHint &&
                    r.nearSamePlacement(it.rect)
            }
            if (match) {
                repeated++
            } else {
                fresh++
            }
            recent.addLast(Entry(r, candidate.marker, candidate.surfaceHint, elapsedRealtimeMs))
            while (recent.size > MAX_ENTRIES) recent.removeFirst()
        }
        return Delta(fresh, repeated)
    }

    fun clear() = recent.clear()

    companion object {
        private const val WINDOW_MS = 2500L
        private const val MAX_ENTRIES = 48
    }
}

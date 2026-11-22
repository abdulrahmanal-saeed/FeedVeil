package com.abdulrahmanalsaeed.feedveil.detection

import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentCandidateDeduperTest {
    private fun scan(rect: ScreenRect = ScreenRect(10, 100, 390, 400)) =
        CandidateScan(SurfaceHint.UNKNOWN,
            listOf(AdCandidate(3, AdLabelObserver.Kind.AD_BADGE, rect,
                CandidateConfidence.PROVISIONAL, SurfaceHint.UNKNOWN)),
            unresolvedMarkerNodes = 0)

    @Test fun suppressesMultipleScansOfSameRecentPlacement() {
        val tracker = RecentCandidateDeduper()
        assertEquals(1, tracker.observe(scan(), 1000).newPlacementClusters)
        assertEquals(1, tracker.observe(scan(), 1400).repeatsSuppressed)
        assertEquals(0, tracker.observe(scan(), 1400).newPlacementClusters)
    }

    @Test fun movingToAnotherPlacementCreatesAnotherCluster() {
        val tracker = RecentCandidateDeduper()
        tracker.observe(scan(), 1000)
        assertEquals(1, tracker.observe(scan(ScreenRect(10, 520, 390, 820)), 1200)
            .newPlacementClusters)
    }

    @Test fun expirationAndResetDoNotPersistFingerprint() {
        val tracker = RecentCandidateDeduper()
        tracker.observe(scan(), 1000)
        assertEquals(1, tracker.observe(scan(), 4000).newPlacementClusters)
        tracker.clear()
        assertEquals(1, tracker.observe(scan(), 4100).newPlacementClusters)
    }

    @Test fun geometryOverlapIsConservative() {
        assertEquals(false, ScreenRect(0, 0, 300, 300)
            .nearSamePlacement(ScreenRect(300, 300, 600, 600)))
    }
}

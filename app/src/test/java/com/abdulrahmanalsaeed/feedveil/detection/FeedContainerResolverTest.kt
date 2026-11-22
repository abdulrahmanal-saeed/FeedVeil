package com.abdulrahmanalsaeed.feedveil.detection

import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import org.junit.Assert.*
import org.junit.Test

class FeedContainerResolverTest {
    private val viewport = ScreenRect(0, 0, 400, 900)
    private fun n(
        id: Int, parent: Int?, depth: Int,
        rect: ScreenRect, kind: AdLabelObserver.Kind = AdLabelObserver.Kind.NONE,
        other: Boolean = false, hint: SurfaceHint = SurfaceHint.UNKNOWN
    ) = UiNodeFeature(id, parent, depth, rect, kind, other, hint)

    private fun feedAd(): List<UiNodeFeature> = listOf(
        n(0, null, 0, viewport),
        n(1, 0, 1, ScreenRect(0, 35, 400, 840)),
        n(2, 1, 2, ScreenRect(0, 80, 400, 490)),
        n(3, 2, 3, ScreenRect(25, 95, 80, 120), AdLabelObserver.Kind.AD_BADGE),
        n(4, 2, 3, ScreenRect(25, 145, 300, 200), other = true),
        n(5, 2, 3, ScreenRect(30, 250, 350, 410)),
        n(6, 1, 2, ScreenRect(0, 520, 400, 820), other = true)
    )

    @Test fun associatesMarkerWithNearestPlausiblePost() {
        val scan = FeedContainerResolver().analyze(feedAd(), viewport)
        assertEquals(1, scan.provisionalCount)
        assertEquals(2, scan.candidates.single().containerId)
        assertEquals(CandidateConfidence.PROVISIONAL, scan.candidates.single().confidence)
        assertEquals(0, scan.unresolvedMarkerNodes)
    }

    @Test fun adTextInOrdinaryCaptionIsIgnoredByMarkerObserver() {
        assertEquals(AdLabelObserver.Kind.NONE, AdLabelObserver.classify("It is an ad I saw"))
        val normal = feedAd().map {
            if (it.id == 3) it.copy(marker = AdLabelObserver.Kind.NONE, hasOtherReadableLabel = true) else it
        }
        assertTrue(FeedContainerResolver().analyze(normal, viewport).candidates.isEmpty())
    }

    @Test fun markerWithoutSafeAncestorRemainsUnknown() {
        val sparse = listOf(
            n(0, null, 0, viewport),
            n(1, 0, 1, ScreenRect(0, 0, 400, 850)),
            n(2, 1, 2, ScreenRect(5, 10, 100, 32), AdLabelObserver.Kind.AD_BADGE)
        )
        val scan = FeedContainerResolver().analyze(sparse, viewport)
        assertEquals(0, scan.provisionalCount)
        assertEquals(1, scan.unresolvedMarkerNodes)
    }

    @Test fun twoBadgesInOneContainerAreOneCandidate() {
        val frames = feedAd() + n(7, 2, 3, ScreenRect(22, 125, 65, 145),
            AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        val scan = FeedContainerResolver().analyze(frames, viewport)
        assertEquals(1, scan.provisionalCount)
        assertEquals(0, scan.unresolvedMarkerNodes)
    }

    @Test fun selectedNavigationHintIsNotInferredFromUnselectedReelsTab() {
        val frames = feedAd() + n(8, 1, 2, ScreenRect(300, 10, 350, 30),
            hint = SurfaceHint.REELS)
        assertEquals(SurfaceHint.REELS, FeedContainerResolver().analyze(frames, viewport).surfaceHint)
        val unknown = FeedContainerResolver().analyze(feedAd(), viewport)
        assertEquals(SurfaceHint.UNKNOWN, unknown.surfaceHint)
    }
}

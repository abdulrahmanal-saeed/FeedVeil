package com.abdulrahmanalsaeed.feedveil.autoskip

import com.abdulrahmanalsaeed.feedveil.detection.AdCandidate
import com.abdulrahmanalsaeed.feedveil.detection.CandidateConfidence
import com.abdulrahmanalsaeed.feedveil.detection.CandidateScan
import com.abdulrahmanalsaeed.feedveil.detection.ScreenRect
import com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint
import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSkipPolicyTest {
    private val viewport = ScreenRect(0, 0, 400, 900)
    private val container = ScreenRect(0, 250, 400, 700)
    private fun scan(kind: AdLabelObserver.Kind, unresolved: Int = 0, surface: SurfaceHint = SurfaceHint.UNKNOWN) =
        CandidateScan(surface, listOf(AdCandidate(1, kind, container,
            CandidateConfidence.PROVISIONAL, surface)), unresolved)

    @Test fun defaultAndAmbiguousModesNeverSkip() {
        val policy = AutoSkipPolicy()
        val feed = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        assertFalse(policy.maySkip(feed, ManualTestMode.UNSPECIFIED, viewport, 1000))
        assertFalse(policy.maySkip(feed, ManualTestMode.UNSPECIFIED, viewport, 3000))
        assertFalse(policy.maySkip(feed, ManualTestMode.REELS, viewport, 4000))
        assertFalse(policy.maySkip(feed, ManualTestMode.REELS, viewport, 6000))
    }

    @Test fun feedTwoStableSamplesEligibleButNeverFirst() {
        val policy = AutoSkipPolicy()
        val feed = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 1000))
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 1800))
        assertTrue(policy.maySkip(feed, ManualTestMode.FEED, viewport, 2400))
        policy.afterCompletedSkip(2400)
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 25000))
        assertFalse(policy.maySkip(CandidateScan(SurfaceHint.UNKNOWN, emptyList(), 0),
            ManualTestMode.FEED, viewport, 25500))
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 26000))
        assertTrue(policy.maySkip(feed, ManualTestMode.FEED, viewport, 27400))
    }

    @Test fun reelsRequireStandaloneBadgeAndCorrectSurface() {
        val policy = AutoSkipPolicy()
        val reels = scan(AdLabelObserver.Kind.AD_BADGE)
        assertFalse(policy.maySkip(reels, ManualTestMode.REELS, viewport, 10))
        assertTrue(policy.maySkip(reels, ManualTestMode.REELS, viewport, 1800))
        val other = AutoSkipPolicy()
        assertFalse(other.maySkip(scan(AdLabelObserver.Kind.AD_BADGE, 0, SurfaceHint.FEED),
            ManualTestMode.REELS, viewport, 100))
        assertFalse(other.maySkip(scan(AdLabelObserver.Kind.AD_BADGE, 0, SurfaceHint.FEED),
            ManualTestMode.REELS, viewport, 2000))
    }

    @Test fun unresolvedAndWrongGeometryDoNotAuthorizeAction() {
        val policy = AutoSkipPolicy()
        val feed = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT, unresolved = 1)
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 1000))
        assertFalse(policy.maySkip(feed, ManualTestMode.FEED, viewport, 3000))
        val small = CandidateScan(SurfaceHint.UNKNOWN,
            listOf(AdCandidate(1, AdLabelObserver.Kind.AD_METADATA_SEGMENT,
                ScreenRect(10, 250, 90, 300), CandidateConfidence.PROVISIONAL, SurfaceHint.UNKNOWN)), 0)
        assertFalse(policy.maySkip(small, ManualTestMode.FEED, viewport, 4000))
        assertFalse(policy.maySkip(small, ManualTestMode.FEED, viewport, 6000))
    }

    @Test fun multipleCandidateContainersAreAmbiguous() {
        val p = AutoSkipPolicy()
        val a = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT).candidates.single()
        val double = CandidateScan(SurfaceHint.UNKNOWN, listOf(a, a.copy(containerId = 2)), 0)
        assertFalse(p.maySkip(double, ManualTestMode.FEED, viewport, 100))
        assertFalse(p.maySkip(double, ManualTestMode.FEED, viewport, 2500))
    }
}




class GlobalAutoSkipPolicyTest {
    private val viewport = ScreenRect(0, 0, 400, 900)
    private val rect = ScreenRect(0, 250, 400, 700)
    private fun scan(kind: AdLabelObserver.Kind, hint: SurfaceHint = SurfaceHint.UNKNOWN) =
        CandidateScan(hint, listOf(AdCandidate(1, kind, rect,
            CandidateConfidence.PROVISIONAL, hint)), 0)

    @Test fun generalFeedAutoSelectsFeedSwipeStyle() {
        val policy = AutoSkipPolicy()
        val c = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 1000))
        assertTrue(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 2400))
        assertEquals(ManualTestMode.FEED, policy.suggestedSwipeMode)
    }

    @Test fun generalReelsAutoSelectsReelSwipeStyle() {
        val policy = AutoSkipPolicy()
        val c = scan(AdLabelObserver.Kind.AD_BADGE)
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 1000))
        assertTrue(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 2400))
        assertEquals(ManualTestMode.REELS, policy.suggestedSwipeMode)
    }

    @Test fun genericSponsoredUnknownSurfaceFailsClosed() {
        val policy = AutoSkipPolicy()
        val c = scan(AdLabelObserver.Kind.SPONSORED_BADGE)
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 1000))
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 2600))
    }

    @Test fun generalModeCanContinueAfterCompletedSwipeWithNewEvidence() {
        val policy = AutoSkipPolicy()
        val c = scan(AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 1000))
        assertTrue(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 2400))
        policy.afterCompletedSkip(2400)
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 17000))
        policy.observedPostSkipScroll()
        assertFalse(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 17500))
        assertTrue(policy.maySkip(c, ManualTestMode.GENERAL, viewport, 19000))
    }
}

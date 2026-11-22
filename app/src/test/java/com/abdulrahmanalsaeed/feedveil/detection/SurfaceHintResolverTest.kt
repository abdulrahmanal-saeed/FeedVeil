package com.abdulrahmanalsaeed.feedveil.detection

import org.junit.Assert.assertEquals
import org.junit.Test

class SurfaceHintResolverTest {
    @Test fun selectedTabsOnly() {
        assertEquals(SurfaceHint.REELS, SurfaceHintResolver.selectedTabHint("Reels", true))
        assertEquals(SurfaceHint.FEED, SurfaceHintResolver.selectedTabHint("الرئيسية", true))
        assertEquals(SurfaceHint.UNKNOWN, SurfaceHintResolver.selectedTabHint("Reels", false))
        assertEquals(SurfaceHint.UNKNOWN, SurfaceHintResolver.selectedTabHint("User likes Reels", true))
    }

    @Test fun conflictingOrAbsentSignalsAreUnknown() {
        val rect = ScreenRect(0, 0, 200, 400)
        fun node(id: Int, hint: SurfaceHint) = UiNodeFeature(id, null, 0, rect,
            com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver.Kind.NONE,
            false, hint)
        assertEquals(SurfaceHint.UNKNOWN, SurfaceHintResolver.resolve(listOf(node(1, SurfaceHint.FEED),
            node(2, SurfaceHint.REELS))))
        assertEquals(SurfaceHint.UNKNOWN, SurfaceHintResolver.resolve(emptyList()))
        assertEquals(SurfaceHint.FEED, SurfaceHintResolver.resolve(listOf(node(1, SurfaceHint.FEED))))
    }
}

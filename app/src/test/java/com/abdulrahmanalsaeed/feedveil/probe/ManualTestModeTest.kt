package com.abdulrahmanalsaeed.feedveil.probe

import org.junit.Assert.assertEquals
import org.junit.Test

class ManualTestModeTest {
    @Test fun parsesOnlyKnownManualModes() {
        assertEquals(ManualTestMode.FEED, ManualTestMode.fromStored("FEED"))
        assertEquals(ManualTestMode.REELS, ManualTestMode.fromStored("REELS"))
        assertEquals(ManualTestMode.UNSPECIFIED, ManualTestMode.fromStored("UNSPECIFIED"))
    }

    @Test fun unknownOrMissingModesNeverInventASurface() {
        assertEquals(ManualTestMode.UNSPECIFIED, ManualTestMode.fromStored("HOME"))
        assertEquals(ManualTestMode.UNSPECIFIED, ManualTestMode.fromStored(null))
        assertEquals(ManualTestMode.UNSPECIFIED, ManualTestMode.fromStored("feed"))
    }
}

package com.abdulrahmanalsaeed.feedveil.probe

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SponsoredLabelMatcherTest {
    @Test fun englishExactLabel() {
        assertTrue(SponsoredLabelMatcher.isExactMarker("Sponsored"))
        assertTrue(SponsoredLabelMatcher.isExactMarker("  SPONSORED  "))
    }

    @Test fun arabicExactLabel() {
        assertTrue(SponsoredLabelMatcher.isExactMarker("ممول"))
        assertTrue(SponsoredLabelMatcher.isExactMarker("مُمَوَّل"))
    }

    @Test fun unrelatedTextIsNeverClassifiedAsMarker() {
        assertFalse(SponsoredLabelMatcher.isExactMarker("My post is sponsored by someone"))
        assertFalse(SponsoredLabelMatcher.isExactMarker("غير ممول"))
        assertFalse(SponsoredLabelMatcher.isExactMarker("Suggested for you"))
        assertFalse(SponsoredLabelMatcher.isExactMarker(""))
        assertFalse(SponsoredLabelMatcher.isExactMarker(null))
    }

    @Test fun noUnknownAppCanBeScanned() {
        assertTrue(ProbeScope.isTargetPackage("com.facebook.katana"))
        assertFalse(ProbeScope.isTargetPackage("com.facebook.lite"))
        assertFalse(ProbeScope.isTargetPackage("com.example.test"))
        assertFalse(ProbeScope.isTargetPackage(null))
    }

    @Test fun policyRequiresConsentAndEnabledMode() {
        assertTrue(ProbeScope.mayRead("com.facebook.katana", consented = true, active = true))
        assertFalse(ProbeScope.mayRead("com.facebook.katana", consented = false, active = true))
        assertFalse(ProbeScope.mayRead("com.facebook.katana", consented = true, active = false))
        assertFalse(ProbeScope.mayRead("com.other", consented = true, active = true))
    }
}

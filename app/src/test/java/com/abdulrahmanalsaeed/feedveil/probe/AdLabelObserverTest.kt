package com.abdulrahmanalsaeed.feedveil.probe

import org.junit.Assert.assertEquals
import org.junit.Test

class AdLabelObserverTest {
    private fun expect(s: String?, kind: AdLabelObserver.Kind) {
        assertEquals(kind, AdLabelObserver.classify(s))
    }

    @Test fun matchesAdLabelsInRealScreenshots() {
        expect("Ad", AdLabelObserver.Kind.AD_BADGE)
        expect(" AD ", AdLabelObserver.Kind.AD_BADGE)
        expect("Advertisement", AdLabelObserver.Kind.AD_BADGE)
        expect("إعلان", AdLabelObserver.Kind.AD_BADGE)
        expect("Ad •", AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        expect("• Ad", AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        expect("Yango Drive • Ad", AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        expect("Careem: Taxi, Food & D… • Ad", AdLabelObserver.Kind.AD_METADATA_SEGMENT)
        expect("Ad · 🌐", AdLabelObserver.Kind.AD_METADATA_SEGMENT)
    }

    @Test fun matchesLegacySponsoredLabels() {
        expect("Sponsored", AdLabelObserver.Kind.SPONSORED_BADGE)
        expect("ممول", AdLabelObserver.Kind.SPONSORED_BADGE)
        expect("مُمَوَّل", AdLabelObserver.Kind.SPONSORED_BADGE)
        expect("إعلان ممول", AdLabelObserver.Kind.SPONSORED_BADGE)
    }

    @Test fun rejectsOrdinaryProse() {
        expect("My sponsored article", AdLabelObserver.Kind.NONE)
        expect("My post is an ad", AdLabelObserver.Kind.NONE)
        expect("Adidas", AdLabelObserver.Kind.NONE)
        expect("Some ad tips", AdLabelObserver.Kind.NONE)
        expect("غير ممول", AdLabelObserver.Kind.NONE)
        expect("", AdLabelObserver.Kind.NONE)
        expect(null, AdLabelObserver.Kind.NONE)
    }

    @Test fun combinesTextAndDescriptionOncePerNode() {
        assertEquals(AdLabelObserver.Kind.AD_BADGE,
            AdLabelObserver.combine(AdLabelObserver.Kind.AD_METADATA_SEGMENT, AdLabelObserver.Kind.AD_BADGE))
    }
}

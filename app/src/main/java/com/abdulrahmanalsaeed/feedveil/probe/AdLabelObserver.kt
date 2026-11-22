package com.abdulrahmanalsaeed.feedveil.probe

import java.util.Locale

/** Passive Phase 0 observations only. NEVER use to authorize clicks or scrolling. */
object AdLabelObserver {
    enum class Kind { NONE, SPONSORED_BADGE, AD_BADGE, AD_METADATA_SEGMENT }
    private val arabicDiacritics = Regex("[\\u064B-\\u065F\\u0670\\u0640]")
    private val rtlMarks = Regex("[\\u200E\\u200F\\u061C]")
    private val whitespace = Regex("\\s+")
    private val separatedAd = Regex("""(?:^|[•·∙])\s*ad\s*(?:$|[•·∙])""")

    fun classify(value: String?): Kind {
        if (value.isNullOrBlank() || value.length > 280) return Kind.NONE
        val cleaned = value.lowercase(Locale.ROOT).trim()
            .replace(arabicDiacritics, "")
            .replace(rtlMarks, "")
            .replace(whitespace, " ")
        if (cleaned == "sponsored" || cleaned == "ممول" ||
            cleaned == "إعلان ممول" || cleaned == "اعلان ممول") return Kind.SPONSORED_BADGE
        if (cleaned == "ad" || cleaned == "advertisement" ||
            cleaned == "إعلان" || cleaned == "اعلان") return Kind.AD_BADGE
        if (separatedAd.containsMatchIn(cleaned)) return Kind.AD_METADATA_SEGMENT
        return Kind.NONE
    }

    fun combine(a: Kind, b: Kind): Kind = when {
        a == Kind.SPONSORED_BADGE || b == Kind.SPONSORED_BADGE -> Kind.SPONSORED_BADGE
        a == Kind.AD_BADGE || b == Kind.AD_BADGE -> Kind.AD_BADGE
        a == Kind.AD_METADATA_SEGMENT || b == Kind.AD_METADATA_SEGMENT -> Kind.AD_METADATA_SEGMENT
        else -> Kind.NONE
    }
}

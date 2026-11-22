package com.abdulrahmanalsaeed.feedveil.probe

import java.util.Locale

/**
 * Hypothesis-only exact-label observer. This is not an ad classifier.
 * Never store or log the input label: Facebook post text may be sensitive.
 */
object SponsoredLabelMatcher {
    private val arabicMarks = Regex("[\\u064B-\\u065F\\u0670\\u0640]")

    fun isExactMarker(text: String?): Boolean {
        if (text.isNullOrBlank() || text.length > 64) return false
        val normalized = text.trim()
            .lowercase(Locale.ROOT)
            .replace(arabicMarks, "")
            .replace(Regex("\\s+"), " ")
        return normalized == "sponsored" || normalized == "ممول"
    }
}

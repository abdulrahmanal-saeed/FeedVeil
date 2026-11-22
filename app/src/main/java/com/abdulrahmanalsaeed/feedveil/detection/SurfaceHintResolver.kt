package com.abdulrahmanalsaeed.feedveil.detection

import java.util.Locale

/** Conservative heuristic; exact selected tab strings only. Unknown is the normal fallback. */
object SurfaceHintResolver {
    fun selectedTabHint(value: String?, selected: Boolean): SurfaceHint {
        if (!selected || value.isNullOrBlank() || value.length > 70) return SurfaceHint.UNKNOWN
        val normalized = value.trim().lowercase(Locale.ROOT)
        return when (normalized) {
            "home", "الصفحة الرئيسية", "الرئيسية" -> SurfaceHint.FEED
            "reels", "ريلز", "مقاطع ريلز" -> SurfaceHint.REELS
            else -> SurfaceHint.UNKNOWN
        }
    }

    fun resolve(nodes: List<UiNodeFeature>): SurfaceHint {
        val distinct = nodes.map { it.selectedSurfaceHint }
            .filter { it != SurfaceHint.UNKNOWN }.distinct()
        return distinct.singleOrNull() ?: SurfaceHint.UNKNOWN
    }
}

package com.abdulrahmanalsaeed.feedveil.detection

import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver

/**
 * Read-only structural experiment. Chooses a plausible bounded ancestor of an ad badge,
 * rather than mistaking an "Ad" text node or whole app window for a verified post.
 * PROVISIONAL means **not suitable for automated actions**.
 */
class FeedContainerResolver {
    fun analyze(features: List<UiNodeFeature>, viewport: ScreenRect): CandidateScan {
        if (features.isEmpty() || viewport.width <= 0 || viewport.height <= 0)
            return CandidateScan(SurfaceHint.UNKNOWN, emptyList(), 0)

        val surface = SurfaceHintResolver.resolve(features)
        val byId = features.associateBy { it.id }
        val markers = features.filter { it.marker != AdLabelObserver.Kind.NONE }
        val chosen = linkedMapOf<Int, AdCandidate>()
        var unresolved = 0

        for (markerNode in markers) {
            var parentId = markerNode.parentId
            var iterations = 0
            var resolved: UiNodeFeature? = null
            while (parentId != null && iterations++ < 18) {
                val ancestor = byId[parentId] ?: break
                if (isPlausibleContainer(ancestor, markerNode, features, byId, viewport)) {
                    resolved = ancestor
                    break // nearest valid ancestor is the most conservative boundary
                }
                parentId = ancestor.parentId
            }
            if (resolved == null) {
                unresolved++
                continue
            }
            val old = chosen[resolved.id]
            chosen[resolved.id] = AdCandidate(
                containerId = resolved.id,
                rect = resolved.rect,
                confidence = CandidateConfidence.PROVISIONAL,
                surfaceHint = surface,
                marker = if (old == null) markerNode.marker
                    else AdLabelObserver.combine(old.marker, markerNode.marker)
            )
        }

        return CandidateScan(surface, chosen.values.toList(), unresolved)
    }

    private fun isPlausibleContainer(
        ancestor: UiNodeFeature,
        marker: UiNodeFeature,
        features: List<UiNodeFeature>,
        byId: Map<Int, UiNodeFeature>,
        viewport: ScreenRect
    ): Boolean {
        // Never accept the app root. A partly visible item can be tall but must not be
        // nearly the entire window; this deliberately produces false negatives.
        if (ancestor.parentId == null || ancestor.depth < 2) return false
        val r = ancestor.rect
        if (!r.contains(marker.rect) ||
            r.width < viewport.width * 0.58 ||
            r.height < viewport.height * 0.10 ||
            r.area > viewport.area * 0.88
        ) return false

        // At least one OTHER readable descendant besides the label must belong to this
        // ancestor, and there must be enough visible descendants to resemble a post.
        var totalDescendants = 0
        var otherReadable = false
        for (feature in features) {
            if (feature.id == ancestor.id || feature.id == marker.id) continue
            if (!descendantOf(feature.id, ancestor.id, byId)) continue
            totalDescendants++
            if (feature.hasOtherReadableLabel) otherReadable = true
            if (totalDescendants >= 2 && otherReadable) return true
        }
        return false
    }

    private fun descendantOf(id: Int, potentialAncestor: Int, byId: Map<Int, UiNodeFeature>): Boolean {
        var current = byId[id]?.parentId
        var hops = 0
        while (current != null && hops++ < 20) {
            if (current == potentialAncestor) return true
            current = byId[current]?.parentId
        }
        return false
    }
}

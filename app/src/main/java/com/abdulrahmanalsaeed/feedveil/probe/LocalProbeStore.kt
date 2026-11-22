package com.abdulrahmanalsaeed.feedveil.probe

import android.content.Context

/**
 * No Facebook content or node identifier is persisted. Only local counters and explicit consent.
 * Phase 0 uses SharedPreferences to keep the small cross-component state simple; a later phase
 * can migrate to DataStore as described in the architecture.
 */
object LocalProbeStore {
    private const val FILE = "phase0_local_settings"
    private const val CONSENT = "consent"
    private const val ACTIVE = "probe_active"
    private const val EVENTS = "sampled_events"
    private const val SCANS = "completed_scans"
    private const val LABELS = "marker_observations"
    private const val AD_BADGES = "ad_badge_observations"
    private const val AD_METADATA = "ad_metadata_observations"
    private const val TEXT_NODES = "last_text_nodes"
    private const val DESCRIPTIONS = "last_content_descriptions"
    private const val NODE_COUNT = "last_node_count"
    private const val LAST_SCAN = "last_scan_time"
    private const val CANDIDATES = "phase1_provisional_container_observations"
    private const val UNRESOLVED = "phase1_unresolved_marker_observations"
    private const val CLUSTERS = "phase1_recent_placement_clusters"
    private const val REPEATED = "phase1_repeat_suppression"
    private const val FEED_SCANS = "phase1_feed_hint_scans"
    private const val REELS_SCANS = "phase1_reels_hint_scans"
    private const val UNKNOWN_SCANS = "phase1_unknown_hint_scans"
    private const val MANUAL_TEST_MODE = "phase1_manual_test_mode"
    private const val SESSION_REVISION = "phase1_session_revision"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isConsented(context: Context) = prefs(context).getBoolean(CONSENT, false)
    fun isActive(context: Context) = prefs(context).getBoolean(ACTIVE, false)

    fun currentSessionRevision(context: Context) = prefs(context).getLong(SESSION_REVISION, 0L)

    /** Selection is supplied by the user, not inferred from Facebook. Clears old counters. */
    @Synchronized
    fun beginManualTest(context: Context, mode: ManualTestMode) {
        val p = prefs(context)
        p.edit().putString(MANUAL_TEST_MODE, mode.name).commit()
        resetCounters(context)
    }

    @Synchronized
    fun grantReadOnlyConsent(context: Context) {
        prefs(context).edit().putBoolean(CONSENT, true).putBoolean(ACTIVE, true).apply()
    }

    @Synchronized
    fun revokeInAppConsent(context: Context) {
        prefs(context).edit().putBoolean(CONSENT, false).putBoolean(ACTIVE, false).apply()
    }

    @Synchronized
    fun setActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(ACTIVE, active && isConsented(context)).apply()
    }

    @Synchronized
    fun recordEvent(context: Context) {
        val preferences = prefs(context)
        preferences.edit()
            .putLong(EVENTS, preferences.getLong(EVENTS, 0L) + 1L)
            .apply()
    }

    @Synchronized
    fun recordScan(context: Context, observation: ScreenObservation) {
        val preferences = prefs(context)
        preferences.edit()
            .putLong(SCANS, preferences.getLong(SCANS, 0L) + 1L)
            .putLong(LABELS, preferences.getLong(LABELS, 0L) + observation.exactMarkerCount)
            .putLong(AD_BADGES, preferences.getLong(AD_BADGES, 0L) + observation.adBadgeCount)
            .putLong(AD_METADATA, preferences.getLong(AD_METADATA, 0L) + observation.adMetadataCount)
            .putInt(TEXT_NODES, observation.textNodeCount)
            .putInt(DESCRIPTIONS, observation.descriptionNodeCount)
            .putInt(NODE_COUNT, observation.nodeCount)
            .putLong(LAST_SCAN, System.currentTimeMillis())
            .putLong(CANDIDATES, preferences.getLong(CANDIDATES, 0L) + observation.provisionalContainers)
            .putLong(UNRESOLVED, preferences.getLong(UNRESOLVED, 0L) + observation.unresolvedMarkers)
            .putLong(CLUSTERS, preferences.getLong(CLUSTERS, 0L) + observation.placementClusters)
            .putLong(REPEATED, preferences.getLong(REPEATED, 0L) + observation.repeatedPlacements)
            .putLong(FEED_SCANS, preferences.getLong(FEED_SCANS, 0L) +
                if (observation.surfaceHint == com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.FEED) 1 else 0)
            .putLong(REELS_SCANS, preferences.getLong(REELS_SCANS, 0L) +
                if (observation.surfaceHint == com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.REELS) 1 else 0)
            .putLong(UNKNOWN_SCANS, preferences.getLong(UNKNOWN_SCANS, 0L) +
                if (observation.surfaceHint == com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.UNKNOWN) 1 else 0)
            .apply()
    }

    @Synchronized
    fun resetCounters(context: Context) {
        prefs(context).edit()
            .remove(EVENTS)
            .remove(SCANS)
            .remove(LABELS)
            .remove(AD_BADGES)
            .remove(AD_METADATA)
            .remove(TEXT_NODES)
            .remove(DESCRIPTIONS)
            .remove(NODE_COUNT)
            .remove(LAST_SCAN)
            .remove(CANDIDATES)
            .remove(UNRESOLVED)
            .remove(CLUSTERS)
            .remove(REPEATED)
            .remove(FEED_SCANS)
            .remove(REELS_SCANS)
            .remove(UNKNOWN_SCANS)
            .putLong(SESSION_REVISION, prefs(context).getLong(SESSION_REVISION, 0L) + 1L)
            .apply()
    }

    fun snapshot(context: Context): ProbeSnapshot {
        val p = prefs(context)
        return ProbeSnapshot(
            consented = p.getBoolean(CONSENT, false),
            active = p.getBoolean(ACTIVE, false),
            manualTestMode = ManualTestMode.fromStored(p.getString(MANUAL_TEST_MODE, null)),
            eligibleEvents = p.getLong(EVENTS, 0L),
            completedScans = p.getLong(SCANS, 0L),
            markerObservations = p.getLong(LABELS, 0L),
            adBadgeObservations = p.getLong(AD_BADGES, 0L),
            adMetadataObservations = p.getLong(AD_METADATA, 0L),
            lastTextNodes = p.getInt(TEXT_NODES, 0),
            lastContentDescriptions = p.getInt(DESCRIPTIONS, 0),
            lastNodesInspected = p.getInt(NODE_COUNT, 0),
            lastScannedAtEpochMs = p.getLong(LAST_SCAN, 0L),
            provisionalContainerObservations = p.getLong(CANDIDATES, 0L),
            unresolvedMarkerObservations = p.getLong(UNRESOLVED, 0L),
            placementClusters = p.getLong(CLUSTERS, 0L),
            repeatedPlacements = p.getLong(REPEATED, 0L),
            feedHintScans = p.getLong(FEED_SCANS, 0L),
            reelsHintScans = p.getLong(REELS_SCANS, 0L),
            unknownHintScans = p.getLong(UNKNOWN_SCANS, 0L)
        )
    }
}

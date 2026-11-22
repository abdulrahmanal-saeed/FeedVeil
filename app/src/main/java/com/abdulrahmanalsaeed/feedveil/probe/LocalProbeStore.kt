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

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isConsented(context: Context) = prefs(context).getBoolean(CONSENT, false)
    fun isActive(context: Context) = prefs(context).getBoolean(ACTIVE, false)

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
            .apply()
    }

    fun snapshot(context: Context): ProbeSnapshot {
        val p = prefs(context)
        return ProbeSnapshot(
            consented = p.getBoolean(CONSENT, false),
            active = p.getBoolean(ACTIVE, false),
            eligibleEvents = p.getLong(EVENTS, 0L),
            completedScans = p.getLong(SCANS, 0L),
            markerObservations = p.getLong(LABELS, 0L),
            adBadgeObservations = p.getLong(AD_BADGES, 0L),
            adMetadataObservations = p.getLong(AD_METADATA, 0L),
            lastTextNodes = p.getInt(TEXT_NODES, 0),
            lastContentDescriptions = p.getInt(DESCRIPTIONS, 0),
            lastNodesInspected = p.getInt(NODE_COUNT, 0),
            lastScannedAtEpochMs = p.getLong(LAST_SCAN, 0L)
        )
    }
}

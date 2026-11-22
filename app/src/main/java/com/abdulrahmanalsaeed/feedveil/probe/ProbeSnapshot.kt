package com.abdulrahmanalsaeed.feedveil.probe

data class ProbeSnapshot(
    val consented: Boolean,
    val active: Boolean,
    val eligibleEvents: Long,
    val completedScans: Long,
    val markerObservations: Long,
    val adBadgeObservations: Long,
    val adMetadataObservations: Long,
    val lastTextNodes: Int,
    val lastContentDescriptions: Int,
    val lastNodesInspected: Int,
    val lastScannedAtEpochMs: Long
)

data class ScreenObservation(
    val nodeCount: Int,
    val exactMarkerCount: Int,
    val adBadgeCount: Int,
    val adMetadataCount: Int,
    val textNodeCount: Int,
    val descriptionNodeCount: Int
)

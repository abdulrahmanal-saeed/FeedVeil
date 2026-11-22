package com.abdulrahmanalsaeed.feedveil.probe

data class ProbeSnapshot(
    val consented: Boolean,
    val active: Boolean,
    val manualTestMode: ManualTestMode,
    val eligibleEvents: Long,
    val completedScans: Long,
    val markerObservations: Long,
    val adBadgeObservations: Long,
    val adMetadataObservations: Long,
    val lastTextNodes: Int,
    val lastContentDescriptions: Int,
    val lastNodesInspected: Int,
    val lastScannedAtEpochMs: Long,
    val provisionalContainerObservations: Long,
    val unresolvedMarkerObservations: Long,
    val placementClusters: Long,
    val repeatedPlacements: Long,
    val feedHintScans: Long,
    val reelsHintScans: Long,
    val unknownHintScans: Long
)

data class ScreenObservation(
    val nodeCount: Int,
    val exactMarkerCount: Int,
    val adBadgeCount: Int,
    val adMetadataCount: Int,
    val textNodeCount: Int,
    val descriptionNodeCount: Int,
    val provisionalContainers: Int,
    val unresolvedMarkers: Int,
    val placementClusters: Int,
    val repeatedPlacements: Int,
    val surfaceHint: com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint
)

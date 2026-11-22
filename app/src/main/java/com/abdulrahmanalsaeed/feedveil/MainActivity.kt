package com.abdulrahmanalsaeed.feedveil

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode
import com.abdulrahmanalsaeed.feedveil.probe.ProbeSnapshot
import com.abdulrahmanalsaeed.feedveil.service.FacebookProbeService
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private var refreshCounter by mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        refreshCounter += 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Read Compose state so resuming from Android settings refreshes the diagnostics.
            @Suppress("UNUSED_VARIABLE")
            val revision = refreshCounter
            val snapshot = LocalProbeStore.snapshot(this)
            val serviceEnabled = isProbeServiceEnabled()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Phase0Screen(
                        snapshot = snapshot,
                        serviceEnabled = serviceEnabled,
                        onConsent = {
                            LocalProbeStore.grantReadOnlyConsent(this)
                            refreshCounter++
                            openSystemAccessibilitySettings()
                        },
                        onDecline = {
                            LocalProbeStore.revokeInAppConsent(this)
                            refreshCounter++
                        },
                        onSettings = { openSystemAccessibilitySettings() },
                        onPauseResume = {
                            LocalProbeStore.setActive(this, !snapshot.active)
                            refreshCounter++
                        },
                        onRefresh = { refreshCounter++ },
                        onReset = {
                            LocalProbeStore.resetCounters(this)
                            refreshCounter++
                        },
                        onBeginTest = { mode ->
                            LocalProbeStore.beginManualTest(this, mode)
                            refreshCounter++
                        }
                    )
                }
            }
        }
    }

    private fun openSystemAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun isProbeServiceEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any {
                it.resolveInfo.serviceInfo.packageName == packageName &&
                    it.resolveInfo.serviceInfo.name ==
                    FacebookProbeService::class.java.name
            }
    }
}

@Composable
private fun Phase0Screen(
    snapshot: ProbeSnapshot,
    serviceEnabled: Boolean,
    onConsent: () -> Unit,
    onDecline: () -> Unit,
    onSettings: () -> Unit,
    onPauseResume: () -> Unit,
    onRefresh: () -> Unit,
    onReset: () -> Unit,
    onBeginTest: (ManualTestMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.subtitle), style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.warning),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.permission_notice), style = MaterialTheme.typography.bodyMedium)

        if (!snapshot.consented) {
            Button(onClick = onConsent, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.accept))
            }
        } else {
            OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.accessibility_settings))
            }
        }

        OutlinedButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.decline))
        }

        HorizontalDivider()
        Text(stringResource(R.string.status_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(if (serviceEnabled) R.string.permission_on else R.string.permission_off))
        Text(stringResource(
            when {
                !snapshot.consented -> R.string.probe_not_consented
                snapshot.active && serviceEnabled -> R.string.probe_on
                else -> R.string.probe_off
            }
        ))

        if (snapshot.consented) {
            Button(onClick = onPauseResume, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (snapshot.active) R.string.pause else R.string.resume))
            }
        }

        HorizontalDivider()
        Text(stringResource(R.string.manual_test_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.manual_test_explanation), style = MaterialTheme.typography.bodySmall)
        Text(
            stringResource(
                when (snapshot.manualTestMode) {
                    ManualTestMode.FEED -> R.string.manual_selected_feed
                    ManualTestMode.REELS -> R.string.manual_selected_reels
                    ManualTestMode.UNSPECIFIED -> R.string.manual_selected_unspecified
                }
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (snapshot.manualTestMode == ManualTestMode.FEED) {
                Button(onClick = { onBeginTest(ManualTestMode.FEED) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.manual_feed))
                }
            } else {
                OutlinedButton(onClick = { onBeginTest(ManualTestMode.FEED) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.manual_feed))
                }
            }
            if (snapshot.manualTestMode == ManualTestMode.REELS) {
                Button(onClick = { onBeginTest(ManualTestMode.REELS) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.manual_reels))
                }
            } else {
                OutlinedButton(onClick = { onBeginTest(ManualTestMode.REELS) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.manual_reels))
                }
            }
        }
        OutlinedButton(onClick = { onBeginTest(ManualTestMode.UNSPECIFIED) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.manual_unspecified))
        }
        HorizontalDivider()
        Text(stringResource(R.string.scans, snapshot.completedScans))
        Text(stringResource(R.string.markers, snapshot.markerObservations))
        Text(stringResource(R.string.ad_badges, snapshot.adBadgeObservations))
        Text(stringResource(R.string.ad_metadata, snapshot.adMetadataObservations))
        HorizontalDivider()
        Text(stringResource(R.string.phase1_heading), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.phase1_note), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.phase1_provisional, snapshot.provisionalContainerObservations))
        Text(stringResource(R.string.phase1_unresolved, snapshot.unresolvedMarkerObservations))
        Text(stringResource(R.string.phase1_clusters, snapshot.placementClusters))
        Text(stringResource(R.string.phase1_repeated, snapshot.repeatedPlacements))
        Text(stringResource(R.string.phase1_surface, snapshot.feedHintScans, snapshot.reelsHintScans, snapshot.unknownHintScans))
        HorizontalDivider()
        Text(stringResource(R.string.text_nodes, snapshot.lastTextNodes))
        Text(stringResource(R.string.description_nodes, snapshot.lastContentDescriptions))
        Text(stringResource(R.string.events, snapshot.eligibleEvents))
        Text(stringResource(R.string.nodes, snapshot.lastNodesInspected))
        Text(
            if (snapshot.lastScannedAtEpochMs == 0L) {
                stringResource(R.string.no_scans)
            } else {
                stringResource(
                    R.string.last_scan,
                    DateFormat.getDateTimeInstance().format(Date(snapshot.lastScannedAtEpochMs))
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.refresh))
            }
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.clear))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.testing_hint), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.privacy_footer), style = MaterialTheme.typography.bodySmall)
    }
}

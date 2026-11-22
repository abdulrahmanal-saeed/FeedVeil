package com.abdulrahmanalsaeed.feedveil.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.abdulrahmanalsaeed.feedveil.R
import com.abdulrahmanalsaeed.feedveil.autoskip.AutoSkipPolicy
import com.abdulrahmanalsaeed.feedveil.autoskip.LocalAutoTrial
import com.abdulrahmanalsaeed.feedveil.detection.CandidateScan
import com.abdulrahmanalsaeed.feedveil.detection.FeedContainerResolver
import com.abdulrahmanalsaeed.feedveil.detection.RecentCandidateDeduper
import com.abdulrahmanalsaeed.feedveil.detection.ScreenRect
import com.abdulrahmanalsaeed.feedveil.detection.SurfaceHintResolver
import com.abdulrahmanalsaeed.feedveil.detection.UiNodeFeature
import com.abdulrahmanalsaeed.feedveil.probe.AdLabelObserver
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode
import com.abdulrahmanalsaeed.feedveil.probe.ProbeScope
import com.abdulrahmanalsaeed.feedveil.probe.ScreenObservation

/**
 * Experimental general Feed/Reels automatic ad skipping, OFF by default.
 * User must separately arm every session. No fixed ad count or time cap.
 * Strict provisional candidates, minimum 15s cooldown, STOP/Back overlay.
 * Only Facebook UI content is read; other apps' event package metadata supports cleanup.
 */
class FacebookProbeService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val resolver = FeedContainerResolver()
    private val deduper = RecentCandidateDeduper()
    private val policy = AutoSkipPolicy()

    private var lastScannedElapsedMs = 0L
    private var lastScrollElapsedMs = 0L
    private var lastGestureElapsedMs = 0L
    private var lastSessionRevision: Long? = null
    private var inFacebook = false
    private var gestureInFlight = false
    private var undoEligible = false
    private var previousMode = ManualTestMode.UNSPECIFIED
    private var pendingCheck: Runnable? = null

    private var overlay: LinearLayout? = null
    private var overlayLabel: TextView? = null
    private var backButton: Button? = null
    private var manualButton: Button? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Never silently resume an armed trial after service restart/reboot.
        LocalAutoTrial.disarm(this)
        removeOverlay()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventPackage = event.packageName?.toString()
        if (!ProbeScope.isTargetPackage(eventPackage)) {
            // Other apps: read event package/class metadata only, NEVER their UI nodes.
            // Our own accessibility overlay can generate window events: don't mistake
            // those for leaving Facebook. MainActivity is an actual app switch.
            val returnedToOurActivity = eventPackage == packageName &&
                event.className?.toString() == "com.abdulrahmanalsaeed.feedveil.MainActivity"
            if (inFacebook && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                (eventPackage != packageName || returnedToOurActivity)) {
                stopTrialAndHide()
            }
            return
        }
        if (!ProbeScope.mayRead(
                eventPackage, LocalProbeStore.isConsented(this), LocalProbeStore.isActive(this)
            )) {
            stopTrialAndHide()
            return
        }

        val root = rootInActiveWindow ?: return
        if (!ProbeScope.isTargetPackage(root.packageName?.toString())) {
            if (inFacebook) stopTrialAndHide()
            return
        }
        inFacebook = true

        val revision = LocalProbeStore.currentSessionRevision(this)
        if (lastSessionRevision != revision) {
            deduper.clear()
            policy.clear()
            cancelCheck()
            lastSessionRevision = revision
        }
        val now = SystemClock.elapsedRealtime()
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            lastScrollElapsedMs = now
            cancelCheck()
            if (!gestureInFlight && now > lastGestureElapsedMs + 400L &&
                lastGestureElapsedMs > 0L) policy.observedPostSkipScroll()
        }
        if (now - lastScannedElapsedMs < SCAN_INTERVAL_MS) return
        lastScannedElapsedMs = now

        LocalProbeStore.recordEvent(this)
        val frame = inspectVisibleNodes(root, now)
        LocalProbeStore.recordScan(this, frame.observation)

        if (!LocalAutoTrial.canAct(this)) {
            if (!undoEligible) removeOverlay()
            return
        }
        if (!showControls()) {
            LocalAutoTrial.disarm(this) // no trial without visible emergency stop
            return
        }
        if (!gestureInFlight) {
            val mode = LocalAutoTrial.state(this).mode
            // Don't consume the policy's stable/ready state while a swipe is still settling.
            if (now - lastScrollElapsedMs > IDLE_AFTER_SCROLL_MS) {
                val eligible = policy.maySkip(frame.analysis, mode, frame.viewport, now)
                reportReason(policy.lastReason.name)
                if (eligible) {
                    performForwardSkip(policy.suggestedSwipeMode)
                } else if (frame.analysis.candidates.isNotEmpty()) {
                    scheduleStableRecheck()
                }
            } else {
                reportReason("WAIT_FOR_SCROLL_IDLE")
                if (frame.analysis.candidates.isNotEmpty()) scheduleStableRecheck()
            }
        }
        updateControls()
    }

    private data class Pending(val node: AccessibilityNodeInfo, val parent: Int?, val depth: Int)
    private data class Frame(
        val observation: ScreenObservation,
        val analysis: CandidateScan,
        val viewport: ScreenRect
    )

    private fun inspectVisibleNodes(root: AccessibilityNodeInfo, now: Long): Frame {
        val work = ArrayDeque<Pending>()
        work.addLast(Pending(root, null, 0))
        val nodes = ArrayList<UiNodeFeature>(100)
        var sponsored = 0
        var badges = 0
        var metadata = 0
        var textNodes = 0
        var descriptionNodes = 0
        while (work.isNotEmpty() && nodes.size < MAX_NODES) {
            val (node, parent, depth) = work.removeFirst()
            if (!node.isVisibleToUser) continue
            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            if (!text.isNullOrBlank()) textNodes++
            if (!desc.isNullOrBlank()) descriptionNodes++
            val marker = AdLabelObserver.combine(
                AdLabelObserver.classify(text), AdLabelObserver.classify(desc)
            )
            when (marker) {
                AdLabelObserver.Kind.SPONSORED_BADGE -> sponsored++
                AdLabelObserver.Kind.AD_BADGE -> badges++
                AdLabelObserver.Kind.AD_METADATA_SEGMENT -> metadata++
                AdLabelObserver.Kind.NONE -> Unit
            }

            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            val id = nodes.size
            nodes.add(
                UiNodeFeature(
                    id = id,
                    parentId = parent,
                    depth = depth,
                    rect = ScreenRect(bounds.left, bounds.top, bounds.right, bounds.bottom),
                    marker = marker,
                    hasOtherReadableLabel = marker == AdLabelObserver.Kind.NONE &&
                        (!text.isNullOrBlank() || !desc.isNullOrBlank()),
                    selectedSurfaceHint = if (node.isSelected) {
                        val a = SurfaceHintResolver.selectedTabHint(text, true)
                        if (a != com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.UNKNOWN) a
                        else SurfaceHintResolver.selectedTabHint(desc, true)
                    } else com.abdulrahmanalsaeed.feedveil.detection.SurfaceHint.UNKNOWN
                )
            )
            if (depth < MAX_DEPTH) for (i in 0 until minOf(node.childCount, MAX_CHILDREN_PER_NODE)) {
                node.getChild(i)?.let { work.addLast(Pending(it, id, depth + 1)) }
            }
        }
        val viewport = nodes.firstOrNull()?.rect ?: ScreenRect(0, 0, 0, 0)
        val analysis = resolver.analyze(nodes, viewport)
        val placement = deduper.observe(analysis, now)
        return Frame(
            ScreenObservation(
                nodeCount = nodes.size,
                exactMarkerCount = sponsored,
                adBadgeCount = badges,
                adMetadataCount = metadata,
                textNodeCount = textNodes,
                descriptionNodeCount = descriptionNodes,
                provisionalContainers = analysis.provisionalCount,
                unresolvedMarkers = analysis.unresolvedMarkerNodes,
                placementClusters = placement.newPlacementClusters,
                repeatedPlacements = placement.repeatsSuppressed,
                surfaceHint = analysis.surfaceHint
            ), analysis, viewport
        )
    }

    private fun scheduleStableRecheck() {
        // IMPORTANT: do not cancel+postpone the task on each content-changed event.
        // Facebook videos can emit accessibility events continuously and starve a timer.
        if (pendingCheck != null) return
        val callback = Runnable {
            pendingCheck = null
            if (!inFacebook || gestureInFlight || !LocalAutoTrial.canAct(this)) return@Runnable
            val root = rootInActiveWindow ?: return@Runnable
            if (!ProbeScope.isTargetPackage(root.packageName?.toString())) {
                stopTrialAndHide()
                return@Runnable
            }
            val now = SystemClock.elapsedRealtime()
            if (now - lastScrollElapsedMs < IDLE_AFTER_SCROLL_MS) {
                reportReason("WAIT_FOR_SCROLL_IDLE")
                scheduleStableRecheck()
                return@Runnable
            }
            val frame = inspectVisibleNodes(root, now) // ephemeral: no extra event-count increments
            val mode = LocalAutoTrial.state(this).mode
            val eligible = policy.maySkip(frame.analysis, mode, frame.viewport, now)
            reportReason(policy.lastReason.name)
            updateControls()
            if (eligible) {
                performForwardSkip(policy.suggestedSwipeMode)
            } else if (frame.analysis.candidates.isNotEmpty()) {
                scheduleStableRecheck()
            }
        }
        pendingCheck = callback
        handler.postDelayed(callback, STABLE_RECHECK_DELAY_MS)
    }

    private fun performForwardSkip(mode: ManualTestMode, manual: Boolean = false) {
        if (!LocalAutoTrial.canAct(this) || gestureInFlight ||
            mode == ManualTestMode.UNSPECIFIED ||
            SystemClock.elapsedRealtime() - lastGestureElapsedMs < AutoSkipPolicy.COOLDOWN_MS) return
        if (!showControls()) {
            LocalAutoTrial.disarm(this)
            return
        }
        val root = rootInActiveWindow ?: return
        if (!ProbeScope.isTargetPackage(root.packageName?.toString())) {
            stopTrialAndHide()
            return
        }
        cancelCheck()
        gestureInFlight = true
        LocalAutoTrial.attempted(this, manual = manual)
        reportReason(if (manual) "MANUAL_SWIPE_REQUESTED" else "AUTO_SWIPE_REQUESTED")
        dispatchVerticalSwipe(forward = true, mode = mode,
            onSuccess = {
                gestureInFlight = false
                LocalAutoTrial.completed(this)
                val completedAt = SystemClock.elapsedRealtime()
                lastGestureElapsedMs = completedAt
                policy.afterCompletedSkip(completedAt)
                reportReason(if (manual) "MANUAL_SWIPE_DONE" else "AUTO_SWIPE_DONE")
                undoEligible = true
                previousMode = mode
                updateControls()
                // Unlimited until STOP / app switch / revoke; keep Back available.
                // After every completed swipe the next one still requires new stable evidence.
            },
            onFailure = {
                gestureInFlight = false
                LocalAutoTrial.failed(this)
                reportReason("GESTURE_CANCELLED")
                undoEligible = false
                removeOverlay()
            }
        )
    }

    private fun dispatchVerticalSwipe(
        forward: Boolean,
        mode: ManualTestMode,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        if (width < 200 || height < 400) {
            onFailure()
            return
        }
        val start = if (mode == ManualTestMode.REELS) 0.77f else 0.72f
        val end = if (mode == ManualTestMode.REELS) 0.25f else 0.38f
        val fromY = height * (if (forward) start else end)
        val toY = height * (if (forward) end else start)
        val path = Path().apply {
            moveTo(width * 0.49f, fromY)
            lineTo(width * 0.49f, toY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 360L))
            .build()
        val accepted = dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onSuccess()
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onFailure()
                }
            },
            handler
        )
        if (!accepted) onFailure()
    }

    private fun approxBack() {
        if (!undoEligible || gestureInFlight ||
            previousMode == ManualTestMode.UNSPECIFIED) return
        val root = rootInActiveWindow ?: return
        if (!ProbeScope.isTargetPackage(root.packageName?.toString())) {
            stopTrialAndHide()
            return
        }
        // No automatic re-skip after reversing. This does NOT restore the exact post.
        LocalAutoTrial.disarm(this)
        undoEligible = false
        cancelCheck()
        gestureInFlight = true
        updateControls()
        dispatchVerticalSwipe(forward = false, mode = previousMode,
            onSuccess = {
                LocalAutoTrial.recordedBack(this)
                gestureInFlight = false
                removeOverlay()
            },
            onFailure = {
                gestureInFlight = false
                removeOverlay()
            }
        )
    }

    private fun showControls(): Boolean {
        if (overlay != null) {
            updateControls()
            return true
        }
        return try {
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8), dp(6), dp(8), dp(6))
                background = GradientDrawable().apply {
                    setColor(0xEE1F2331.toInt())
                    cornerRadius = dp(12).toFloat()
                }
            }
            val title = TextView(this).apply {
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 12f
            }
            val back = Button(this).apply {
                text = getString(R.string.trial_back_button)
                contentDescription = getString(R.string.trial_back_button)
                setOnClickListener { approxBack() }
            }
            val manual = Button(this).apply {
                text = getString(R.string.trial_manual_button)
                contentDescription = getString(R.string.trial_manual_button)
                setOnClickListener {
                    // User-requested one-swipe test bypasses candidate classification only.
                    // It still requires arming, cooldown and Facebook foreground.
                    val chosen = LocalProbeStore.snapshot(this@FacebookProbeService).manualTestMode
                    val surface = when (chosen) {
                        ManualTestMode.FEED, ManualTestMode.REELS -> chosen
                        else -> policy.suggestedSwipeMode.takeIf {
                            it == ManualTestMode.FEED || it == ManualTestMode.REELS
                        } ?: ManualTestMode.FEED
                    }
                    performForwardSkip(surface, manual = true)
                }
            }
            val stop = Button(this).apply {
                text = getString(R.string.trial_stop_button)
                contentDescription = getString(R.string.trial_stop_button)
                setOnClickListener { stopTrialAndHide() }
            }
            column.addView(title)
            column.addView(back)
            column.addView(manual)
            column.addView(stop)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dp(10)
                y = dp(100)
            }
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(column, params)
            overlay = column
            overlayLabel = title
            backButton = back
            manualButton = manual
            updateControls()
            true
        } catch (_: RuntimeException) {
            removeOverlay()
            false
        }
    }

    private fun updateControls() {
        val s = LocalAutoTrial.state(this)
        overlayLabel?.text = getString(R.string.trial_overlay_count, s.completed) +
            "\n" + getString(R.string.trial_reason_overlay, s.lastReason)
        backButton?.visibility = if (undoEligible) View.VISIBLE else View.GONE
        manualButton?.isEnabled = !gestureInFlight && s.armed &&
            (SystemClock.elapsedRealtime() - lastGestureElapsedMs >= AutoSkipPolicy.COOLDOWN_MS)
    }

    private fun removeOverlay() {
        overlay?.let {
            try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
            catch (_: IllegalArgumentException) { /* already removed */ }
        }
        overlay = null
        overlayLabel = null
        backButton = null
        manualButton = null
    }

    /** Persist one symbolic verdict for debugging; never save a Facebook node or label. */
    private fun reportReason(reason: String) {
        LocalAutoTrial.diagnostic(this, reason)
    }

    private fun cancelCheck() {
        pendingCheck?.let(handler::removeCallbacks)
        pendingCheck = null
    }

    private fun stopTrialAndHide() {
        LocalAutoTrial.disarm(this)
        inFacebook = false
        undoEligible = false
        policy.clear()
        deduper.clear()
        cancelCheck()
        removeOverlay()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onInterrupt() {
        stopTrialAndHide()
    }

    override fun onDestroy() {
        stopTrialAndHide()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val SCAN_INTERVAL_MS = 750L
        private const val STABLE_RECHECK_DELAY_MS = 1750L
        private const val IDLE_AFTER_SCROLL_MS = 1200L
        private const val MAX_NODES = 250
        private const val MAX_DEPTH = 14
        private const val MAX_CHILDREN_PER_NODE = 30
    }
}

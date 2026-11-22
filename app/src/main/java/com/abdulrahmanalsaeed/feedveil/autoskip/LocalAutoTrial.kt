package com.abdulrahmanalsaeed.feedveil.autoskip

import android.content.Context
import android.os.SystemClock
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode

/** Opt-in experiment; only aggregate numbers persist. No post media or text is saved. */
object LocalAutoTrial {
    private const val FILE = "phase3_autoskip"
    private const val ARMED = "armed"
    private const val MODE = "mode"
    private const val START = "started_elapsed"
    private const val ATTEMPTS = "attempts"
    private const val COMPLETE = "completed"
    private const val FAILED = "failed"
    private const val BACKS = "approximate_back"
    const val MAX_SKIPS = 3
    private const val SESSION_MS = 5 * 60_000L

    data class State(
        val armed: Boolean,
        val mode: ManualTestMode,
        val attempts: Int,
        val completed: Int,
        val failed: Int,
        val approximateBacks: Int
    )
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    @Synchronized fun arm(context: Context, mode: ManualTestMode): Boolean {
        if (mode == ManualTestMode.UNSPECIFIED ||
            !LocalProbeStore.isConsented(context) || !LocalProbeStore.isActive(context)) return false
        prefs(context).edit()
            .putBoolean(ARMED, true).putString(MODE, mode.name)
            .putLong(START, SystemClock.elapsedRealtime())
            .putInt(ATTEMPTS, 0).putInt(COMPLETE, 0)
            .putInt(FAILED, 0).putInt(BACKS, 0).commit()
        return true
    }

    @Synchronized fun disarm(context: Context) {
        prefs(context).edit().putBoolean(ARMED, false).commit()
    }

    fun state(context: Context): State {
        val p = prefs(context)
        return State(
            armed = p.getBoolean(ARMED, false),
            mode = ManualTestMode.fromStored(p.getString(MODE, null)),
            attempts = p.getInt(ATTEMPTS, 0),
            completed = p.getInt(COMPLETE, 0),
            failed = p.getInt(FAILED, 0),
            approximateBacks = p.getInt(BACKS, 0)
        )
    }

    @Synchronized fun canAct(context: Context): Boolean {
        val state = state(context)
        val start = prefs(context).getLong(START, 0)
        val now = SystemClock.elapsedRealtime()
        if (!state.armed) return false
        if (!LocalProbeStore.isConsented(context) || !LocalProbeStore.isActive(context) ||
            LocalProbeStore.snapshot(context).manualTestMode != state.mode ||
            state.mode == ManualTestMode.UNSPECIFIED ||
            state.completed >= MAX_SKIPS || state.attempts >= MAX_SKIPS ||
            now < start || now - start > SESSION_MS
        ) {
            disarm(context)
            return false
        }
        return true
    }

    @Synchronized fun attempted(context: Context) {
        val p = prefs(context)
        p.edit().putInt(ATTEMPTS, p.getInt(ATTEMPTS, 0) + 1).commit()
    }

    @Synchronized fun completed(context: Context) {
        val p = prefs(context)
        val count = p.getInt(COMPLETE, 0) + 1
        p.edit().putInt(COMPLETE, count).putBoolean(ARMED, count < MAX_SKIPS).commit()
    }

    @Synchronized fun failed(context: Context) {
        val p = prefs(context)
        p.edit().putInt(FAILED, p.getInt(FAILED, 0) + 1).putBoolean(ARMED, false).commit()
    }

    @Synchronized fun recordedBack(context: Context) {
        val p = prefs(context)
        p.edit().putInt(BACKS, p.getInt(BACKS, 0) + 1).commit()
    }
}

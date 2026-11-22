package com.abdulrahmanalsaeed.feedveil.autoskip

import android.content.Context
import com.abdulrahmanalsaeed.feedveil.probe.LocalProbeStore
import com.abdulrahmanalsaeed.feedveil.probe.ManualTestMode

/** Opt-in session with no fixed count/time cap; only aggregate numbers persist. */
object LocalAutoTrial {
    private const val FILE = "phase3_autoskip"
    private const val ARMED = "armed"
    private const val MODE = "mode"
    private const val ATTEMPTS = "attempts"
    private const val COMPLETE = "completed"
    private const val FAILED = "failed"
    private const val BACKS = "approximate_back"
    private const val MANUAL_ATTEMPTS = "manual_attempts"
    private const val REASON = "last_reason"


    data class State(
        val armed: Boolean,
        val mode: ManualTestMode,
        val attempts: Int,
        val completed: Int,
        val failed: Int,
        val approximateBacks: Int,
        val manualAttempts: Int,
        val lastReason: String
    )
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    @Synchronized fun arm(context: Context, mode: ManualTestMode): Boolean {
        if (mode != ManualTestMode.GENERAL ||
            !LocalProbeStore.isConsented(context) || !LocalProbeStore.isActive(context)) return false
        prefs(context).edit()
            .putBoolean(ARMED, true).putString(MODE, mode.name)
            .putInt(ATTEMPTS, 0).putInt(COMPLETE, 0)
            .putInt(FAILED, 0).putInt(BACKS, 0)
            .putInt(MANUAL_ATTEMPTS, 0).putString(REASON, "WAITING").commit()
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
            approximateBacks = p.getInt(BACKS, 0),
            manualAttempts = p.getInt(MANUAL_ATTEMPTS, 0),
            lastReason = p.getString(REASON, "WAITING") ?: "WAITING"
        )
    }

    @Synchronized fun canAct(context: Context): Boolean {
        val state = state(context)
        if (!state.armed) return false
        if (!LocalProbeStore.isConsented(context) || !LocalProbeStore.isActive(context) ||
            state.mode != ManualTestMode.GENERAL
        ) {
            disarm(context)
            return false
        }
        return true
    }

    @Synchronized fun attempted(context: Context, manual: Boolean = false) {
        val p = prefs(context)
        val edit = p.edit().putInt(ATTEMPTS, p.getInt(ATTEMPTS, 0) + 1)
        if (manual) edit.putInt(MANUAL_ATTEMPTS, p.getInt(MANUAL_ATTEMPTS, 0) + 1)
        edit.commit()
    }

    @Synchronized fun diagnostic(context: Context, reason: String) {
        val p = prefs(context)
        if (p.getString(REASON, "") != reason) p.edit().putString(REASON, reason).apply()
    }

    @Synchronized fun completed(context: Context) {
        val p = prefs(context)
        val count = p.getInt(COMPLETE, 0) + 1
        p.edit().putInt(COMPLETE, count).commit()
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

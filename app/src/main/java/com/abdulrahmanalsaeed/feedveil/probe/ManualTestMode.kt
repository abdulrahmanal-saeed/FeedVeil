package com.abdulrahmanalsaeed.feedveil.probe

/**
 * User-provided label for a manually supervised test session.
 * NEVER interpreted as automatic Facebook screen detection or ad confidence.
 */
enum class ManualTestMode {
    UNSPECIFIED,
    FEED,
    REELS,
    /** Used by the separately armed Auto Skip, never treated as a real tab detection. */
    GENERAL;

    companion object {
        fun fromStored(value: String?): ManualTestMode =
            entries.firstOrNull { it.name == value } ?: UNSPECIFIED
    }
}

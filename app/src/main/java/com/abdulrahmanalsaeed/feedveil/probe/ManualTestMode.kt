package com.abdulrahmanalsaeed.feedveil.probe

/**
 * User-provided label for a manually supervised test session.
 * NEVER interpreted as automatic Facebook screen detection or ad confidence.
 */
enum class ManualTestMode {
    UNSPECIFIED,
    FEED,
    REELS;

    companion object {
        fun fromStored(value: String?): ManualTestMode =
            entries.firstOrNull { it.name == value } ?: UNSPECIFIED
    }
}

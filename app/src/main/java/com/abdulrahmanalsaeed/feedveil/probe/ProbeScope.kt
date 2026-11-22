package com.abdulrahmanalsaeed.feedveil.probe

/**
 * Pure allowlist policy. Being permitted by this method does not grant OS Accessibility access.
 */
object ProbeScope {
    const val TARGET_PACKAGE = "com.facebook.katana"

    fun isTargetPackage(packageName: String?): Boolean = packageName == TARGET_PACKAGE

    fun mayRead(packageName: String?, consented: Boolean, active: Boolean): Boolean =
        consented && active && isTargetPackage(packageName)
}

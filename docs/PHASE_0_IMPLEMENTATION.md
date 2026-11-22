# Phase 0 — Implementation notes and manual device test worksheet

## Scope / exact status
This branch adds a **read-only Android probe**, not an ad blocker. It never dispatches a gesture, clicks Facebook controls, overlays Facebook, captures media, uses networking, or reads unrelated applications.

- `com.facebook.katana` only.
- Scans only on foreground Facebook accessibility events after both in-app consent and user-enabled system Accessibility.
- Diagnostic counters: eligible Facebook events, completed scans, exact-marker observations, last sampled node count and local scan time.
- **Every counter is per event/screen scan, NOT per unique sponsored post.**
- Exact label matching is hypothesis-only, not classified ad identification. Marker readings do not prove visible ads or post boundaries.
- All UI text is discarded immediately; only aggregate counters and consent/pause settings are persisted.
- Phase 0 does not compute recall/precision. That requires manual reference labels collected on a real device.

## Build prerequisites
JDK 17, Android SDK Platform 36, Android Build Tools 36.0.0, **Gradle 8.13** (Android Gradle Plugin 8.13.2). If the wrapper is available, use `./gradlew`; otherwise `gradle` from your local Gradle 8.13 install.

```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

GitHub Actions runs this on PR/push and uploads **FeedVeil-Phase0-Debug** containing `app-debug.apk` if the job succeeds. A debug APK can be installed on a personal Android device; allow sideloading only through normal OS protections. Never disable Play Protect or try to bypass restricted-accessibility security controls.

## Manual Phase 0 test procedure (owner-operated, no screenshot upload)
1. Open the APK and read the permission explanation. Confirm you can decline and see zero scanning.
2. Tap explicit read-only consent; enter standard Android Accessibility settings; find and enable **FeedVeil read-only probe** if the OS permits it. Do not bypass platform safety restrictions.
3. Open the native **Facebook app** (not a browser). Manually scroll a mixed sample of ordinary and visibly sponsored posts; record your visual labels privately with simple tally marks.
4. Return to FeedVeil; press **Refresh counters**. Verify scans and marker observations increase, and that FeedVeil did NOT swipe, click, mask or navigate Facebook.
5. Recheck using Arabic and English Facebook language (if possible). Verify the label observations and document the Facebook app and Android versions.
6. Pause probe inside FeedVeil; revisit Facebook and manually scroll; ensure counters **do not increase**.
7. Revoke in-app consent; ensure zero scans. Re-enable system accessibility but do **not** re-consent; ensure still no scans.
8. Check moving to other apps; counters must not increase for unrelated screens.
9. Restart/rotate device, open various Facebook screens, ensure the app never takes action and state remains safe.

### Private manual worksheet (no identifying post data)
| Date | Device / OS | Facebook version | UI language | Visible sponsored tally | Ordinary tally | Marker-observation total delta | Notes without personal data |
| --- | --- | --- | --- | ---: | ---: | ---: | --- |
| | | | | | | | |

**Caution:** Marker observations may repeat for one ad because of multiple events. Don't divide observations by visible ads and claim detection accuracy; P1 must link distinct labels to distinct post roots first.

## Phase 0 acceptance checklist
- [ ] Unit tests, lint, assembleDebug GREEN in GitHub Actions.
- [ ] APK installed and service enabled without bypassing Android restrictions.
- [ ] Only Facebook generates scans.
- [ ] In-app consent OFF prevents scanning.
- [ ] Pause stops scanning.
- [ ] Zero scroll, clicks, masks, or screen recordings.
- [ ] No personal post text, raw accessibility tree, screenshot or account data retained.
- [ ] Actual device shows whether Sponsored/ممول labels are exposed.
- [ ] Report records Android/Facebook versions, bilingual results and limitations.
- [ ] GO / CONDITIONAL GO / NO-GO documented before Phase 1.

## What cannot be validated via repository CI
CI can compile and exercise synthetic unit tests. It **cannot observe your real Facebook UI**, prove sponsor labels are exposed, or verify real-device behavior. Do not merge Phase 0 or begin automatic actions based only on build status.

## Compatibility references
- AGP 8.13: https://developer.android.com/build/releases/agp-8-13-0-release-notes
- Android AccessibilityService: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Compose Kotlin compiler: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- Google Play policy (interfering with other apps' ads): https://support.google.com/googleplay/android-developer/answer/16559646

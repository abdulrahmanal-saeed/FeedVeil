# Testing, metrics and release gates
**Version:** Planning 1.0. All targets below are proposed thresholds, NOT measured outcomes.

## 1. Test layers
1. **Unit (JVM):** rules, classification, post-boundary mapping, confidence reasons, dedupe, mode policy, cooldown, pause and edge cases with synthetic fixtures.
2. **Android instrumentation:** consent flow, service start/stop, event filtering, safe overlay lifecycle, manual action callbacks, rotation, process death, accessibility revocation.
3. **Real-device manual observation:** original installed Facebook app; manually label ads vs normal vs inaccessible/uncertain; compare UI to service classification.
4. **Compatibility:** Samsung Android 16 as first target; another Android version/device when available; dark/light, EN/AR, RTL, font sizes, feed/reels/tab, varying network speed.
5. **Privacy/security:** verify no persistent post text, screenshots, account info, raw trees or unintended network, no unbounded accessibility scope; audit APK manifest.
6. **Operational:** low event volume on irrelevant screens, battery/CPU memory profiles, user interaction and lock-screen interruptions.

## 2. Manually labeled evaluation dataset
- Labels: VISIBLE_AD, VISIBLE_NORMAL, UNCERTAIN/NOT_VISIBLE; retain only aggregate tally and synthetic fixture examples.
- Label by direct visual observation and avoid storing screenshots or private post/account content.
- **Initial evaluation target:** >=100 distinct visible sponsored items and >=300 ordinary items for each major validated feed locale when feasible. If dataset is smaller, report exploratory findings only.
- Separate **unreadable sponsored items** as failures of recall, not automatically false positives or successes.
- Log Facebook app version, date, Android version, locale, general screen type, classifier-rule version, counts. Exclude any user-identifying data.
- Verify at least one second scrolling session to reduce overfitting to a single feed snapshot.

## 3. Metrics and definitions
- **Precision (confirmed ad)** = true sponsored predictions / all positive sponsored predictions.
- **Recall (all visible sponsored)** = true sponsored predictions / all manually identified visible sponsored posts, including unreadable sponsored items in denominator.
- **Accessible-marker recall** = detections / all visually sponsored items whose marker is actually exposed; report separately from total recall.
- **Unknown rate** = unknown labels / all labeled items.
- **False auto-skip rate** = normal posts actually scrolled past because of false sponsored classification / normal posts encountered under test; count any navigation damage separately.
- **Successful skip rate** = verified moves beyond intended ad / scroll attempts; do NOT equate scrolling with blocking network ads.
- **Wrong native-hide action rate** = clicks on anything except verified Hide Ad / hide attempts (must be zero).
- **Mask leakage** = overlays remaining outside intended state or region (must be zero for release).

### Candidate gates
| Gate | Proposed acceptance |
| --- | --- |
| Read-only P0 | Sponsor evidence exposed and safely associated with a post on actual device; zero gestures |
| Detection classification | >=98% sponsored precision; report recall and unknown rate per locale/build |
| Detection usable coverage | >=70% accessible-marker recall in validated UI; lower coverage triggers manual-only or NO-GO |
| Auto Skip | <1% false auto-skip of normal encountered items, with no high-risk navigation errors |
| Hide Ad | 0 wrong-menu clicks in labeled validation set, no unconsented preference change |
| Mask | 0 stale overlays and 0 covering security/other-app UI during lifecycle tests |
| Privacy | 0 unapproved data transmissions, 0 raw sensitive-content retention |
| Stop control | 100% user pause / foreground exit / permission revoke cleanup in test matrix |

**Interpretation:** These thresholds are engineering gates, not statistical guarantees. If counts are insufficient for meaningful confidence, keep auto-action OFF. Always publish numerator/denominator, platform build, and observational limitations. Report confidence intervals where feasible. A measured 0 errors in a small sample does not prove zero future risk.

## 4. Mandatory adversarial cases
- Normal post says the word "sponsored" or "ممول" in its own text; should not auto-skip.
- Sponsored marker absent, truncated, inaccessible, in a nested inaccessible custom view → UNKNOWN.
- Repeated accessibility events and rapid swipes → max one decision, no scroll loop.
- User touches screen between detection and action → abort.
- Feed jumps, dialog opens, post menu changed, notification shade, keyboard, login prompt → abort/clear overlay.
- Facebook leaves foreground or app switches → no action/overlay.
- Rotation / font scale / screen resize / split screen / dark mode → stale coordinate detection and removal.
- UI localization changed or Facebook updated → compatibility status UNVERIFIED and auto off.
- Accessibility permission revoked, crash/restart → no lingering overlay, no silent reenable.
- Other app displays identical sponsored labels → ignored.
- Explicit consent declined → no monitoring.
- Do not confuse **Hide Ad** with **Hide Post / Report / Save**; any ambiguity aborts.

## 5. CI quality
Required: Gradle compile, unit tests, Android lint, test report artifact, dependency security audit where available, no committed secrets, no non-redacted diagnostics. On main, no broken build or failing tests should be merged.
CI debug build success does **not** validate Facebook UI behavior; real-device sign-off is a separate mandatory check.

## 6. Test report template (one per app/OS version)
- Date, branch/commit, release/debug build, device model and Android version, Facebook version, locale, UI mode.
- Classification: total ads, regular, unknown; TP/FP/FN/TN, precision, recall, coverage.
- Actions: attempted/completed/aborted, false skips, wrong menu taps, mask cleanup outcomes.
- Performance: event-processing latency and battery/CPU comparison vs service disabled.
- Deviations, known unsupported surfaces, verdict GO / CONDITIONAL / NO-GO.
- No screenshot/private account information embedded in public repository.

## 7. Release decision
A feature is released only when documentation, tests, device evidence and privacy review all agree; otherwise it stays flagged OFF, with a transparent explanation in the UI and CHANGELOG.

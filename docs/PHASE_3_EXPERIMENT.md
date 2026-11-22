# Phase 3 early experiment — supervised Auto Skip (not production)
**Branch:** `phase-3/experimental-autoskip` stacked on `phase-1/detection`.
**Reason:** owner explicitly requested actual trial automatic skipping now and a way to review what was skipped. This accelerates an opt-in *experimental* path; it does NOT conclude Phase 1 detection accuracy.

## Explicit safety conditions
- **OFF by default.** Requires Android Accessibility consent, active probe, an owner-selected Feed or Reels manual test session, then a **second explicit warning/arm confirmation**.
- **Maximum 3 gestures** and 5-minute session; 15-second cooldown after a completed skip.
- Only one plausible container with the appropriate label type is eligible. Feed expects `• Ad` metadata; Reels expects standalone `Ad`; both may accept Sponsored. Never infer safety from this heuristic alone.
- Candidate must remain in nearly the same position across two scans at least 1.2 seconds apart; if still moving or multiple candidates/unresolved markers exist: **no action**.
- Conservative geometry check excludes tiny controls, offscreen placement and oversized full-screen containers.
- UI includes a small **Accessibility overlay with STOP and approximate Back**. If overlay cannot be displayed, automatic gestures stay disabled. `Back` tries ONE reverse swipe and disarms trial.
- After 3 completed actions the trial disarms; a visible back button is retained briefly (up to 30 seconds if still within Facebook).
- Leaving Facebook, revoking consent, pausing service, or service restart disarms and removes controls.
- No INTERNET, SYSTEM_ALERT_WINDOW, screenshots, Facebook post storage or credential collection. Only aggregated attempt/completion/failure/approx-back counts persist. **No exact history of skipped posts is available.**
- Changed service event subscription to receive other apps' **event package metadata** for foreground-exit cleanup. Only `com.facebook.katana` roots can be read, never other app content.

## What the review button actually does
Android's accessibility tree does not provide a reliable browser-like history for arbitrary Facebook feed cards. `Back ~ (approx.)` makes a reverse swipe; Facebook may recycle cards or change feed order. Do not market it as Undo or claim it can restore all skipped content. A persistent screenshot/post-text history would introduce significant privacy risks, so it is deliberately omitted.

## Manual trial procedure (owner-operated)
1. Install the **successful CI Debug APK**. New debug signing might require uninstalling the previous copy and re-granting standard permission; never bypass Android's Restricted Settings protections.
2. Select **Start Feed test** or **Start Reels test** in FeedVeil to reset counters and label the experiment. Stay inside that surface; screen type still cannot be recognized automatically on the owner's device.
3. Tap **Arm 3-skip supervised trial**, read the second consent warning and confirm. Do not leave the phone unattended.
4. Open original Facebook. Observe if the overlay STOP appears. **If no STOP appears, no automatic swipes should happen**.
5. Watch every scroll: count visually whether the item was an ad or normal. Tap **Back ~** immediately after an incorrect skip (approximate only), and tap STOP.
6. Record session type, attempts, completed/failed, approximate backs, visually confirmed correct/incorrect skips, and whether the wrong content could be recovered. No private screenshots necessary.
7. Verify STOP, pause, service revocation and leaving Facebook remove overlays/stop future gestures.

## Explicit known limitations
- Phase 1's provisional candidates have **not** passed the >=98% precision threshold in `TEST_PLAN.md`. An exact `Ad` text badge is not itself confirmed to belong to the candidate post.
- The manual Feed/Reels selection may differ from the actual Facebook page; automatic selected-tab hints were 100% UNKNOWN on earlier device sample.
- A vertical gesture can interrupt natural scrolling and move past normal posts.
- Reversed scroll is **best effort** and never exact, especially Reels.
- Service / overlay behavior must be verified on Android 16 owner phone. Successful GitHub CI does not test runtime Accessibility gestures.
- Some Play distribution policies restrict autonomous accessibility controls and interfering with other apps/ads. Treat as private research/debug APK, not a Play-ready product.

## Exit criteria
- Build, unit tests, lint pass.
- Screen overlay visibly accessible and immediately stoppable.
- Device can run at most 3 automatic swipes per arm.
- No actions outside Facebook, after pause, after app exit, or without opt-in.
- Reversing swipe works only as a best-effort convenience; do not rely on it for accuracy.
- Manually labeled correct vs wrong skips and false positive analysis supplied before considering expanded automation.

**Final always-on Auto Skip remains BLOCKED pending Phase 1 detection metrics and safety review.**

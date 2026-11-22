# Phase 1 — Read-only candidate post association (preview v0.1.0)

## Why this exists
Phase 0 on the owner's Android 16 device saw 1,176 Facebook screen scans, 28 exact Ad badges, and 40 metadata • Ad observations. Those were **repeated UI node observations, not ads**. Phase 1 investigates whether an observed marker can be associated with a bounded post, without clicking, scrolling, masking, screenshotting, or storing Facebook content.

## Mechanism (conservative and imperfect)
1. Read UI tree only while app is \`com.facebook.katana\`, user has opted in, and service is active.
2. Build an **ephemeral feature graph** of visible UI nodes: structural parent index, depth, rectangle, whether any non-ad readable label exists, and *symbolic* ad marker class. Raw text is discarded immediately.
3. For each Ad/Sponsored marker, walk upward to the nearest plausible container. Reject the app root, tiny toolbars, oversized all-screen windows, containers without at least two descendants and separate readable content, and any marker outside candidate bounds. **These are heuristics, not validated post boundaries.**
4. Coalesce multiple markers inside one candidate container on **one screen scan**.
5. Across nearby scans, use a **2.5-second in-memory, geometry-only heuristic** to suppress repeated placements. Does **not** identify unique ads; two different ads at similar coordinates can be mistakenly clustered, while one ad scrolling to a new location can form multiple clusters.
6. Only if an *exact selected-tab* label for Home/الرئيسية or Reels/ريلز is exposed do we add a FEED or REELS **hint**. Unknown is normal; this is not a verified screen classifier.
7. Increment counters for provisional containers, unresolved marker nodes, recent placement clusters, repeats suppressed and per-surface-hint scan counts; store **numbers only**.

### Absolutely no automation
**PROVISIONAL does not mean verified sponsored content.** Every category remains read-only. Never pass these results directly into Auto Skip/Hide Ad/Visual Mask. We still lack precision, recall and failure-case evidence.

## New dashboard statistics
- \`Provisional post-container observations\`: candidate placements repeatedly seen; not actual/unique ad count.
- \`Markers without safe post boundary\`: possible ads that cannot be responsibly associated with a container.
- \`Recent placement clusters\`: repeated-reduction output; not deduplicated ads.
- \`Repeated placements suppressed\`: counts suppressed in that short window.
- \`Selected-tab hint scans\`: number of times selected-tab exact label provided Feed/Reels; otherwise UNKNOWN. Do not confuse with true count of Feed/Reels ads.

## Owner validation plan
- Install Phase 1 preview APK; if update fails due to debug signing, uninstall previous FeedVeil and re-enable through normal Android UI.
- Reset counters. Browse **Feed only** for several sponsored and normal posts; note visually whether ads are present.
- Return, refresh and record each Phase 1 counter.
- Reset, browse **Reels only**, repeat. Record counts even if surface hints say UNKNOWN.
- Observe behavior: **zero automated swipes, zero menu selections, zero overlay masks**.
- Pause service; ensure counts do not grow while revisiting Facebook. Revoke consent and ensure no monitoring of other apps.
- Return only aggregate numbers and Android/Facebook versions. Do not upload personal post content into public repository.

## Current known limitations / Phase 1 exit criteria
- Labels visually marked \`Ad\` may not share an accessible post subtree.
- Very large posts, videos, empty-text sponsored cards and custom Android views may remain UNKNOWN.
- Exact \`Ad\` may occur in non-ad content. **No confidence tier HIGH yet**.
- Tree geometry can be imprecise; scrolling can reuse item containers.
- Need a manually labeled evaluation set of normal posts and ads to estimate precision, recall, false positives, unknown rate and separate Feed versus Reels compatibility. No target has been achieved yet.
- To complete Phase 1, review real-device results, improve boundary strategy carefully, and run test-gate methodology in TEST_PLAN.md.

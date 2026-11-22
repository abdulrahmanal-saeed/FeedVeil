# Feature specification — classification and four action modes
**Everything below is proposed and must be validated against the real Facebook Android app.** All interventions are OFF by default; merely enabling Accessibility must not initiate gestures.

## A. Detection engine
### Signals
1. Strong: accessibility-exposed Sponsored / ممول (and tested localized equivalents) linked to a visible feed-post root.
2. Supporting: feed hierarchy bounds, nearby post menu, role, visibility, and stable scrolling container.
3. Negative: label is detached from the candidate post, outside feed, stale node, a quoted word in regular body text, or ambiguous menu context.

Classification is **AD, NORMAL, UNKNOWN**; confidence is evidence-backed, not a guessed ML probability. A substring match alone never authorizes automated actions. When hierarchy changes or no sponsor marker is accessible, output UNKNOWN. Unknown posts must not be treated as normal for recall estimates, and must never auto-scroll.

### Scope
Feed tab only during first validation. Reels/videos/Stories are separate classification domains. English/Arabic locales, RTL and accessibility text variants require separately labeled tests. Rule versions stored locally with explicit rollback plan.

## B. Auto Skip
**Purpose:** Scroll past a confirmed sponsored post, leaving regular posts untouched.
**Entry gates:** user explicitly enabled, service active, Facebook foreground, known Feed screen, HIGH-confidence AD associated with currently visible container, no interaction in progress, no previous action on this target, no overlay/modal, adequate current geometry.
**Strategy:** resolve nearest safe scrollable ancestor; choose an Android accessibility scroll action where precise enough, otherwise opt-in gesture tested for device/layout; use bounded movement and check window change afterwards. Implement configurable conservative cooldown and a hard cap on consecutive auto scrolls. No arbitrary background swiping.
**Stop conditions:** user touches/scrolls, visibility changes, uncertain classifier, layout mismatches, screen not Facebook, revoked permission, abnormal rapid events, safety cap.
**Possible outcomes:** verified moved past, unverified, failed, aborted. Never count an attempt as a successful hide.
**Privacy:** do not persist post labels.

## C. Hide Ad (native Facebook action)
**Purpose:** Use Facebook's own menu to hide a confirmed ad if the option exists.
**Entry gates:** separate user consent, proven ad and matching menu anchor, menu opened without speculative coordinates, exact verified Hide Ad entry; confirm not Hide Post, Report, Delete, etc.
**Interaction:** deterministic menu navigation and one explicit action, no blind tapping and no bulk account alterations; revalidate after every window transition.
**Risk:** this can change the user's server-side Facebook preferences, so this must have a warning and be OFF by default.
**Failure:** cannot find exact action → abandon, optionally offer manual assist; never guess.

## D. Visual Mask
**Purpose:** Cover pixels occupied by a correctly identified ad while scrolling, without changing the Facebook app.
**Implementation:** ephemeral accessibility overlay positioned using **on-screen bounds** of a confirmed ad container; low-opacity or opaque user-selected mask with label, dismiss, and optionally skip control. Avoid overlapping navigation/status bar; ensure touch behavior is intentional and test pass-through or dismissal.
**Limits:** the ad still occupies space in Facebook's own feed, and overlays can be misaligned after scroll/rotation. Do not claim it removes ad impressions or the native card.
**Lifecycle:** remove mask immediately if node disappears, foreground changes, user pauses, scroll occurs, or location validation fails. Never draw over login, payments, security prompts, or other apps.

## E. Manual Assist
**Purpose:** Give control to the user when ad detection is credible but automatic action is unsafe.
**Implementation:** small, accessible, dismissible bubble/chip, only inside Facebook and after user permission; short-lived, screen-reader-friendly, and never obstructing normal controls.
**Interaction:** button triggers one explicitly confirmed skip action; no autonomous swipe. Dismissal means no action. Provide immediate pause/disable access.
**Fallback:** if even a possible ad cannot be located with sufficient reliability, show no bubble at all.

## F. Mode coordination and safety
- User can configure feature switches separately, but **only one intervention per candidate per decision cycle**.
- Priority is deterministic: verified native Hide Ad (if expressly enabled) → verified Auto Skip → valid Visual Mask → Manual Assist → none.
- Native Hide Ad and Auto Skip must not both operate on one candidate; fallback only after a confirmed non-action, never after uncertain outcomes.
- Pause or Accessibility revoke immediately clears overlay and cancels scheduled gesture attempts.
- Ignore repeated events caused by own overlay/actions; revalidate every action against current foreground window.

## G. Statistics and disclosures
Counts: sponsored candidates, confirmed sponsored candidates, unknown items, manual assists offered/used, scroll attempts/completions, native hides, masks shown, aborted actions, misclassifications reported.
The dashboard must never call all detected items "blocked." Percentages require denominator definitions and documented sample size. Stats reset button only clears local counters.

## H. Feature-by-feature acceptance tests
- Auto Skip: bounded, no regular-item navigation in controlled scenarios, stops on user interaction.
- Hide Ad: exact option and screen match, prevents report/hide-post mistakes.
- Visual Mask: correct bounds, 100% cleaned up on app exit/stop and UI changes in testing.
- Manual Assist: user gesture required, removable, usable with RTL and TalkBack.
- Any feature: uncertainty results in no irreversible action.

# Feasibility study, decision gates, limitations and risks
**Status: Proposed. No access-tree measurements have been collected yet.**

## Why native Facebook blocking is difficult
Android sandboxes apps. An independent app **cannot delete, edit, or reflow the native Facebook feed's internal views**, and DNS/VPN blocking cannot reliably distinguish ordinary posts from sponsored content served through common infrastructure. Accessibility APIs expose only the UI semantics Facebook chooses to make accessible; they may omit the sponsored marker, expose it as partial/hidden text, or reorder nodes.

The goal is **best-effort UI assistance**, not guaranteed prevention of ad delivery or ad impression accounting.

## P0 research questions (must answer with evidence)
1. Does the native Facebook package expose labeled sponsored content in Arabic and English to an authorized AccessibilityService?
2. Which event types reliably fire during scrolling, tab changes, and RecyclerView-like re-use?
3. Can a sponsored label be associated with a **specific post container**, not an unrelated toolbar/ad label?
4. Can we identify an action-safe scroll container and location without skipping ordinary posts?
5. Is the Facebook three-dot menu accessible? Is **Hide ad** present and distinguishable from **Hide post**, **Report** or unrelated menus?
6. Are on-screen bounds of accessible nodes useful enough for an overlay without obscuring ordinary content?
7. Do Reels/feed videos expose accessible sponsor metadata? Test separately.
8. What happens across English/Arabic, dark mode, different font sizes, and Facebook updates?

## Evidence collection protocol
- Install a minimal **read-only** Android service limited to com.facebook.katana.
- Authorized user manually navigates public/non-sensitive feed items and notes visible sponsorship status.
- Classify into AD / NORMAL / UNKNOWN using only accessible text, content descriptions, class/role and hierarchy geometry.
- Store in-memory feature booleans, timing, node counts, and non-reversible salted hashes only if truly necessary; **do not persist raw accessibility trees, post text, account/profile identifiers, screenshots, or video**.
- For an on-device study, keep a separately and manually labeled count of visible ads and ordinary posts. No background recording or cloud upload.
- Repeat on the owner's real device and one other device or emulator where valid; document device/build and app version.
- Record missing sponsor labels as UNKNOWN, not as a success.

## Hard go/no-go conditions
- **GO:** accessible sponsorship markers can be matched to a bounded post reliably, and measured precision and recall gates pass on a meaningful, manually labeled sample.
- **CONDITIONAL GO:** detection adequate but auto-action geometry unreliable → Manual Assist only; Auto Skip/Hide Ad/Mask remain off.
- **NO-GO:** sponsored semantics inaccessible or false-positive rate unsafe → stop auto-action features; document blockers. Don't quietly switch to MediaProjection/OCR or root.

No real success rates should appear in the README until collected.

## Risk register
| Risk | Severity | Mitigation |
| --- | --- | --- |
| Sponsor text inaccessible/obfuscated | Critical | Read-only P0 gate; fail closed |
| Normal content mistaken for ad | Critical | HIGH-confidence required; unknown=untouched; manual fallback |
| Wrong menu clicked (hide/report/etc.) | High | Exact menu-state validation; one permitted action; no speculative tapping |
| Scroll jumps, fights user's gesture | High | Foreground + idle checks, debounce/cooldown, single bounded action |
| Overlay blocks content or taps | High | Accessibility overlay only, transparent/non-touch-intercepting, timed removal |
| Reading private screen content | Critical | Facebook-only package checks, no raw persistent diagnostics, explicit consent |
| Facebook updates change hierarchy | High | Version-specific compatibility gates, rule rollback and kill switch |
| Android/Play policy violations | High | No Play distribution assumption; review policy before any release |
| Battery/process churn | Medium | Event-driven pipeline, no background polling, lifecycle cleanup |
| Clicks alter account preferences | High | Hide Ad experimental and separately opted-in |
| Public repository exposes user info | High | Never commit captures, tokens, screenshots or personal logs |
| Automated detection looks successful but is biased | High | Blinded/manual labels and unknown bucket; precision/recall + stratified tests |

## Fallback sequence
1. Read-only status and transparent incompatibility message.
2. Human-confirmed Manual Assist.
3. Abandon auto actions until a reliable semantic signal becomes available.
4. Alternative browser-based Facebook is **explicitly outside product scope**; discuss only as a separate product decision.

## Explicit limitations
- Overlay hides pixels, not Facebook layout space or network ad requests.
- Gesture-based scrolling cannot guarantee the next item is precisely aligned.
- Native Hide Ad is dependent on Facebook UI and can change feed preferences.
- Stories, Reels and instream ad insertions may never expose suitable metadata.
- Pre-existing impressions may already have been counted before visual hiding; we make no measurement or avoidance claim.

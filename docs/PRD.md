# Product Requirements Document (PRD)
**Project:** FeedVeil | **Version:** Planning v1.0 | **Date:** 2026-11-22 | **Status:** Proposed — not yet verified on-device

## 1. Problem / intended outcome
The user wants to continue using the **original installed Facebook app** (no custom web browser and no Facebook clone) and see fewer sponsored feed items. A trustworthy companion should detect likely sponsored content without skipping ordinary friends, groups, or pages. There is no guarantee every ad is detectable. This is a best-effort UI-assistance project, **not an ad-free guarantee**.

## 2. Intended audience and platform
- Initially: personal APK for Android 16 on the user's device; Android versions outside the tested matrix are not guaranteed.
- First target: Facebook Android package **com.facebook.katana**, when foreground and unlocked. Facebook Lite, Messenger, Instagram, Facebook Web, other apps and background usage are out of v1 scope.
- Minimum SDK **tentatively API 29**; latest compatible target SDK will be selected when the project is initialized. Device and app version must be recorded in test reports.
- Localized first-party UI: Arabic and English, RTL support.

## 3. Scope of first release
**P0:** read-only accessibility feasibility probe, precise package scoping, safe on-device diagnostics.
**MVP after passing P0:** ad detection with local counters, clear opt-in, Manual Assist, plus guarded Auto Skip when precision is proven.
**Follow-up experiments:** Facebook-native Hide Ad action and Visual Mask overlay, each independently gated. Reels/videos/Stories only after separate tests.

### Feature inventory
| ID | Requirement | Priority |
| --- | --- | --- |
| F01 | Consent and clear explanation before enabling Accessibility | Must |
| F02 | Detect foreground Facebook and stop activity elsewhere | Must |
| F03 | Classify visible items: AD / NORMAL / UNKNOWN | Must |
| F04 | Arabic/English labels in configurable versioned rule sets | Must |
| F05 | Read-only diagnostic mode with redacted data | Must |
| F06 | Manual Assist: optional dismissible user-triggered skip | MVP |
| F07 | Auto Skip: user-enabled, deterministic scroll for confirmed ads | Conditional MVP |
| F08 | Hide Ad: use Facebook's own menu if discoverable | Experimental |
| F09 | Visual Mask: unobtrusive and reversible overlay | Experimental |
| F10 | Local statistics; no content retention | MVP |
| F11 | Master pause / safety fallback / accessibility off detection | Must |
| F12 | Reels/video/Story-aware handling | Research, not committed |
| F13 | User-defined false-positive feedback (local only) | Future |
| F14 | Local rule version, compatibility alerts, and manual rollback | Future |

## 4. User journeys
1. **Onboarding:** launch → see exact permissions, monitored target and limits → explicit opt-in → system Accessibility settings → return → service status visible. Declining permissions means all monitoring remains off.
2. **Passive mode:** enable read-only → open Facebook → confirmed/suspected ad classification is counted locally; user sees no unsolicited scrolls.
3. **Manual Assist:** detection with sufficient confidence → small non-blocking assist chip → user taps to skip once or dismisses → no action on dismissal.
4. **Auto Skip:** user explicitly selects auto mode → service validates current context and a confirmed ad → bounded scroll → verifies screen changed → cooldown; stop if user touches screen or ambiguity appears.
5. **Hide Ad:** user separately opts in after warnings that this may change Facebook feed preferences → menu action only when verified; no speculative taps.
6. **Visual Mask:** user separately opts in → non-interactive temporary ad-region overlay → overlay follows scroll/window changes or disappears; never obscure login/security screens.
7. **Emergency pause:** a clearly accessible stop control disables actions immediately and clears all overlays.
8. **Uninstall/disable:** app stops without altering Facebook installation or account data.

## 5. UI specification
Home: status (Service OFF / Read-only / Assisting / Auto), big pause switch, explanation of current capability; local counters: detected, skipped, hidden, masked, uncertain, and avoided false actions. Counters distinguish **detected** from **actually suppressed**.
Modes: Manual Assist (default when feasible), Auto Skip (off by default), Hide Ad (off), Visual Mask (off); mutually conflicting actions must have a documented priority, not simultaneous taps.
Diagnostics: current Facebook app version, Android version, rule version, service accessibility state, last classified outcome **without post text**; export only after deliberate user action and redaction.
Safety: confidence indication, simple rule-reset, feedback for incorrect detection, stop switch.
Accessibility: minimum touch targets, TalkBack-friendly UI, RTL layouts, plain-language disclosures.

## 6. Non-functional requirements
- Never scrape login secrets, messages, private pages, or other applications.
- Use no root, Xposed/LSPosed, VPN interception, TLS interception, or Facebook binary modification.
- Avoid network permission for MVP; optional signed rule updates require a new explicit security/privacy decision.
- Debounce accessibility events; never block the main/UI thread; drop stale nodes.
- Battery/CPU impact tracked on real devices; no arbitrary always-on polling.
- Recover from Facebook update, rotation, multi-window, keyboard, notification shade, accessibility revocation.
- Releasing actions requires successful P0 and QA gates, not just implemented code.

## 7. Out of scope / non-goals
Universal ad blocking; 100% of posts/ads; removing/reflowing Facebook views; background crawling; altering Facebook account/profile; logging into Facebook on the user's behalf; extracting cookies; bypassing platform protections; disguising app purpose; Play Store publication as an assumption.

## 8. Success definition
A user can enable the assistant knowingly; monitoring stays within Facebook; ambiguous items are untouched; demonstrated ad precision and scroll false-positive thresholds are met in labeled controlled tests; pause/disable is reliable; private content never leaves the device. See [test plan](TEST_PLAN.md).

## 9. Constraints and dependencies
The Facebook app's accessibility tree and labels change without notice. Some sponsored items may be represented as custom-drawn views with no usable text. Hide Ad options can be renamed or removed. Overlays cannot collapse/reflow Facebook's feed. Google Play's device/network abuse policy disallows apps that interfere with another app showing ads, so private distribution is the only assumed path and should still be legally reviewed.

## 10. Approval points
- **Gate A:** approve P0 evidence and detection viability before action implementation.
- **Gate B:** approve each automation/overlay separately after test evidence.
- **Gate C:** approve privacy notice, APK signing, and distribution method.

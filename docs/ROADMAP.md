# Phase-based delivery roadmap & implementation backlog
**Current checkpoint:** Documentation first. No app code has been written. Each phase requires reviewable output and explicit acceptance evidence.

## Working rules for every phase
- Branch per phase: phase-0/probe, phase-1/detection, phase-2/manual-assist, phase-3/auto-skip, phase-4/native-hide, phase-5/mask, phase-6/hardening, phase-7/ui-ux.
- Open a PR with scope, screenshots **of our own app only**, test commands/results, known limitations, and linked acceptance checklist. No automatic merges.
- CI failures block merges. On-device Facebook testing is a separate manual gate, not something CI can certify.
- **Do not start the next phase until the previous gate is met** or the owner explicitly approves a documented reduced scope.
- Preserve all four requested modes in the roadmap even if some are eventually NO-GO.

## Phase 0 — Minimal Android probe / actual feasibility
### Tasks
- P0-001: Scaffold Android Kotlin/Compose project, Gradle wrapper and safe GitHub Actions debug build (no release signing).
- P0-002: Service XML and manifest with minimum capabilities, Facebook-only scope, consent screen; no auto actions.
- P0-003: Implement event coalescing, foreground and window guards, lifecycle cleanup.
- P0-004: Build read-only node-feature inspector that never persists raw text, credentials, screenshots or account data.
- P0-005: Implement synthetic fixture tests and diagnostics dashboard, with redacted/exportable aggregate counts.
- P0-006: Produce real-device observation protocol and feasibility report for English and Arabic Facebook feeds.
- P0-007: Record Facebook version/device/locale, percentage of manually identified ads with detectable marker, list of ambiguous layouts; go/no-go proposal.
### Deliverables
APK debug artifact + read-only report + permission disclosure + short demo notes + tests.
### Exit gate
At least one real device successfully exposes **usable** candidate evidence; no inadvertent gestures or data persistence; plan for classifier evaluation is agreed. If not, terminate auto-action approach.

## Phase 1 — Classification engine (no automatic clicks)
### Tasks
- P1-001: Define safe immutable snapshot feature model and AD/NORMAL/UNKNOWN result types.
- P1-002: Build feed-post boundary resolver and versioned Arabic/English sponsored-label rules.
- P1-003: Add confidence and reason codes, strict unknown fallback, dedupe and stale-node cancellation.
- P1-004: Implement user-visible diagnostics and local counters with no raw content storage.
- P1-005: Synthetic test corpus, negative cases, false positives and rule regression tests.
- P1-006: Manually label real feed sample and calculate precision, recall, unknown rate by locale/version.
### Exit gate
Precision/recall gate in TEST_PLAN.md met; demonstrable ability to distinguish sponsor label inside the correct item.

## Phase 2 — Manual Assist (safest intervention MVP)
### Tasks
- P2-001: Small opt-in, dismissible accessibility bubble/chip with RTL & TalkBack semantics.
- P2-002: One-shot user-triggered scroll operation and safe fallback if scroll container unknown.
- P2-003: Immediate pause, overlay cleanup, debounce, window-switch handling.
- P2-004: Unit, instrumentation, and manually supervised gesture tests.
### Exit gate
No spontaneous gestures; user control always reliable; verified false action risk low.

## Phase 3 — Auto Skip (guarded)
### Tasks
- P3-001: Implement isolated deterministic ActionPolicyEngine; HIGH-confidence + visible post + idle checks.
- P3-002: Safe scroll action with constrained distance, verification, action cap and cooldown.
- P3-003: Detect user gestures/interruption without introducing unnecessary privacy-invasive monitoring.
- P3-004: Automatic shutdown on unknown states, update breakage, foreground exit.
- P3-005: Labeled real-device experiment, error analysis and rollback.
### Exit gate
Auto Skip meets normal-post false-scroll and no-navigation safety targets. Otherwise leave it disabled/manual-only.

## Phase 4 — Native Hide Ad (experimental)
### Tasks
- P4-001: Feasibility report for Facebook's menu exposure / per-locale exact label.
- P4-002: Separate warning/consent and verified menu navigation.
- P4-003: Allowlist exact action and abort on any ambiguous option.
- P4-004: Test account-preference change and stop-on-failure behaviors without touching unrelated menu actions.
### Exit gate
Zero wrong menu selections in test matrix; otherwise mark unsupported, not silently substituted.

## Phase 5 — Visual Mask (experimental)
### Tasks
- P5-001: Validate geometry for post container across font size, screen size, RTL, scrolling, overlays, system bars.
- P5-002: TYPE_ACCESSIBILITY_OVERLAY with removable localized label, accessibility semantics, non-blocking touches.
- P5-003: Ensure overlay revalidation and removal on motion/screen changes/error.
- P5-004: QA no obscured critical controls / cleanup on service exit.
### Exit gate
Correct-region masking and zero lingering overlays across lifecycle test matrix. Otherwise leave off.

## Phase 6 — Technical hardening, compatibility and release readiness
### Tasks
- P6-001: Regression across Android versions and verified Facebook releases, including RTL, large fonts, performance/battery.
- P6-002: Service crash recovery, rule compatibility safety switch, manual reset, failure handling.
- P6-003: Static analysis, dependency checks, security/privacy audit, green automated test suite.
- P6-004: Versioned changelog, reproducible debug builds and verified feature support matrix (supported / experimental / unsupported).
- P6-005: Prepare APK packaging and owner-controlled signing strategy; **final signed user-facing release is deferred until Phase 7 UI/UX approval**.
### Exit gate
Technical implementation is stable, safety checks pass, all feature flags accurately reflect validation. Core safety/consent UI works; final product visual refinement comes next.

## Phase 7 — UI/UX design system, polished experience and final personal APK
**Full specification:** [UI/UX Phase 7](UI_UX_PHASE_7.md)
### Tasks
- P7-001: UX audit, user journeys, navigation, full screen-state inventory and low-fidelity wireframes.
- P7-002: Brand direction, original app icon and adaptive splash; component library / Material 3 tokens.
- P7-003: High-fidelity screens for Dashboard, mode controls, local statistics, permissions, diagnostics, settings, privacy, about and error states.
- P7-004: Arabic RTL / English LTR, dark/light themes, font scaling and accessible screen-reader interactions.
- P7-005: Prototypes for permission education, global pause, Auto Skip consent, Hide Ad opt-in and error recovery.
- P7-006: Design unobtrusive Manual Assist bubble and removable Visual Mask variants, reflecting actual verified technical limitations.
- P7-007: Implement the approved designs using reusable Jetpack Compose components wired to real state.
- P7-008: UI/instrumentation tests, TalkBack and contrast review, device usability walkthrough, defect fixes.
- P7-009: Owner design review and final validation; produce owner-signed APK, changelog and private distribution documentation.
### Exit gate
Visual and interaction experience approved in Arabic/English, critical accessibility/safety flows preserved, no misleading blocked-ad claims, tests pass and final APK is signed by owner-controlled keys. No Google Play release assumed.

## Later research backlog (not committed)
- Reels, Stories and in-stream ads: independent P0-style feasibility + tests before actions.
- Configurable sponsor text locales beyond Arabic/English.
- Locally imported/versioned rules with signature verification (no invisible remote rule execution).
- Optional anonymized user-feedback workflow **only after new consent/privacy review**.
- Facebook Lite support only after separate package/scope validation.

## Definition of done (all tasks)
- Scoped code + automated tests + accessibility/security review.
- Logging is structured, minimal and redacted; no raw personal content.
- Relevant state transitions tested.
- Acceptance evidence linked; feature flag stays OFF if evidence absent.
- Repo docs updated with reality, not promised results.

## Change control
If sponsored markers become inaccessible or the Facebook UI changes, mark corresponding support as **UNVERIFIED/UNSUPPORTED**, turn off affected automatic actions, open an issue with redacted evidence, re-run P0/P1 on the new UI; no emergency rule guessing.

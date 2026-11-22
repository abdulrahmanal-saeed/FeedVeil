# Technical architecture (proposed, not implemented)
## Stack
- **Android:** Kotlin; Jetpack Compose / Material 3; AndroidX lifecycle; coroutines + Flow.
- **Build:** Gradle Kotlin DSL, Kotlin/AGP versions pinned at project initialization; reproducible Gradle wrapper; lint/test CI.
- **Storage:** Preferences DataStore for settings; local aggregate counters in DataStore or Room only if aggregate/history warrants it.
- **Scope:** initially a single Android app module; modularize pure logic into JVM-testable components. No backend required.
- **Package:** candidate com.abdulrahmanalsaeed.feedveil (confirm stable applicationId before publishing/signed install). Target Facebook package **com.facebook.katana** only; guard against false package matching.

## High-level event pipeline
1. Android emits **TYPE_WINDOW_STATE_CHANGED, TYPE_WINDOW_CONTENT_CHANGED, TYPE_VIEW_SCROLLED** (and limited related events where necessary).
2. **ForegroundScopeGuard** ensures currently active app is the approved Facebook package, user has granted consent, screen context is safe, and app mode is enabled.
3. **EventThrottler** coalesces events, cancels stale work, and applies rate limits (avoid endless scanning and recursion).
4. **AccessibilitySnapshotReader** reads only the required node metadata in the current window; avoids storing node references across callbacks.
5. **FeedContainerResolver** associates sponsor evidence with a bounded, currently visible post.
6. **AdClassifier** returns AD / NORMAL / UNKNOWN and evidence codes + confidence with a versioned ruleset. No ML or off-device inference in v1.
7. **ActionPolicyEngine** chooses at most one authorized action, checks user idle/foreground/duplicate event/cooldown.
8. **ActionExecutor** calls a validated node scroll action or dispatchGesture only when required, or shows a bounded accessibility overlay/assist chip; results observed and counted without claim of perfect blocking.
9. **CountersRepository** holds local aggregate stats. **SettingsRepository** stores explicit mode switches and rules version.

Event → scope guard → snapshot → container → classifier → safety policy → (0 or 1 action) → outcome/counter.

## Proposed source tree
app/src/main/
- service/FacebookAccessibilityService.kt
- service/AccessibilityConfig.kt
- monitoring/ForegroundScopeGuard.kt
- monitoring/EventThrottler.kt
- nodes/AccessibilitySnapshotReader.kt
- nodes/FeedContainerResolver.kt
- detection/AdClassifier.kt
- detection/RuleSet.kt
- detection/ClassificationEvidence.kt
- actions/ActionPolicyEngine.kt
- actions/ScrollExecutor.kt
- actions/HideAdExecutor.kt
- overlay/MaskOverlayController.kt
- overlay/ManualAssistController.kt
- data/SettingsRepository.kt
- data/LocalCountersRepository.kt
- ui/onboarding/OnboardingScreen.kt
- ui/home/DashboardScreen.kt
- ui/settings/ModeSettingsScreen.kt
- ui/diagnostics/DiagnosticsScreen.kt
- util/Redaction.kt
- res/xml/accessibility_service_config.xml
app/src/test/...
app/src/androidTest/...
.github/workflows/...

These are planning paths; do not create hollow class files before each phase is approved.

## Android integration
- Declare service with **BIND_ACCESSIBILITY_SERVICE** permission and android.accessibilityservice.AccessibilityService intent filter.
- Service metadata: **canRetrieveWindowContent** required for reading node content; **canPerformGestures** only when action phase begins; appropriate eventTypes and feedbackType; **packageNames=com.facebook.katana** where feasible.
- Declare package visibility with a narrowly scoped **queries** block if needed, not QUERY_ALL_PACKAGES.
- Node inspection: text/contentDescription, child/parent relationships, scrollable/actions, bounds; only collect features necessary for classification.
- Scroll: prefer safe, context-specific AccessibilityNodeInfo actions when supported; use **dispatchGesture** only after separately validated behavior and only while user idle. Some actions require service gesture capability.
- Visual mask: **TYPE_ACCESSIBILITY_OVERLAY** scoped to the accessibility service; no SYSTEM_ALERT_WINDOW request by default. Must not obscure sensitive dialogs or intercept unrelated input.
- Service stops overlay/diagnostics on disabled state, foreground exit, window switch, screen locked, or unexpected errors.
- For Android restrictions, onboarding instructs users to grant access only via normal OS UI, not bypass restricted-settings security controls.

## Data contracts
ClassificationResult {
  kind: AD | NORMAL | UNKNOWN,
  confidence: HIGH | MEDIUM | LOW,
  evidenceCodes: List<String>,
  nodeBounds: Rect? (ephemeral),
  scopeValid: Boolean,
  ruleVersion: String
}
ActionDecision {
  action: NONE | MANUAL_PROMPT | AUTO_SCROLL | NATIVE_HIDE | MASK,
  reasonCode: String,
  dedupeToken: opaque ephemeral identifier
}
ActionOutcome {
  attempted: Boolean,
  completed: Boolean?,
  validation: CONFIRMED | UNCERTAIN | FAILED,
  durationMs: Long
}

Only statistics aggregate across sessions; no post content or durable identifiers.

## State machine and priorities
DISABLED → PERMISSION_READY → READ_ONLY → MANUAL_ASSIST / AUTO_ACTION.
Any state → PAUSED on user switch, permission revoke, error, or incompatible UI.
AUTO_ACTION chooses **one** action per ad. Conditional precedence when user explicitly enables several: native hide only if verified + expressly allowed; otherwise bounded scroll; optional mask only when still visible; manual prompt when automatic action unsafe. Never race gestures and overlays.
A rate limiter and per-window dedupe prevent duplicate scrolls. Abort if window/token changes before action. Clear node/overlay handles on disconnect.

## Testability / CI
- Pure classifier and policy logic unit-tested with synthetic data (no user content).
- Accessibility integration tested on device/emulator with controlled fixtures and manually observed Facebook UI.
- CI runs lint, unit tests and debug assemble. **CI cannot prove compatibility with the actual Facebook feed**; on-device QA must sign off.
- Store debug APK as CI artifact, never signing secrets; signed release requires protected signing workflow.

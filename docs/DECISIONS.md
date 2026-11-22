# Engineering decisions, open questions & official references
## Decision log — 2026-11-22
| ID | Decision | Status / why |
| --- | --- | --- |
| D001 | Product name FeedVeil | Selected for current repository; trademark not verified |
| D002 | Original native Facebook app, not in-app web browser | User requirement |
| D003 | Kotlin + Compose for Android APK | Planned stack |
| D004 | AccessibilityService as first technical hypothesis | Feasibility to be tested |
| D005 | No root, VPN interception, LSPosed, or patched Facebook client | Safer/maintainable scope |
| D006 | Four candidate features: Auto Skip, Native Hide Ad, Visual Mask, Manual Assist | Requested; feasibility-gated |
| D007 | Unknown classification never causes automatic action | Safety invariant |
| D008 | Read-only probe first; no automated clicking before precision tests | Risk reduction |
| D009 | User-visible consent; no raw post data persisted or sent | Privacy invariant |
| D010 | Do not target Google Play for this ad-blocking use case | Current Play policy prohibits interference |
| D011 | Public repo contains docs/code only, no private captures/secrets | Repo currently PUBLIC |
| D012 | Scoped architecture for com.facebook.katana; Reels etc. separate research | Limit false positives |
| D013 | No promises of 100% blocking or guaranteed app support | API/third-party UI uncertainty |
| D014 | Initial minSdk 29 candidate; final target SDK at project creation | Pending implementation check |
| D015 | Phase 7 is dedicated to complete final UI/UX, brand, design system and polished Compose experience | Explicit user decision |
| D016 | Phase 6 is technical QA/readiness; final signed personal release follows Phase 7 design approval | Updated roadmap; baseline safety UI exists earlier |

## Open research questions
- Are precise sponsor labels reliably present in actual Facebook accessibility trees for Arabic and English?
- Can each label be linked to one visible feed item rather than a reused child?
- Which safe scroll approach works across a sample of Android 16 and other OS builds?
- Can Hide Ad be identified without accidental Hide Post or Report actions?
- Is non-intercepting overlay alignment stable during fast scrolling and RTL layout?
- What is a defensible compatibility strategy after frequent Facebook changes?
- Should automatic modes stay limited to the main Feed indefinitely?
- What external review, if any, is needed before distributing APK to other users?

## Current project state
- **Documentation only**; no APK, Android project, tests, model, detector, or proven supported versions.
- The first deliverable to validate the hypothesis is **P0 read-only probe**.
- Features become implemented/supported only once their phases and test gates are satisfied.
- Don't invent success statistics or cite hypothetical demos as observed behavior.

## Official primary sources
- Android AccessibilityService: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Android AccessibilityNodeInfo: https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo
- Android accessibility actions: https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo.AccessibilityAction
- Android AccessibilityService configuration and capabilities: https://developer.android.com/guide/topics/ui/accessibility/service
- Google Play Device and Network Abuse policy (third-party ads): https://support.google.com/googleplay/android-developer/answer/16559646
- Google Play AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491
- Google Play sensitive APIs policy: https://support.google.com/googleplay/android-developer/answer/16558241
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311

## Documentation review checklist
- [x] User intent and scope captured.
- [x] Four strategies specified, including non-guarantees and safety constraints.
- [x] Read-only feasibility probe defined.
- [x] Architecture and state machine proposed.
- [x] Phase backlog and exit gates proposed.
- [x] Test plan and acceptance metrics proposed.
- [x] Privacy and store policy limitations documented.
- [x] Phase 7 UI/UX scope and final release gate documented.
- [ ] Real-device sponsored marker data collected (NOT DONE).
- [ ] On-device Android build validated (NOT DONE).
- [ ] User approves proceeding to Phase 0 implementation.

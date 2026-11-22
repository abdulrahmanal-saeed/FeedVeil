# Phase 7 — UI/UX Design System & Product Experience
**Status:** Planned / not designed yet.  
**Owner-approved phase:** Yes — UI/UX is **Phase 7**, after core functionality and technical hardening.  
**Scope:** FeedVeil app screens and accessibility overlays only, **not redesigning Facebook's own UI**.

## Goal
Deliver a coherent, polished, modern Android app that makes the status of protection, control over actions, privacy/accessibility consent, and real-world limitations understandable. Turn the minimal functional screens built during Phases 0–6 into a production-quality end-user experience.

This is **the dedicated visual design phase**, but basic usable, accessible permission and safety screens are required in earlier phases. Do not block safe onboarding or emergency stop until Phase 7.

## 7.1 Product experience principles
1. **Clarity over marketing:** say "detected" or "skipped"; never show "blocked" if an ad was merely recognized or visually covered.
2. **User control:** global pause switch always obvious; no automation by default; separately explain and opt in to actions that can change Facebook state.
3. **Honest confidence:** unsupported and unverified modes look disabled and explain why; no fabricated success percentages.
4. **Privacy first:** show why Accessibility is required and what data is/ is not retained; no deceptive dialogs.
5. **Android-native experience:** Material 3, responsive layout, smooth but restrained motion, system dynamic color where appropriate.
6. **Bilingual from the start:** Arabic and English, accurate RTL mirroring and localization of icon placement/text.
7. **Accessible to all:** TalkBack labels, semantic ordering, 48dp touch targets, scalable typography, contrast, reduced-motion options where practical.
8. **Overlay restraint:** assist chip/mask must not obscure content or trap clicks; immediately removable.

## 7.2 Information architecture / navigation
### Main navigation
- **Home / Dashboard:** live state, service access status, global master switch, mode summary, local counters, compatibility status, quick diagnostics.
- **Protection Modes:** Manual Assist, Auto Skip, Hide Ad, Visual Mask cards with safety explanations, dependencies and independent user-controlled toggles.
- **Activity & Stats:** detected vs attempted vs verified skipped vs native hidden vs masked vs unknown; timeframe and reset; disclaimers about unverified outcomes.
- **Settings:** language, theme, accessibility permission instructions, haptics/notifications (only if justified), safety & timeout defaults, rules version/reset.
- **Help / About / Privacy:** onboarding replay, capabilities/limitations, disclaimer, local data deletion, issue reporting, project/version.

### Special flows
- Welcome / onboarding (purpose, warnings and suitability).
- Permission education and OS settings handoff.
- Rejected/disabled/restricted service state.
- Detection unsupported / incompatible Facebook build state.
- Mode-specific opt-in confirmation (Hide Ad changes feed preferences, Auto Skip gestures, masking overlays).
- Emergency stop and errors / recovery.
- Offline-first information: no account sign-in.

## 7.3 Required screen inventory
| Screen | Must show | Critical states |
| --- | --- | --- |
| Splash/launch | branded launch without ad claims | first install, returning |
| Onboarding | value, limitations, data disclosure | accept, decline |
| Accessibility setup | simple exact permission explanation | off, granted, restricted, revoked |
| Dashboard | status, pause, mode, meaningful metrics | inactive, read-only, ready, active, paused, unsupported, error |
| Mode settings | separate controls for all four features | available, experimental, blocked by missing validation |
| Mode detail | description, consent warning, safe use | enabled, unavailable, requires permission |
| Statistics | accurate local aggregates | empty, populated, reset confirmation |
| Diagnostics | rule/build/version info, redacted export | compatible, unverified, incompatible |
| Privacy & about | independent-project disclaimer, privacy details | local data wipe |
| In-Facebook manual chip | dismiss or one-shot user action | display, moving, timeout, dismiss |
| In-Facebook visual mask | obvious, temporary mask that can be dismissed | visible, scrolling, removed |
| Recovery/error | plain text and next safe step | inaccessible node, revoked permission |

## 7.4 Visual direction
- Brand: **FeedVeil**; avoid Facebook's logos or confusing third-party trademark styling.
- Mood: calm, minimal, premium, practical, clearly different from Meta.
- Design tokens to define: primary/secondary/surface/error/success/warning; typography scale; radii; 4/8pt spacing grid; elevations; iconography; state and status colors.
- **No final color palette approved yet**; choose palette via design exploration and contrast testing, not fixed without review.
- Icon: an original geometric shield/feed motif; provide adaptive icon, monochrome icon, light/dark, splash.
- Material 3 components: NavigationBar, Cards, Switches, Dialogs, Snackenbars, Buttons, accessible Chips.
- Support light and dark mode; tablet/foldable layout is a later enhancement unless required by actual user test.

## 7.5 Specific UX decisions
### Dashboard
Use clear service status, not a fake "Ads blocked" hero. Big pause action, then mode(s) and 4 honest counters. Give one-tap route to system permission if off. Display Facebook compatibility and last verified version, or "Not tested / incompatible".
### Mode toggles
Each mode gets definition, expected behavior, limitations, risk level, and availability. If two enabled modes conflict, policy engine arbitrates; explain priority in UI. Automatic modes must not silently enable themselves.
### In-Facebook elements
Assist chip: small, dismissible, visible only for confirmed candidate, responsive to RTL and user focus; one gesture only upon user tap. Mask: constrained to validated bounds, branded and removable; never draw over login/security; recompute/clear when feed moves. No counterfeit Facebook-looking buttons.
### Motion & feedback
Use understated animations, vibrations only when useful and configurable. Avoid distracting notifications. Provide brief inline explanations for aborted actions.
### Error states
Don't show raw exception text. Give service off, Facebook updated, unsupported layout, no ad found, or action canceled, with clear recovery steps. When uncertain, explain that no action was taken.

## 7.6 Wireframes / prototypes / implementation deliverables
1. **User journeys:** flows for onboarding, manual assist, auto skip, hide action, masking, pause, diagnostics.
2. **Low-fidelity wireframes:** screen hierarchy and UX logic before colors.
3. **High-fidelity designs:** all critical screens/states in both light and dark mode; Arabic RTL and English LTR.
4. **Design system:** component library and semantic tokens: typography, spacing, colors, motion, iconography, accessibility states.
5. **Interactive prototype:** clickable navigation and key permission/disable flows (in design tool, when available).
6. **Android implementation:** Compose components bound to real service/settings state, no hardcoded or invented results.
7. **Assets:** custom adaptive launcher icon, splash, matching notification icon only if ever used, vector illustrations as justified.
8. **UX research:** scenario walkthrough, usability issues log, test fixes.
9. **Handoff:** screen-to-component map, string resource list, UI tests, and snapshots from our own app only.

**Deliverables may be kept as Markdown specifications, source-controlled visual assets, Compose previews, or a linked external design file after owner authorization.** Do not promise the ability to create editable Figma files without that tool/workflow actually being used.

## 7.7 Implementation backlog
- P7-001: UX audit of working features (Phase 0–6) and issue inventory.
- P7-002: Define complete app information architecture and screen-state matrix.
- P7-003: Make low-fidelity wireframes and review flow.
- P7-004: Create brand direction, custom icon, and accessible design tokens.
- P7-005: Design polished mobile screens in Arabic/English, light/dark.
- P7-006: Prototype critical flows (permission, toggles, error/stop).
- P7-007: Implement reusable Compose UI components and navigation.
- P7-008: Integrate accurate service state, feedback and metrics with UI.
- P7-009: Design/test non-invasive manual chip and mask variants against verified geometry.
- P7-010: Accessibility & responsiveness audit: TalkBack, contrast, font scale, RTL, touch size, orientation.
- P7-011: Conduct user usability walkthroughs and fix blocking UX issues.
- P7-012: Update docs, deliver design handoff and screenshots **of our app only**.
- P7-013: Prepare owner-controlled signed APK, changelog, hashes and private release documentation after final regression & approval.

## 7.8 Acceptance criteria / exit gate
- No placeholder counters presented as real, and no unsupported mode presented as working.
- All screens and major states have intentional UI and Arabic/English strings with correct RTL/LTR.
- Complete consent/offboarding and emergency stop flows; permission denial is handled.
- Supports minimum accessibility requirements: readable contrast, font scaling, TalkBack order and labeled controls, minimum touch targets.
- Overlay components are safe/dismissible and do not obscure unrelated apps or sensitive UI.
- No UI triggers irreversible/automatic actions without opt-in and policy check.
- Automated Compose UI tests pass where applicable, plus manual device usability walkthrough.
- Owner reviews visual design, approves final integrated UX, and only **then** is the personal APK release considered complete.

## Interfaces with earlier phases
Phases **0–6** may use minimal Material 3 UI for diagnostics, settings, consent, and reliable safety controls. Phase 7 consolidates aesthetics and interaction patterns, improves usability, and finalizes the UI. The *technical* test/hardening work remains in Phase 6. The *final user-facing signed release* happens at the end of Phase 7.

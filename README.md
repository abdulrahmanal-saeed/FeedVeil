# FeedVeil

> **Planning stage — no Android application code has been implemented yet.**

**FeedVeil** is a proposed, privacy-first Android companion that attempts to **detect and reduce sponsored posts in the original Facebook Android app**, without root, repackaging, reverse engineering Facebook binaries, or a built-in browser.

**Status:** Documentation / feasibility-first project (2026-11-22). All features below are **planned experiments, not confirmed capabilities**.

## Vision and constraints

- Target the **installed Facebook app** (`com.facebook.katana`) while the user is actively using it.
- Try Android's **AccessibilityService** to analyze accessible UI nodes, not encrypted network traffic.
- Treat ordinary posts as more important than skipping a suspicious ad: **do nothing when uncertain**.
- Start **read-only**; automated actions require proven detection, opt-in, and a user-accessible emergency stop.
- On-device only by default. No Facebook passwords, session cookies, post bodies, raw accessibility trees, screenshots, or account data collected.
- Accessibility APIs **cannot remove or rewrite Facebook's own feed layout**. Performance and compatibility depend on the current Facebook UI and Android version.
- **Google Play is not a viable distribution assumption**: Play policy explicitly prohibits apps that block or interfere with another app displaying ads. Plan for personal testing / private distribution subject to review.
- This repository is currently **public**; never commit user data, test screenshots, keys, or sensitive diagnostics.

## Planned features (subject to feasibility gates)

| Feature | Intended behavior | Default initially |
| --- | --- | --- |
| Sponsored-content detection | Classify visible content using accessible labels + UI context | Read-only |
| Auto Skip | Cautiously scroll past confidently detected ads | Off |
| Hide Ad | Try the native Facebook "Hide ad" action when discoverable | Off |
| Visual Mask | Non-destructive, removable overlay over recognized ad bounds | Off |
| Manual Assist | Show a dismissible action to let the user skip a detected ad | Off until validated |
| Arabic / English | Detect sponsored labels and support bilingual app UI | Planned |
| Local statistics | Count validated detections/actions without storing post text | Planned |
| Kill switch | Pause everything immediately | Required before auto actions |

## الخطة بالعربي

[اقرأ الخطة التنفيذية الكاملة بالعربية](docs/PLAN_AR.md) — تشمل المميزات الأربع، مراحل التطوير، الاختبارات والخصوصية.

## Documentation index

1. [Product requirements, scope, user experience](docs/PRD.md)
2. [Feasibility gates and risks](docs/FEASIBILITY.md)
3. [Architecture, modules, event pipeline](docs/ARCHITECTURE.md)
4. [Detection and the four action modes](docs/FEATURE_SPEC.md)
5. [Milestones, deliverables, detailed backlog](docs/ROADMAP.md)
6. [Testing methodology and acceptance metrics](docs/TEST_PLAN.md)
7. [Privacy, security, permissions, Play policy](docs/PRIVACY_AND_POLICY.md)
8. [Decision log and official references](docs/DECISIONS.md)
9. [**Phase 7 — final UI/UX design system & experience**](docs/UI_UX_PHASE_7.md)

**Roadmap:** Phases 0–6 cover feasibility, features and technical hardening. **Phase 7 is dedicated to final UI/UX design**, Arabic/English screens, design system, prototype, Compose implementation and final signed personal APK (after owner review). Earlier phases still include minimal, safe permission and stop interfaces.

## Immediate next step

**Phase 0 only:** build a minimal read-only Android accessibility probe; verify whether the Facebook app exposes enough information to reliably distinguish sponsored from ordinary posts on real devices. Do **not** implement auto-skip, menu clicks, or a masking overlay before the feasibility report and test evidence.

## Development convention

- One phase per branch / PR; tests and acceptance criteria documented in the PR.
- No premature claim of ad-blocking effectiveness.
- If accessibility semantics are insufficient, retain a manual-assist-only fallback or stop the project; do not silently introduce screenshot capture, root, VPN, or covert access.
- Documentation is authoritative until implementation starts.

**Not affiliated with, endorsed by, or sponsored by Meta or Facebook.**

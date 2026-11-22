# Privacy, security, accessibility permission, distribution
**Important:** this is a product-engineering plan, not legal advice.

## 1. Privacy-by-design
FeedVeil is intended to work entirely **on device** using Facebook's accessible UI metadata. Do not collect/store/transmit:
- Facebook credentials, cookies, tokens or account identifiers;
- raw accessibility node dumps or message/post text;
- images, screenshots, audio or screen recordings;
- browsing history, contact graphs, private chat content;
- analytics identifiers, advertising IDs or personal profiles.

Allowed local data: user-selected settings, aggregate counters and current compatibility/rule version. Runtime node bounds and classification evidence are **ephemeral** only and destroyed after a decision. Any diagnostics exported for developers must be opt-in, redacted and reviewed on device before sharing. No default cloud telemetry, crash SDK uploading view content, or backend.

## 2. Explicit onboarding disclosure
Before user opens Android Accessibility settings, clearly present a notice such as:

> FeedVeil asks for Android Accessibility access to examine UI elements **only when you are using the original Facebook app** and, if you choose, to display a manual action or execute a skip. Accessibility can potentially read screen contents and perform gestures. FeedVeil does **not** collect your Facebook password, save the content of posts, or send the content off-device. You can pause or disable the service at any time. Detection and ad reduction are not guaranteed. Automatic modes are initially off.

Obtain an explicit informed action from user **before** directing them to system permission controls. The user must be able to reject/disable without dark patterns or disguised system prompts.

## 3. Android permissions / technical controls
- AccessibilityService protected by system BIND_ACCESSIBILITY_SERVICE, configured by dedicated metadata; user enables it through OS settings.
- Use package filter **com.facebook.katana** and verify foreground context on every action, not just once at service startup.
- Request only capabilities actually used in current phase: content retrieval for probe; gesture performance when automated/user-triggered scroll is implemented.
- Prefer TYPE_ACCESSIBILITY_OVERLAY for the optional masking/assist UI. Do not request blanket overlay/system-wide display permissions unless a future separately justified design truly requires them.
- No root, accessibility security bypasses, hidden settings changes, TLS interception or stealth activation.
- Don't use **isAccessibilityTool=true** unless the app genuinely qualifies as an assistive technology for people with disabilities. This project is not represented as such.
- No INTERNET permission needed in MVP. If introduced later, privacy documentation, threat model and consent must change.
- Preserve OS restricted-settings safeguards; no workaround instructions or forced enablement.

## 4. Security model
Threats: unintentionally reading private UI, automation harming account settings, malicious rules, developer keys leaking from public repo, UI overlays imitating Facebook, and false-positive scrolling.
Mitigations:
- Allowlisted package and explicit current-window checks; deny outside target and on sensitive windows.
- Strict safe-by-default policy; UNKNOWN means no gesture.
- Separate consent for native-hide actions that could change preferences.
- Rule updates as data only, locally reviewed and versioned; do not load executable scripts from untrusted network content.
- Keep overlays obviously branded FeedVeil and dismissible; never imitate Facebook/system dialogs.
- Protect Android signing keys offline or GitHub encrypted secrets; never commit them.
- Keep public repo free of user screenshots, account names, raw node dumps, tokens, logs, signing artifacts.
- Dependency pinning, least-privilege manifest, Gradle verification and CI scanning as project grows.

## 5. Google Play policy reality
Google Play's **Device and Network Abuse** policy explicitly lists **apps that block or interfere with another app displaying ads** as prohibited. Separately, AccessibilityService usage has additional disclosure and eligibility rules. Therefore **do not plan Google Play distribution for this use case**, even if technical implementation works. No attempt to mislabel or hide its purpose. Changing distribution is a separate owner/legal decision subject to then-current policy.

Relevant policy references:
- https://support.google.com/googleplay/android-developer/answer/16559646
- https://support.google.com/googleplay/android-developer/answer/10964491
- https://support.google.com/googleplay/android-developer/answer/16558241
- https://support.google.com/googleplay/android-developer/answer/10144311

## 6. Relationship to Meta/Facebook
Independent tool, unaffiliated with Meta. Do not use Facebook logos, impersonate brand assets or claim endorsement. Independently review Meta terms, Android distribution/platform terms and regional legal considerations before broader distribution. Technical access through accessibility does not grant permission to scrape or redistribute third-party content.

## 7. Distribution and release checklist
- Personal/private APK distribution only as starting assumption; sharing outside the owner needs renewed policy/terms review.
- Use user-controlled signing keys; reproducible build instructions, checksum and release notes.
- Provide onboarding permission rationale, plain-language privacy policy, in-app disable button, bug reporting with no personal data.
- Explicitly disclose that it cannot eliminate ad delivery, is not guaranteed after Facebook updates, and has potential interaction risks.
- Make app removal straightforward, no persistent device changes.
- Document unsupported devices/versions, end-of-support and rule rollback behavior.
- Publish test results without sensitive source material.

## 8. Data retention
Settings remain until local app data is cleared/uninstalled. Aggregate counters can be reset at any time. Transient UI data has no persistence. If user opts into redacted diagnostics, export is created only on explicit action and user chooses its destination; never uploaded automatically.

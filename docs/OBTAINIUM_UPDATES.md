# FeedVeil: GitHub Releases → Obtainium updates

## Purpose and status
This is the **update distribution pipeline**, separate from Facebook detection. On a qualifying push to the active development branch (currently phase-1/detection; later main), GitHub Actions will run automated tests, build a **signed APK**, verify its signature, and publish it as a GitHub prerelease. Obtainium watches the Releases feed for new versions and can notify the phone.

**Current setup status:** The workflow is committed, but publication is safely skipped until the owner installs **four GitHub Actions signing secrets**. A valid permanent signing key is necessary. No secrets, screenshots or Facebook data should be committed or pasted into chat.

## Why our old APKs cannot update reliably
The earlier Phase 0 / Phase 1 APKs were debug builds from CI. CI runners can have different debug signing certificates. Android will **not install an update over an app signed by a different key**, even if the package ID is the same. Android also needs the installed versionCode to be lower than the new versionCode.

**One-time migration:** When the first permanent-key-signed GitHub Release is available, uninstall the old CI debug-installed FeedVeil APK, then install the signed one through Obtainium. This clears old local counters and permission/consent state. Grant Accessibility normally again if required. **After that**, upgrades signed with the same permanent key and increasing versionCode should install over the existing application without a data wipe. Never disable Android security settings or bypass safeguards.

## Step 1. Generate and safeguard a key ON YOUR OWN WINDOWS PC
Do not request signing files or passwords from assistants.

Install a JDK (Java 17+) if keytool is not already installed. Open PowerShell and run:

    keytool -genkeypair -v -keystore "$HOME\FeedVeil-release.jks" -storetype PKCS12 -alias feedveil -keyalg RSA -keysize 3072 -validity 10000 -dname "CN=FeedVeil, OU=Android, O=Personal, C=AE"

Choose a strong password when prompted. Use alias feedveil. PKCS12 ordinarily uses the same password for keystore and private key; keep track if your environment prompts for both.

Store the .jks file and password securely in private, backed-up storage outside the GitHub repo. Losing them breaks your ability to publish future updates under the same identity. Anyone possessing them could impersonate your app updates.

## Step 2. Add four GitHub Actions repository secrets
Navigate to:
https://github.com/abdulrahmanal-saeed/FeedVeil/settings/secrets/actions

Use "New repository secret" to add each of the following:

| Secret name | Value |
| --- | --- |
| FEEDVEIL_KEYSTORE_BASE64 | Base64 representation of the entire .jks file, NOT a path |
| FEEDVEIL_KEYSTORE_PASSWORD | Your keystore password |
| FEEDVEIL_KEY_ALIAS | feedveil |
| FEEDVEIL_KEY_PASSWORD | Password of the signing key (often same as keystore) |

To copy the base64 content into your Windows clipboard without creating a plaintext file:

    [Convert]::ToBase64String([IO.File]::ReadAllBytes("$HOME\FeedVeil-release.jks")) | Set-Clipboard

Paste the clipboard into the FEEDVEIL_KEYSTORE_BASE64 GitHub secret, and remove it from clipboard when done. Do **not** share secret values in chat, public issues, screenshots, or source files.

The publisher is set up to skip its release stage gracefully if any secret is missing. Regular debug CI remains independent.

## Step 3. First release from GitHub
When all 4 secrets are configured, the next source change pushed to phase-1/detection (or main after the app is merged) will trigger:
1. Test and lint verification.
2. Signed release build with **the same permanent keystore on every run**.
3. Android APK signature validation.
4. A prerelease tagged like v0.1.1 with one APK named **FeedVeil.apk** and one **FeedVeil.apk.sha256** verification file.
5. Automatic versionCode increment and new release notes.

The release job runs only for source/build/workflow changes, not README/document edits. No failed build should publish an APK.

Release history: https://github.com/abdulrahmanal-saeed/FeedVeil/releases
Workflow: https://github.com/abdulrahmanal-saeed/FeedVeil/actions/workflows/obtainium-release.yml

## Step 4. Add to Obtainium (once the first GitHub prerelease exists)
1. Open Obtainium → **Add App**.
2. Paste repository URL: https://github.com/abdulrahmanal-saeed/FeedVeil
3. Select GitHub source if it is not detected automatically.
4. **Include prereleases** / enable receiving development releases (the exact option wording varies by Obtainium version).
5. Set **APK filename filter** to: ^FeedVeil\.apk$
6. Leave **Track only** off. Leave **Verify Latest Tag** off for development prereleases, since GitHub prereleases are not the latest stable release.
7. Add the app and install the first owner-signed version. If an old debug-signed FeedVeil is already installed, uninstall that old APK first.
8. In Obtainium Settings → **Updates**, enable "Check on start", choose a background update check interval, and allow update notifications.

Obtainium can automatically check and download according to its settings, but normal Android may still require **your approval to install** an APK. Do not promise silent installations.

## How automatic versioning works
- Default debug app build: v0.1.0-phase1-preview, code 3 (local development).
- Release job: versionName is 0.1.N, tag is v0.1.N, where N is GitHub's workflow run number.
- Release versionCode is **100000 + N**, monotonically increasing for this release workflow.
- One APK in each release prevents incorrect selection.
- Before a new stable/public track, update the version strategy carefully to ensure that versionCode never decreases.
- Future phase branches are intentionally not on the auto-release allowlist. Update the workflow's push branch filters when starting a new phase to avoid publishing old branches as newer versions.

## Security and privacy
- No INTERNET permission, in-app updater SDK, tracking, login, VPN, or new Accessibility privileges were introduced into the Android app.
- All update communication happens through GitHub Actions and Obtainium itself, outside FeedVeil.
- Github repository is public. Dev APK releases will also be public; keep signing material and personal diagnostics private.
- The Android app's original package name remains unchanged.
- No Facebook auto-scrolling, hiding, overlays, or P1 precision claims are enabled by this release system.
- The publisher never generates or rotates the signing key on its own.
- The workflow refuses to replace an existing APK/tag on reruns.

## Troubleshooting
| Symptom | Check |
| --- | --- |
| No release published | All four secrets configured? Is the publish job skipped? Was the changed file in the trigger paths? |
| Obtainium shows no app | First prerelease actually exists? Include prereleases enabled? |
| Obtainium sees ZIP but no APK | Filter must match FeedVeil.apk, not the CI artifact ZIP |
| Android says update incompatible | Old debug certificate vs permanent signed certificate; **one-time** reinstall required |
| Android reports downgrade | versionCode did not increase; inspect Android APK metadata |
| Updates are not checked | Obtainium update notification/background settings |
| Gradle or signing fails | Check actions logs without posting secret material |

## References
- Obtainium sources: https://wiki.obtainium.imranr.dev/sources/
- Obtainium app tracking: https://wiki.obtainium.imranr.dev/app_tracking/
- Android application signing: https://developer.android.com/studio/publish/app-signing
- Android app updates: https://developer.android.com/google/play/app-updates

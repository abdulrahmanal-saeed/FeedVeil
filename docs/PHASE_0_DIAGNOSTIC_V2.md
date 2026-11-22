# Phase 0 diagnosis and v0.0.2

Owner screenshot evidence (22 Nov 2026): visible ads marked Ad or • Ad in Facebook Feed and Reels; first probe received 218 scans and recorded zero Sponsored / ممول labels.

Diagnosis: original observer omitted exact Ad and separated metadata variants. Whether the Facebook accessibility tree itself exposes those labels remains unverified. Screenshots alone do not prove tree accessibility.

Changes: observe exact Ad, Advertisement, إعلان, Sponsored, ممول, and separated metadata segments like Yango Drive • Ad. Display 3 independent marker counters plus number of text and description nodes on the last scan. No ad classification and no unique ad counting. All user content is discarded immediately; no network or screenshot capture.

Retest: install new APK, reset counters, visually hold an Ad post in original Facebook for several seconds, repeat in Reels, return and refresh. Record numeric counters. If debug signing differs, uninstall old FeedVeil and reinstall; Android may require normal accessibility permission setup again. Never bypass device security restrictions.

Interpretation: if new Ad counters increase, Facebook exposes some markers; Phase 1 must link them to the correct post. If text nodes exist but all marker counters stay zero, labels may be absent or exposed in another representation. Any candidate observations can be false positives. Do NOT use these heuristics for automatic scrolling/hiding.

Use issue #2 for redacted numerical reports; do not upload raw Facebook content into the public repository.
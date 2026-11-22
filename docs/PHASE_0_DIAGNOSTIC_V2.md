# Phase 0 diagnosis and v0.0.2

Owner screenshot evidence (22 Nov 2026): visible ads marked Ad or • Ad in Facebook Feed and Reels; first probe received 218 scans and recorded zero Sponsored / ممول labels.

Diagnosis: original observer omitted exact Ad and separated metadata variants. Whether the Facebook accessibility tree itself exposes those labels remains unverified. Screenshots alone do not prove tree accessibility.

Changes: observe exact Ad, Advertisement, إعلان, Sponsored, ممول, and separated metadata segments like Yango Drive • Ad. Display 3 independent marker counters plus number of text and description nodes on the last scan. No ad classification and no unique ad counting. All user content is discarded immediately; no network or screenshot capture.

Retest: install new APK, reset counters, visually hold an Ad post in original Facebook for several seconds, repeat in Reels, return and refresh. Record numeric counters. If debug signing differs, uninstall old FeedVeil and reinstall; Android may require normal accessibility permission setup again. Never bypass device security restrictions.

Interpretation: if new Ad counters increase, Facebook exposes some markers; Phase 1 must link them to the correct post. If text nodes exist but all marker counters stay zero, labels may be absent or exposed in another representation. Any candidate observations can be false positives. Do NOT use these heuristics for automatic scrolling/hiding.

Use issue #2 for redacted numerical reports; do not upload raw Facebook content into the public repository.

## Actual device findings — 2026-11-22
The owner tested **v0.0.2** in native Facebook with visible ad examples from Feed and Reels and shared the on-device aggregate counters:

| Metric | Value |
| --- | ---: |
| Screen scans | 1,176 |
| Facebook events | 1,176 |
| Sponsored / ممول observations | 0 |
| Exact Ad / إعلان badge observations | 28 |
| • Ad metadata observations | 40 |
| Nodes inspected (last scan) | 43 |
| Text nodes (last scan) | 5 |
| Content description nodes (last scan) | 14 |

**Conclusion:** real device feasibility signal present. At least some textual ad-label candidates are accessible, unlike the old exact-Sponsored-only probe. **Conditional GO** to Phase 1 *research* into reliable ad/post association and separate Feed/Reels surfaces; not yet an auto-action authorization. Exact counts are event observations, **not distinct ad counts**. The last-scan text/node numbers describe one scan only and don't measure all views. Precision, recall and false positives are unknown. Do not merge or release automatic interaction based on this screenshot alone. See [Issue #2](https://github.com/abdulrahmanal-saeed/FeedVeil/issues/2).

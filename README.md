# otel-kotlin-min-agp-repro

Minimal Kotlin Multiplatform app demonstrating that **`opentelemetry-kotlin`'s declared minimum
Android Gradle Plugin version (8.0.0) does not build** when paired with the library's required
**Kotlin 2.0** at its declared **`minSdk 21`**. Bumping the minimum AGP to **8.0.1** fixes it.

## TL;DR

`opentelemetry-kotlin` declares these consumer minimums:

| | declared minimum |
|---|---|
| AGP | **8.0.0** |
| Gradle | 8.0 |
| Kotlin | **2.0.0** |
| `minSdk` | **21** |
| `compileSdk` | 34 |

This app is pinned at exactly those floors — and `./gradlew build` **fails**:

```
> Task :app:androidApp:dexBuilderDebug FAILED
ERROR: D8: com.android.tools.r8.internal.yw
```

## Root cause

The dexer bundled in **AGP 8.0.0 is R8 8.0.35**. Below **API 24**, D8 rewrites Kotlin's default/static
interface methods (and the affected classes' `@kotlin.Metadata`) so they run on older runtimes. **Kotlin 2.0
bumped the metadata format version**, and R8 8.0.35 — released May 2023, a year *before* Kotlin 2.0 — can't
parse it, so the rewrite throws. At `minSdk ≥ 24` those methods are native — no rewrite, no crash.

## Reproduce

Requires **JDK 17** — Gradle 8.0.x runs on JDK ≤ 19 and AGP 8.0.x requires JDK 17.

```bash
./gradlew build
# → FAILS at :app:androidApp:dexBuilderDebug with  ERROR: D8: com.android.tools.r8.internal.yw
```

> Pressing the ▶ Run button in Android Studio might still work — it runs dex for the
> target device, so if the device is **API ≥ 24** it avoids the crash; an API 21–23
> target fails like the CLI.

Now apply the fix — in [`gradle/libs.versions.toml`](gradle/libs.versions.toml):

```diff
-agp = "8.0.0"
+agp = "8.0.1"
```

and rebuild:

```bash
./gradlew build
# → BUILD SUCCESSFUL
```

## Keeping AGP 8.0.0 (uncommon hack)

A project *could* stay on AGP 8.0.0 and still build — by forcing a newer R8 onto the
buildscript classpath, out-resolving the bundled 8.0.35. In the root
[`build.gradle.kts`](build.gradle.kts) `buildscript {}` block:

```kotlin
dependencies { classpath("com.android.tools:r8:8.2.47") }
```

The newer R8 parses Kotlin 2.0's metadata, so it dexes cleanly with AGP still at 8.0.0
(verified). But overriding R8 is a non-obvious hack few consumers would reach for — so it
doesn't make 8.0.0 a reasonable minimum to advertise.

## Recommendation for `opentelemetry-kotlin`

Bump the documented **minimum AGP from 8.0.0 → 8.0.1**, keeping `minSdk 21`. It's a same-month
(May 2023), non-breaking patch that drops **zero** Android-version users and excludes only the single
broken AGP release. Use **8.1.0** instead if a warning-free floor is preferred.

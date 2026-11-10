# YTDLP Downloader

A native Android video downloader built with Kotlin and Jetpack Compose, using yt-dlp through the youtubedl-android wrapper.

> Use this app only for content you own, have permission to download, or are otherwise legally entitled to save. DRM-protected content is out of scope.

## Current status

**Stage 1 — Android project foundation**

- [x] Android Gradle project structure
- [x] Kotlin + Jetpack Compose starter screen
- [x] AndroidX and Gradle memory configuration

**Stage 2 — URL inspection and metadata extraction**

- [x] HTTP/HTTPS URL validation
- [x] yt-dlp engine initialization on a background thread
- [x] Video title, thumbnail, duration, uploader and extractor details
- [x] Available source formats, codecs, approximate sizes and resolutions
- [x] User-facing extraction error handling

**Stage 3 — Quality and output selection**

- [x] Best Available quality mode
- [x] Best Efficient mode, preferring AV1 where available without a fixed resolution cap
- [x] Manual maximum resolution, populated from resolutions reported by the source
- [x] Independent Original / HEVC (H.265) output choice
- [x] Selection summary and format-selection plan
- [x] Start yt-dlp downloads using selected quality mode
- [x] Download progress and ETA reporting
- [x] FFmpeg runtime added for separate-stream merging
- [x] Publish completed downloads into Android Downloads on Android 10+
- [ ] Cancellation and persistent background downloads (later stage)

**Stage 4 — Download execution (initial implementation)**

- Downloads run on an IO coroutine with yt-dlp progress updates.
- Best Available uses yt-dlp's best video + best audio selection.
- Best Efficient applies yt-dlp codec/resolution/bitrate sorting with AV1 preference where available; this is a preference, not a guarantee of smallest output.
- Manual Resolution applies the chosen maximum height.
- FFmpeg is initialized so yt-dlp can merge separate audio/video streams.
- On Android 10+, completed output is copied into `Downloads/YTDLP Downloader` through MediaStore. On Android 8–9, this first implementation keeps the file in app-specific external storage.
- HEVC selection is shown, but transcoding is deliberately deferred to Stage 5.

**Planned later stages**

- [ ] yt-dlp download execution and format selection
- [ ] FFmpeg merge and optional HEVC conversion
- [ ] Download queue, progress, cancellation and history
- [ ] Save completed files to Android Downloads

## Development environment

- Android Studio
- JDK 17
- Android SDK (compile SDK 36)
- Kotlin + Jetpack Compose

## Website compatibility

The app uses yt-dlp extractors and is intended for websites supported by yt-dlp, rather than a fixed list of platforms. Actual availability depends on the extractor, site changes, region, network access, and whether the site requires authentication or cookies. No universal compatibility is guaranteed.

## Download options

- **Best Available:** request the best available video and audio streams.
- **Best Efficient:** prefer efficient codecs such as AV1 when offered, then use yt-dlp's available format ranking. This is a preference, not a guarantee of a smaller file.
- **Manual Resolution:** use the best source video at or below the selected resolution; the choices are populated from formats reported for that video.
- **Original:** do not re-encode the downloaded video.
- **HEVC/H.265:** selected independently and intended for FFmpeg conversion in Stage 5.

Quality selection and output codec are independent. For example, Best Available + HEVC is a valid planned combination. Stage 3 currently displays and maintains these selections; Stage 4 will pass the chosen format options to the actual download engine.

## Third-party licensing

The youtubedl-android library (0.18.1) is licensed under GPL-3.0. Review and comply with its license and applicable yt-dlp licensing obligations before distributing builds of this app. Coil Compose is used for thumbnail loading.

## Package

Application ID: `com.fhbotsv1.ytdlpapp`

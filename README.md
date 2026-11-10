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
- [ ] Manual format selection and download execution

**Planned later stages**

- [ ] Best Available / Best Efficient / Manual quality selection
- [ ] Original codec / HEVC (H.265) output selection
- [ ] FFmpeg merge and optional HEVC conversion
- [ ] Download queue, progress, cancellation and history
- [ ] Save completed files to Android Downloads

## Development environment

- Android Studio
- JDK 17
- Android SDK (compile SDK 36)
- Kotlin + Jetpack Compose

## Open the project

1. Download or clone this repository.
2. In Android Studio, choose **Open** and select the repository folder.
3. Allow Gradle to sync and install any requested Android SDK components.
4. Run the **app** configuration on an Android device or emulator.

The Gradle wrapper properties are included. If the wrapper scripts/JAR are not present in your local checkout, create them from the project root with `gradle wrapper --gradle-version 8.13` (requires a local Gradle installation), then reopen the project.

## Website compatibility

The app uses yt-dlp extractors and is intended for websites supported by yt-dlp, rather than a fixed list of platforms. Actual availability depends on the extractor, site changes, region, network access, and whether the site requires authentication or cookies. No universal compatibility is guaranteed.

## Planned download options

- **Quality:** Best Available, Best Efficient, or Manual resolution (only source-provided formats).
- **Output codec:** Original (no video re-encoding) or HEVC/H.265 (FFmpeg conversion).
- Audio should be preserved and copied where compatible, avoiding unnecessary re-encoding.

Quality selection and output codec are independent. For example, Best Available + HEVC is intended to be a supported combination.

## Third-party licensing

The youtubedl-android library (0.18.1) is licensed under GPL-3.0. Review and comply with its license and applicable yt-dlp licensing obligations before distributing builds of this app. Coil Compose is used for thumbnail loading.

## Package

Application ID: `com.fhbotsv1.ytdlpapp`

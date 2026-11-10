# YTDLP Downloader

A native Android video downloader built with Kotlin and Jetpack Compose, using yt-dlp as the extraction engine.

> Use this app only for content you own, have permission to download, or are otherwise legally entitled to save. DRM-protected content is out of scope.

## Current status

**Stage 1 — Android project foundation and starter UI**

- [x] Android Gradle project structure
- [x] Kotlin + Jetpack Compose starter screen
- [ ] URL metadata extraction
- [ ] Format and resolution selection
- [ ] yt-dlp download engine
- [ ] FFmpeg merge and optional HEVC conversion
- [ ] Download queue, progress, and history
- [ ] Save to Android Downloads

## Development environment

- Android Studio
- JDK 17
- Android SDK (compile SDK 36)
- Kotlin
- Jetpack Compose

## Open the project

1. Download or clone this repository.
2. In Android Studio, choose **Open** and select the repository folder.
3. Allow Gradle to sync and install any requested Android SDK components.
4. Run the **app** configuration on an Android device or emulator.

The Gradle wrapper properties are included. If the wrapper scripts/JAR are not present in your local checkout, create them from the project root with `gradle wrapper --gradle-version 8.13` (requires a local Gradle installation), then reopen the project.

## Planned download options

- **Quality:** Best Available, Best Efficient, or Manual resolution (only source-provided formats).
- **Output codec:** Original (no video re-encoding) or HEVC/H.265 (FFmpeg conversion).
- Audio should be preserved and copied where compatible, avoiding unnecessary re-encoding.

Quality selection and output codec are independent. For example, Best Available + HEVC is a supported intended combination.

## Package

Initial application ID: `com.fhbotsv1.ytdlpapp`

This is a development starter. Download functionality will be added in later stages.

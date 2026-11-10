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

**Stage 4 — Download execution**
- [x] Download on an IO coroutine with yt-dlp progress updates
- [x] Best Available, Best Efficient and Manual Resolution passed to yt-dlp
- [x] FFmpeg initialized for yt-dlp stream merging
- [x] Android 10+ completed downloads published to `Downloads/YTDLP Downloader` through MediaStore
- [x] Stable yt-dlp updater available through the library
- [ ] Cancellation and persistent background downloads

**Stage 6 — Source codec and size estimate**
- [x] Display video codecs reported by the source website
- [x] Estimate selected source download size from reported video/audio format sizes when available
- [x] Identify the likely selected video codec for the chosen quality mode
- [x] Warn when converting an AV1 source to HEVC may increase file size and cause unnecessary re-encoding
- [x] Clarify that source size is an estimate and actual download size can differ

**Stage 5 — HEVC/H.265 output conversion (initial implementation)**
- [x] Original mode keeps the downloaded video stream unchanged
- [x] HEVC mode downloads the source, then invokes the bundled FFmpeg executable to encode video with `libx265`
- [x] Initial encoding defaults: CRF 28 and fast preset
- [x] Original audio copied without re-encoding; if MP4 cannot contain the audio codec, retry in MKV
- [x] Conversion progress reported separately from download progress
- [x] If conversion fails, preserve and save the original download instead
- [ ] Verify `libx265` availability and conversion behavior on target devices

**Planned later stages**
- [ ] Download queue, pause/resume/cancel and history
- [ ] Persistent background downloads and notifications
- [ ] Improved format/codec controls and user-configurable HEVC quality
- [ ] Download history and file management

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
- **Manual Resolution:** use the best source video at or below the selected resolution; choices are populated from formats reported for that video.
- **Original:** do not re-encode the downloaded video.
- **HEVC/H.265:** selected independently; after downloading, FFmpeg re-encodes the video to H.265 while copying audio when compatible. The app tries MP4 first and MKV as a compatibility fallback.

Quality selection and output codec are independent. For example, Best Available + HEVC is a valid combination.

## Third-party licensing

The youtubedl-android library (0.18.1) is licensed under GPL-3.0. Review and comply with its license and applicable yt-dlp licensing obligations before distributing builds of this app. Coil Compose is used for thumbnail loading.

## Package

Application ID: `com.fhbotsv1.ytdlpapp`

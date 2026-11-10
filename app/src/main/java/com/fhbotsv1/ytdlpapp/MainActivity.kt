package com.fhbotsv1.ytdlpapp

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.net.URLConnection
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yausername.youtubedl_android.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class QualityMode(val label: String) {
    BEST_AVAILABLE("Best Available"),
    BEST_EFFICIENT("Best Efficient"),
    MANUAL("Manual Resolution")
}

private enum class OutputCodec(val label: String) {
    ORIGINAL("Original"),
    HEVC("HEVC / H.265")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloaderHome()
                }
            }
        }
    }
}

@Composable
private fun DownloaderHome() {
    var videoUrl by remember { mutableStateOf("") }
    var videoInfo by remember { mutableStateOf<VideoInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadEta by remember { mutableStateOf<Long?>(null) }
    var downloadStatus by remember { mutableStateOf("No download started.") }
    var downloadedLocation by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Paste a video link to get started.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var qualityMode by remember { mutableStateOf(QualityMode.BEST_AVAILABLE) }
    var outputCodec by remember { mutableStateOf(OutputCodec.ORIGINAL) }
    var manualResolution by remember { mutableStateOf(1080) }
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("YTDLP Downloader", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Inspect links from yt-dlp supported websites",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Video URL", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = {
                        videoUrl = it
                        videoInfo = null
                        errorMessage = null
                        statusMessage = "Ready to check the link."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://…") },
                    singleLine = true,
                    enabled = !isLoading
                )
                Button(
                    onClick = {
                        val submittedUrl = videoUrl.trim()
                        if (!isValidHttpUrl(submittedUrl)) {
                            errorMessage = "Enter a valid web link beginning with https:// or http://."
                            videoInfo = null
                            statusMessage = "The link needs attention."
                        } else {
                            isLoading = true
                            videoInfo = null
                            errorMessage = null
                            statusMessage = "Connecting to the website and reading video information…"
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        YoutubeDL.init(appContext)
                                        val request = YoutubeDLRequest(submittedUrl).apply {
                                            addOption("--no-playlist")
                                            addOption("--no-warnings")
                                        }
                                        YoutubeDL.getInfo(request)
                                    }
                                    videoInfo = result
                                    val heights = result.formats.orEmpty()
                                        .mapNotNull { it.height.takeIf { height -> height > 0 } }
                                        .distinct().sortedDescending()
                                    if (heights.isNotEmpty() && manualResolution !in heights) {
                                        manualResolution = heights.first()
                                    }
                                    statusMessage = "Video information loaded. Choose quality and output codec below."
                                } catch (exception: Exception) {
                                    errorMessage = exception.message?.lineSequence()
                                        ?.firstOrNull()?.take(350)
                                        ?: "The website did not provide video information."
                                    statusMessage = "Could not read this link."
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Text("  Checking video…")
                    } else {
                        Text("Check video")
                    }
                }
                TextButton(
                    onClick = {
                        videoUrl = ""
                        videoInfo = null
                        errorMessage = null
                        statusMessage = "Paste a video link to get started."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Clear link")
                }
            }
        }

        if (isLoading) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("First-time engine setup or website extraction can take a little while.")
                }
            }
        }

        errorMessage?.let { message ->
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Unable to check this link", fontWeight = FontWeight.SemiBold)
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                    Text("Some websites require sign-in or cookies, or may block automated access.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        videoInfo?.let { info ->
            VideoDetailsCard(info)
            AvailableFormatsCard(info.formats.orEmpty())
            DownloadSettingsCard(
                formats = info.formats.orEmpty(),
                qualityMode = qualityMode,
                onQualityModeChange = { qualityMode = it },
                outputCodec = outputCodec,
                onOutputCodecChange = { outputCodec = it },
                manualResolution = manualResolution,
                onManualResolutionChange = { manualResolution = it }
            )
            SelectionSummaryCard(
                qualityMode = qualityMode,
                outputCodec = outputCodec,
                manualResolution = manualResolution,
                formats = info.formats.orEmpty()
            )
            DownloadActionCard(
                enabled = !isLoading && !isDownloading,
                isDownloading = isDownloading,
                progress = downloadProgress,
                etaSeconds = downloadEta,
                status = downloadStatus,
                savedLocation = downloadedLocation,
                codec = outputCodec,
                onStart = {
                    val selectedUrl = videoUrl.trim()
                    isDownloading = true
                    downloadProgress = 0f
                    downloadEta = null
                    downloadedLocation = null
                    downloadStatus = "Preparing download…"
                    scope.launch {
                        try {
                            val saved = withContext(Dispatchers.IO) {
                                YoutubeDL.init(appContext)
                                FFmpeg.getInstance().init(appContext)
                                val outputDir = File(
                                    appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                        ?: appContext.filesDir,
                                    "ytdlp-stage4"
                                ).apply { mkdirs() }
                                val previousNames = outputDir.listFiles()
                                    ?.map { it.name }?.toSet().orEmpty()
                                val request = YoutubeDLRequest(selectedUrl).apply {
                                    addOption("--no-playlist")
                                    addOption("--no-mtime")
                                    addOption("--newline")
                                    addOption("-o", File(outputDir,
                                        "%(title).180B [%(id)s]-%(epoch)s.%(ext)s").absolutePath)
                                    addOption("-f", buildFormatSelector(qualityMode, manualResolution))
                                    if (qualityMode == QualityMode.BEST_EFFICIENT) {
                                        addOption("-S", "vcodec:av01,res,br")
                                    }
                                }
                                val processId = "ytdlp-${System.currentTimeMillis()}"
                                YoutubeDL.getInstance().execute(request, processId) { progress, eta, line ->
                                    scope.launch {
                                        downloadProgress = progress.coerceIn(0f, 100f)
                                        downloadEta = eta
                                        if (line.isNotBlank()) downloadStatus = line.take(180)
                                    }
                                }
                                val outputFile = outputDir.listFiles()
                                    ?.filter { file ->
                                        file.isFile && file.name !in previousNames &&
                                            !file.name.endsWith(".part", true) &&
                                            !file.name.endsWith(".ytdl", true) &&
                                            !file.name.endsWith(".temp", true)
                                    }
                                    ?.maxByOrNull { it.lastModified() }
                                    ?: throw IllegalStateException(
                                        "yt-dlp finished but no completed output file was found."
                                    )
                                publishToDownloads(appContext, outputFile)
                            }
                            downloadProgress = 100f
                            downloadEta = 0L
                            downloadedLocation = saved
                            downloadStatus = "Download completed successfully."
                        } catch (exception: Exception) {
                            downloadStatus = exception.message?.lineSequence()
                                ?.firstOrNull()?.take(300) ?: "Download failed."
                        } finally {
                            isDownloading = false
                        }
                    }
                }
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(statusMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Use this app only for content you are permitted to save. DRM-protected content is not supported.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun DownloadActionCard(
    enabled: Boolean,
    isDownloading: Boolean,
    progress: Float,
    etaSeconds: Long?,
    status: String,
    savedLocation: String?,
    codec: OutputCodec,
    onStart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Download", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            if (codec == OutputCodec.HEVC) {
                Text(
                    "HEVC is selected. Stage 4 downloads the original source first; actual H.265 conversion will be added in Stage 5.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text(if (isDownloading) "Downloading…" else "Start download")
            }
            if (isDownloading || progress > 0f) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${progress.toInt()}% complete" +
                    (etaSeconds?.takeIf { it > 0 }?.let { " · ETA ${formatDuration(it.toInt())}" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium)
            }
            Text(status, style = MaterialTheme.typography.bodySmall)
            savedLocation?.let { SettingLine("Saved to", it) }
            Text(
                "Downloads are saved to the phone's Downloads folder when supported by Android. On older Android versions, the app uses its private download folder.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildFormatSelector(mode: QualityMode, resolution: Int): String = when (mode) {
    QualityMode.BEST_AVAILABLE -> "bestvideo+bestaudio/best"
    QualityMode.BEST_EFFICIENT -> "bestvideo+bestaudio/best"
    QualityMode.MANUAL ->
        "bestvideo[height<=$resolution]+bestaudio/best[height<=$resolution]"
}

private fun publishToDownloads(context: android.content.Context, source: File): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return source.absolutePath
    val mimeType = URLConnection.guessContentTypeFromName(source.name)
        ?: "application/octet-stream"
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DOWNLOADS + "/YTDLP Downloader")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: throw IllegalStateException("Android could not create the Downloads entry.")
    try {
        resolver.openOutputStream(uri)?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        } ?: throw IllegalStateException("Could not open the Downloads output file.")
        val completeValues = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        resolver.update(uri, completeValues, null, null)
        source.delete()
        return "Downloads/YTDLP Downloader/${source.name}"
    } catch (exception: Exception) {
        resolver.delete(uri, null, null)
        throw exception
    }
}

@Composable
private fun VideoDetailsCard(info: VideoInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Video information", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            info.thumbnail?.takeIf { it.startsWith("http") }?.let { imageUrl ->
                AsyncImage(model = imageUrl, contentDescription = "Video thumbnail",
                    modifier = Modifier.fillMaxWidth().height(190.dp),
                    contentScale = ContentScale.Crop)
            }
            Text(info.title ?: info.fulltitle ?: "Untitled video",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            info.uploader?.let { SettingLine("Uploader", it) }
            if (info.duration > 0) SettingLine("Duration", formatDuration(info.duration))
            info.extractor?.let { SettingLine("Website", it) }
            if (info.width > 0 && info.height > 0) {
                SettingLine("Source dimensions", "${info.width} × ${info.height}")
            }
            info.webpageUrl?.let { SettingLine("Page", it) }
        }
    }
}

@Composable
private fun AvailableFormatsCard(formats: List<VideoFormat>) {
    val displayFormats = remember(formats) {
        formats.distinctBy { it.formatId ?: "${it.height}-${it.ext}-${it.vcodec}-${it.acodec}" }
            .sortedByDescending { it.height }.take(18)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Available formats", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            if (displayFormats.isEmpty()) {
                Text("No separate format list was provided for this link.")
            } else {
                val resolutions = formats.mapNotNull { it.height.takeIf { height -> height > 0 } }
                    .distinct().sortedDescending()
                Text(if (resolutions.isNotEmpty()) {
                    "Available video resolutions: ${resolutions.joinToString { "${it}p" }}"
                } else {
                    "No video resolution details were reported."
                }, style = MaterialTheme.typography.bodyMedium)
                displayFormats.forEach { FormatLine(it) }
                if (formats.size > displayFormats.size) {
                    Text("Showing ${displayFormats.size} of ${formats.size} formats.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FormatLine(format: VideoFormat) {
    val resolution = if (format.height > 0) "${format.height}p" else "Audio only"
    val extension = format.ext?.uppercase() ?: "Unknown format"
    val videoCodec = format.vcodec?.takeUnless { it == "none" } ?: "no video"
    val audioCodec = format.acodec?.takeUnless { it == "none" } ?: "no audio"
    val bytes = format.fileSizeApproximate.takeIf { it > 0L } ?: format.fileSize
    val sizeText = if (bytes > 0L) " · ${formatBytes(bytes)}" else ""
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$resolution · $extension · ${format.formatId ?: "format"}",
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text("$videoCodec / $audioCodec$sizeText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DownloadSettingsCard(
    formats: List<VideoFormat>,
    qualityMode: QualityMode,
    onQualityModeChange: (QualityMode) -> Unit,
    outputCodec: OutputCodec,
    onOutputCodecChange: (OutputCodec) -> Unit,
    manualResolution: Int,
    onManualResolutionChange: (Int) -> Unit
) {
    val resolutions = remember(formats) {
        formats.mapNotNull { it.height.takeIf { height -> height > 0 } }
            .distinct().sortedDescending()
    }
    var resolutionMenuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Download settings", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Quality", style = MaterialTheme.typography.titleSmall)
            QualityMode.values().forEach { mode ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = qualityMode == mode,
                        onClick = { onQualityModeChange(mode) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mode.label, fontWeight = FontWeight.Medium)
                        Text(
                            when (mode) {
                                QualityMode.BEST_AVAILABLE -> "Choose the highest source quality and best available audio."
                                QualityMode.BEST_EFFICIENT -> "Prefer efficient source codecs such as AV1 when available, without forcing a fixed resolution cap."
                                QualityMode.MANUAL -> "Choose a resolution actually offered by this video."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (qualityMode == QualityMode.MANUAL) {
                Text("Maximum resolution", style = MaterialTheme.typography.labelLarge)
                if (resolutions.isEmpty()) {
                    Text("This link did not report selectable video resolutions.")
                } else {
                    OutlinedButton(
                        onClick = { resolutionMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (manualResolution in resolutions) "${manualResolution}p" else "${resolutions.first()}p")
                    }
                    DropdownMenu(
                        expanded = resolutionMenuExpanded,
                        onDismissRequest = { resolutionMenuExpanded = false }
                    ) {
                        resolutions.forEach { height ->
                            DropdownMenuItem(
                                text = { Text("${height}p") },
                                onClick = {
                                    onManualResolutionChange(height)
                                    resolutionMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text("Output video codec", style = MaterialTheme.typography.titleSmall)
            OutputCodec.values().forEach { codec ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = outputCodec == codec,
                        onClick = { onOutputCodecChange(codec) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(codec.label, fontWeight = FontWeight.Medium)
                        Text(
                            if (codec == OutputCodec.ORIGINAL)
                                "Keep the downloaded video stream unchanged; no video re-encoding."
                            else
                                "Convert the downloaded video to H.265 using FFmpeg in a later stage."
                            ,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text("Audio will be preserved and copied where compatible.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    qualityMode: QualityMode,
    outputCodec: OutputCodec,
    manualResolution: Int,
    formats: List<VideoFormat>
) {
    val selector = when (qualityMode) {
        QualityMode.BEST_AVAILABLE -> "bestvideo+bestaudio/best"
        QualityMode.BEST_EFFICIENT -> "bestvideo+bestaudio/best (prefer AV1, then resolution and audio quality)"
        QualityMode.MANUAL -> "bestvideo[height<=$manualResolution]+bestaudio/best[height<=$manualResolution]"
    }
    val manualAvailable = formats.any { it.height == manualResolution && it.height > 0 }
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Current selection", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            SettingLine("Quality mode", if (qualityMode == QualityMode.MANUAL) "Manual · ${manualResolution}p" else qualityMode.label)
            SettingLine("Video output", outputCodec.label)
            SettingLine("Format selection plan", selector)
            if (qualityMode == QualityMode.MANUAL && !manualAvailable) {
                Text("The selected resolution is not listed as a separate format; yt-dlp will use the best available format up to this resolution.",
                    style = MaterialTheme.typography.bodySmall)
            }
            if (outputCodec == OutputCodec.HEVC) {
                Text("HEVC conversion is selected but is not performed yet. It will be added with FFmpeg in Stage 5.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun isValidHttpUrl(value: String): Boolean = runCatching {
    val uri = Uri.parse(value)
    (uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()
}.getOrDefault(false)

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

private fun formatBytes(bytes: Long): String {
    val megabytes = bytes / (1024.0 * 1024.0)
    return if (megabytes >= 1024.0) "%.2f GB".format(megabytes / 1024.0)
    else "%.1f MB".format(megabytes)
}

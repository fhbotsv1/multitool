package com.fhbotsv1.ytdlpapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import java.io.File
import java.net.URLConnection
import android.provider.OpenableColumns
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
import androidx.core.content.FileProvider
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

private data class DownloadOutcome(
    val location: String,
    val status: String,
    val uri: String
)

private data class SavedMedia(
    val location: String,
    val uri: String
)

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
    var downloadedLocation by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Paste a video link to get started.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var qualityMode by remember { mutableStateOf(QualityMode.BEST_AVAILABLE) }
    var outputCodec by remember { mutableStateOf(OutputCodec.ORIGINAL) }
    var manualResolution by remember { mutableStateOf(1080) }
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(appContext) }
    val workInfos by workManager.getWorkInfosForUniqueWorkLiveData(QUEUE_WORK_NAME).observeAsState()
    val activeWorkInfo = workInfos?.firstOrNull { it.state == androidx.work.WorkInfo.State.RUNNING || it.state == androidx.work.WorkInfo.State.ENQUEUED || it.state == androidx.work.WorkInfo.State.BLOCKED }
    val isQueueRunning = activeWorkInfo != null
    val isDownloading = isQueueRunning
    val downloadProgress = activeWorkInfo?.progress?.getFloat("progress", 0f) ?: 0f
    val downloadEta = activeWorkInfo?.progress?.getLong("eta", 0L)?.takeIf { it > 0L }
    val downloadStatus = activeWorkInfo?.progress?.getString("status") ?: if (isQueueRunning) "Preparing background download…" else "No download started."
    var history by remember { mutableStateOf(loadDownloadHistory(appContext)) }
    var downloadQueue by remember { mutableStateOf(loadDownloadQueue(appContext)) }
    var queuePaused by remember { mutableStateOf(loadQueuePaused(appContext)) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        setQueuePaused(appContext, false)
        queuePaused = false
        setQueueControl(appContext, "")
        enqueueDownloadQueue(workManager)
    }
    LaunchedEffect(workInfos) {
        if (workInfos != null) {
            var refreshedQueue = loadDownloadQueue(appContext)
            if (activeWorkInfo == null && refreshedQueue.any { it.status == "Downloading" }) {
                refreshedQueue = refreshedQueue.map { if (it.status == "Downloading") it.copy(status = "Queued") else it }
                saveDownloadQueue(appContext, refreshedQueue)
            }
            downloadQueue = refreshedQueue
            queuePaused = loadQueuePaused(appContext)
            history = loadDownloadHistory(appContext)
        }
    }

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
                    if (isValidHttpUrl(selectedUrl)) {
                        val queued = QueueDownloadItem(
                            id = System.currentTimeMillis().toString(),
                            url = selectedUrl,
                            title = info.title ?: info.fulltitle ?: "Video",
                            qualityMode = qualityMode.name,
                            outputCodec = outputCodec.name,
                            manualResolution = manualResolution,
                            durationSeconds = info.duration.toLong(),
                            status = "Queued",
                            error = ""
                        )
                        downloadQueue = downloadQueue + queued
                        saveDownloadQueue(appContext, downloadQueue)
                        statusMessage = "Added to download queue."
                    } else {
                        statusMessage = "Check the video link before adding it to the queue."
                    }
                }
            )
        }

        DownloadQueueCard(
            items = downloadQueue,
            isRunning = isQueueRunning,
            progress = downloadProgress,
            etaSeconds = downloadEta,
            currentStatus = downloadStatus,
            isPaused = queuePaused,
            isConverting = downloadStatus.contains("converting", ignoreCase = true),
            onStart = {
                if (!isQueueRunning && downloadQueue.any { it.status == "Queued" || it.status == "Cancelled" }) {
                    setQueuePaused(appContext, false)
                    queuePaused = false
                    setQueueControl(appContext, "")
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else enqueueDownloadQueue(workManager)
                }
            },
            onPause = {
                setQueuePaused(appContext, true)
                queuePaused = true
                setQueueControl(appContext, "pause")
                downloadQueue.firstOrNull { it.status == "Downloading" }?.let { active ->
                    runCatching { YoutubeDL.getInstance().destroyProcessById("ytdlp-" + active.id) }
                }
            },
            onResume = {
                setQueuePaused(appContext, false)
                queuePaused = false
                setQueueControl(appContext, "")
                enqueueDownloadQueue(workManager)
            },
            onCancelActive = {
                downloadQueue.firstOrNull { it.status == "Downloading" }?.let { active ->
                    setQueueControl(appContext, "cancel_current")
                    runCatching { YoutubeDL.getInstance().destroyProcessById("ytdlp-" + active.id) }
                }
            },
            onRetry = { item ->
                downloadQueue = downloadQueue.map { if (it.id == item.id) it.copy(status = "Queued", error = "") else it }
                saveDownloadQueue(appContext, downloadQueue)
            },
            onRemove = { item ->
                downloadQueue = downloadQueue.filterNot { it.id == item.id }
                saveDownloadQueue(appContext, downloadQueue)
            },
            onClearCompleted = {
                downloadQueue = downloadQueue.filter { it.status != "Completed" }
                saveDownloadQueue(appContext, downloadQueue)
            }
        )

        DownloadHistoryCard(
            items = history,
            onOpen = { item -> openDownloadedFile(context, item) },
            onShare = { item -> shareDownloadedFile(context, item) },
            onRemove = { item ->
                history = history.filterNot { it.id == item.id }
                saveDownloadHistory(appContext, history)
            },
            onClear = {
                history = emptyList()
                saveDownloadHistory(appContext, history)
            }
        )

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
            Text("Add to download queue", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            if (codec == OutputCodec.HEVC) {
                Text(
                    "The downloaded video will be converted to H.265. The original is retained if conversion fails.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text(
                    when {
                        !isDownloading -> "Add to queue"
                        codec == OutputCodec.HEVC && progress >= 50f -> "Converting to HEVC…"
                        else -> "Downloading…"
                    }
                )
            }
            if (isDownloading || progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
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

private fun transcodeToHevc(
    context: android.content.Context,
    source: File,
    outputDir: File,
    durationSeconds: Long,
    onProgress: (Float, String) -> Unit
): File {
    FFmpeg.init(context)
    val ffmpegBinary = File(context.applicationInfo.nativeLibraryDir, "libffmpeg.so")
    if (!ffmpegBinary.isFile) {
        throw IllegalStateException("The bundled FFmpeg executable was not found.")
    }

    val baseName = source.nameWithoutExtension
    val mp4Output = File(outputDir, "$baseName.hevc.mp4")
    val mkvOutput = File(outputDir, "$baseName.hevc.mkv")

    val mp4Result = runHevcEncoding(
        context, ffmpegBinary, source, mp4Output, durationSeconds, "mp4", onProgress
    )
    if (mp4Result.exitCode == 0 && mp4Output.isFile && mp4Output.length() > 0L) {
        return mp4Output
    }
    mp4Output.delete()

    // Retry in MKV if the source audio codec cannot be copied into MP4.
    val mkvResult = runHevcEncoding(
        context, ffmpegBinary, source, mkvOutput, durationSeconds, "mkv", onProgress
    )
    if (mkvResult.exitCode == 0 && mkvOutput.isFile && mkvOutput.length() > 0L) {
        return mkvOutput
    }
    mkvOutput.delete()

    val details = mkvResult.error.ifBlank { mp4Result.error }
    throw IllegalStateException(
        "FFmpeg could not encode this video as HEVC. " +
            details.takeLast(500).ifBlank { "The bundled build may not include the libx265 encoder." }
    )
}

private data class HevcProcessResult(
    val exitCode: Int,
    val error: String
)

private fun runHevcEncoding(
    context: android.content.Context,
    ffmpegBinary: File,
    source: File,
    destination: File,
    durationSeconds: Long,
    container: String,
    onProgress: (Float, String) -> Unit
): HevcProcessResult {
    destination.delete()
    val command = mutableListOf(
        ffmpegBinary.absolutePath,
        "-y",
        "-hide_banner",
        "-loglevel", "error",
        "-i", source.absolutePath,
        "-map", "0:v:0",
        "-map", "0:a?",
        "-c:v", "libx265",
        "-preset", "fast",
        "-crf", "28"
    )
    if (container == "mp4") {
        command.addAll(listOf("-tag:v", "hvc1", "-movflags", "+faststart"))
    }
    command.addAll(
        listOf("-c:a", "copy", "-progress", "pipe:1", "-nostats", destination.absolutePath)
    )

    val baseDir = File(context.noBackupFilesDir, "youtubedl-android")
    val pythonLibDir = File(baseDir, "packages/python/usr/lib")
    val ffmpegLibDir = File(baseDir, "packages/ffmpeg/usr/lib")
    val nativeLibDir = File(context.applicationInfo.nativeLibraryDir)
    val processBuilder = ProcessBuilder(command).redirectErrorStream(true)
    processBuilder.environment().apply {
        val libraryPaths = listOf(pythonLibDir, ffmpegLibDir, nativeLibDir)
            .filter { it.exists() }
            .joinToString(":") { it.absolutePath }
        this["LD_LIBRARY_PATH"] = libraryPaths
        this["PATH"] = nativeLibDir.absolutePath + ":" + (this["PATH"] ?: "")
        this["TMPDIR"] = context.cacheDir.absolutePath
        this["HOME"] = context.noBackupFilesDir.absolutePath
    }

    val process = processBuilder.start()
    val recentOutput = ArrayDeque<String>()
    process.inputStream.bufferedReader().useLines { lines ->
        lines.forEach { line ->
            if (line.isNotBlank()) {
                if (recentOutput.size >= 20) recentOutput.removeFirst()
                recentOutput.addLast(line)
                if (line.startsWith("out_time_ms=")) {
                    val timeMicros = line.substringAfter('=').toLongOrNull()
                    if (timeMicros != null && durationSeconds > 0L) {
                        val percent = (timeMicros.toDouble() /
                            (durationSeconds * 1_000_000.0) * 100.0).toFloat().coerceIn(0f, 99f)
                        onProgress(percent, "Converting to HEVC… ${percent.toInt()}%")
                    }
                } else if (line.startsWith("progress=") && line.endsWith("end")) {
                    onProgress(100f, "Finishing HEVC conversion…")
                }
            }
        }
    }
    val exitCode = process.waitFor()
    return HevcProcessResult(exitCode, recentOutput.joinToString("\n"))
}

private fun publishToDownloads(context: android.content.Context, source: File): SavedMedia {
    val mimeType = URLConnection.guessContentTypeFromName(source.name)
        ?: "application/octet-stream"
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val contentUri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            source
        )
        return SavedMedia(source.absolutePath, contentUri.toString())
    }
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
        return SavedMedia("Downloads/YTDLP Downloader/${source.name}", uri.toString())
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
            val reportedCodecs = info.formats.orEmpty()
                .mapNotNull { format ->
                    format.vcodec?.takeIf { it.isNotBlank() && it != "none" }
                        ?.let { displayVideoCodec(it) }
                }
                .distinct()
            if (reportedCodecs.isNotEmpty()) {
                SettingLine("Available video codecs", reportedCodecs.joinToString(" · "))
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
                                "Re-encode the video to H.265 with FFmpeg; copy audio when compatible."
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
    val estimate = remember(formats, qualityMode, manualResolution) {
        estimateSourceFormat(formats, qualityMode, manualResolution)
    }
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Current selection", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            SettingLine("Quality mode", if (qualityMode == QualityMode.MANUAL) "Manual · ${manualResolution}p" else qualityMode.label)
            SettingLine("Likely selected source codec", estimate.codec)
            SettingLine("Estimated download size", estimate.bytes?.let { "~${formatBytes(it)} (source estimate)" } ?: "Not provided by this website")
            SettingLine("Video output", outputCodec.label)
            SettingLine("Format selection plan", selector)
            if (qualityMode == QualityMode.MANUAL && !manualAvailable) {
                Text("The selected resolution is not listed as a separate format; yt-dlp will use the best available format up to this resolution.",
                    style = MaterialTheme.typography.bodySmall)
            }
            if (outputCodec == OutputCodec.HEVC) {
                Text("The video will be re-encoded to H.265 after downloading. Audio is copied where compatible.",
                    style = MaterialTheme.typography.bodySmall)
                if (estimate.codec == "AV1") {
                    Text("This source is already AV1. Converting it to HEVC may increase file size and will re-encode the video unnecessarily.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
            Text("Size is an estimate from the formats reported by the website. Actual download size can differ.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class SourceFormatEstimate(
    val codec: String,
    val bytes: Long?
)

private fun estimateSourceFormat(
    formats: List<VideoFormat>,
    mode: QualityMode,
    resolution: Int
): SourceFormatEstimate {
    val videos = formats.filter {
        !it.vcodec.isNullOrBlank() && it.vcodec != "none" &&
            (mode != QualityMode.MANUAL || (it.height > 0 && it.height <= resolution))
    }
    if (videos.isEmpty()) return SourceFormatEstimate("Not reported", null)

    fun sizeOf(format: VideoFormat): Long? =
        format.fileSizeApproximate.takeIf { it > 0L }
            ?: format.fileSize.takeIf { it > 0L }

    val videoOnly = videos.filter { it.acodec.isNullOrBlank() || it.acodec == "none" }
    val candidates = if (videoOnly.isNotEmpty()) videoOnly else videos
    val selected = if (mode == QualityMode.BEST_EFFICIENT) {
        candidates.maxWithOrNull(
            compareBy<VideoFormat> { if (it.vcodec?.startsWith("av01", true) == true) 1 else 0 }
                .thenBy { it.height }
                .thenBy { sizeOf(it) ?: 0L }
        )
    } else {
        candidates.maxWithOrNull(
            compareBy<VideoFormat> { it.height }.thenBy { sizeOf(it) ?: 0L }
        )
    } ?: return SourceFormatEstimate("Not reported", null)

    val codec = displayVideoCodec(selected.vcodec)
    val videoBytes = sizeOf(selected)
    val hasAudioInSameFormat = !selected.acodec.isNullOrBlank() && selected.acodec != "none"
    val audioBytes = if (hasAudioInSameFormat) 0L else {
        formats.asSequence()
            .filter { it.vcodec == "none" && !it.acodec.isNullOrBlank() && it.acodec != "none" }
            .mapNotNull { sizeOf(it) }
            .maxOrNull() ?: 0L
    }
    val total = videoBytes?.let { it + audioBytes }?.takeIf { it > 0L }
    return SourceFormatEstimate(codec, total)
}

private fun displayVideoCodec(codec: String?): String {
    val value = codec?.lowercase()?.takeIf { it.isNotBlank() && it != "none" }
        ?: return "Not reported"
    return when {
        value.startsWith("av01") -> "AV1"
        value.startsWith("hev") || value.startsWith("hvc1") -> "HEVC / H.265"
        value.startsWith("avc") -> "H.264 / AVC"
        value.startsWith("vp09") -> "VP9"
        value.startsWith("vp8") -> "VP8"
        value.startsWith("theora") -> "Theora"
        else -> codec.substringBefore('.').uppercase()
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


private data class QueueDownloadItem(
    val id: String, val url: String, val title: String, val qualityMode: String,
    val outputCodec: String, val manualResolution: Int, val durationSeconds: Long,
    val status: String, val error: String
)

private const val QUEUE_WORK_NAME = "ytdlp-download-queue"
private const val DOWNLOAD_NOTIFICATION_CHANNEL = "ytdlp_downloads"
private const val DOWNLOAD_NOTIFICATION_ID = 7101
private const val DOWNLOAD_COMPLETION_NOTIFICATION_ID = 7102
private const val QUEUE_CONTROL_PREFS = "queue_control"
private const val QUEUE_PAUSED_KEY = "paused"
private const val QUEUE_COMMAND_KEY = "command"

private fun loadQueuePaused(context: Context): Boolean =
    context.getSharedPreferences(QUEUE_CONTROL_PREFS, Context.MODE_PRIVATE)
        .getBoolean(QUEUE_PAUSED_KEY, false)

private fun setQueuePaused(context: Context, paused: Boolean) {
    context.getSharedPreferences(QUEUE_CONTROL_PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(QUEUE_PAUSED_KEY, paused).apply()
}

private fun setQueueControl(context: Context, command: String) {
    context.getSharedPreferences(QUEUE_CONTROL_PREFS, Context.MODE_PRIVATE)
        .edit().putString(QUEUE_COMMAND_KEY, command).apply()
}

private fun getQueueControl(context: Context): String =
    context.getSharedPreferences(QUEUE_CONTROL_PREFS, Context.MODE_PRIVATE)
        .getString(QUEUE_COMMAND_KEY, "") ?: ""

private fun enqueueDownloadQueue(workManager: WorkManager) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val request = OneTimeWorkRequestBuilder<DownloadQueueWorker>().setConstraints(constraints).build()
    workManager.enqueueUniqueWork(QUEUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
}

private fun loadDownloadQueue(context: android.content.Context): List<QueueDownloadItem> = runCatching {
    val raw = context.getSharedPreferences("download_queue", android.content.Context.MODE_PRIVATE).getString("items", "[]") ?: "[]"
    val array = org.json.JSONArray(raw)
    (0 until array.length()).mapNotNull { i ->
        val o = array.optJSONObject(i) ?: return@mapNotNull null
        val status = o.optString("status", "Queued")
        QueueDownloadItem(
            id = o.optString("id"), url = o.optString("url"), title = o.optString("title", "Video"),
            qualityMode = o.optString("qualityMode", "BEST_AVAILABLE"), outputCodec = o.optString("outputCodec", "ORIGINAL"),
            manualResolution = o.optInt("manualResolution", 1080), durationSeconds = o.optLong("durationSeconds"),
            status = status, error = o.optString("error")
        )
    }
}.getOrDefault(emptyList())

private fun saveDownloadQueue(context: android.content.Context, items: List<QueueDownloadItem>) {
    val array = org.json.JSONArray()
    items.forEach { item -> array.put(org.json.JSONObject().apply {
        put("id", item.id); put("url", item.url); put("title", item.title)
        put("qualityMode", item.qualityMode); put("outputCodec", item.outputCodec)
        put("manualResolution", item.manualResolution); put("durationSeconds", item.durationSeconds)
        put("status", item.status); put("error", item.error)
    }) }
    context.getSharedPreferences("download_queue", android.content.Context.MODE_PRIVATE).edit().putString("items", array.toString()).apply()
}

@Composable
private fun DownloadQueueCard(
    items: List<QueueDownloadItem>, isRunning: Boolean, progress: Float, etaSeconds: Long?,
    currentStatus: String, isPaused: Boolean, isConverting: Boolean,
    onStart: () -> Unit, onPause: () -> Unit, onResume: () -> Unit,
    onCancelActive: () -> Unit, onRetry: (QueueDownloadItem) -> Unit,
    onRemove: (QueueDownloadItem) -> Unit, onClearCompleted: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Download queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (items.any { it.status == "Completed" }) TextButton(onClick = onClearCompleted) { Text("Clear completed") }
            }
            if (items.isEmpty()) Text("Check a video and tap Add to queue to place it here.", style = MaterialTheme.typography.bodySmall)
            items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, fontWeight = FontWeight.Medium)
                    Text(item.status + if (item.error.isNotBlank()) " · " + item.error else "", style = MaterialTheme.typography.bodySmall,
                        color = if (item.status == "Failed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.status == "Failed" || item.status == "Cancelled") {
                            TextButton(onClick = { onRetry(item) }, enabled = !isRunning) { Text("Retry") }
                        }
                        if (item.status != "Downloading") {
                            TextButton(onClick = { onRemove(item) }) { Text("Remove") }
                        }
                    }
                }
                if (item != items.last()) androidx.compose.material3.HorizontalDivider()
            }
            if (isRunning) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                Text(progress.toInt().toString() + "% · " + currentStatus + (etaSeconds?.takeIf { it > 0 }?.let { " · ETA " + formatDuration(it.toInt()) } ?: ""), style = MaterialTheme.typography.bodySmall)
            }
            if (isRunning) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onPause, enabled = !isPaused && !isConverting, modifier = Modifier.weight(1f)) {
                        Text("Pause queue")
                    }
                    OutlinedButton(onClick = onCancelActive, enabled = !isConverting && items.any { it.status == "Downloading" }, modifier = Modifier.weight(1f)) {
                        Text("Cancel current")
                    }
                }
                if (isConverting) {
                    Text("Pause and cancel are temporarily unavailable during HEVC conversion.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (isPaused && items.any { it.status == "Queued" }) {
                Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text("Resume queue") }
            } else {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth(),
                    enabled = items.any { it.status == "Queued" }) { Text("Start queued downloads") }
            }
            Text("Downloads run one at a time in the background. Pause stops the active yt-dlp process; resume retries it and may continue from a partial file when the source supports resuming.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private suspend fun performQueuedDownload(context: android.content.Context, item: QueueDownloadItem, onProgress: (Float, Long?, String) -> Unit): DownloadOutcome {
    val quality = runCatching { QualityMode.valueOf(item.qualityMode) }.getOrDefault(QualityMode.BEST_AVAILABLE)
    val codec = runCatching { OutputCodec.valueOf(item.outputCodec) }.getOrDefault(OutputCodec.ORIGINAL)
    return withContext(Dispatchers.IO) {
        YoutubeDL.init(context); FFmpeg.getInstance().init(context)
        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "ytdlp-stage4").apply { mkdirs() }
        val before = outputDir.listFiles()?.map { it.name }?.toSet().orEmpty()
        val request = YoutubeDLRequest(item.url).apply {
            addOption("--no-playlist"); addOption("--no-mtime"); addOption("--newline")
            addOption("-o", File(outputDir, "%(title).180B [%(id)s]-%(epoch)s.%(ext)s").absolutePath)
            addOption("-f", buildFormatSelector(quality, item.manualResolution))
            if (quality == QualityMode.BEST_EFFICIENT) addOption("-S", "vcodec:av01,res,br")
        }
        YoutubeDL.getInstance().execute(request, "ytdlp-" + item.id) { p, eta, line ->
            val command = getQueueControl(context)
            if (command == "pause" || command == "cancel_current") {
                runCatching { YoutubeDL.getInstance().destroyProcessById("ytdlp-" + item.id) }
            }
            onProgress(if (codec == OutputCodec.HEVC) p.coerceIn(0f, 100f) * 0.5f else p.coerceIn(0f, 100f), eta, line)
        }
        val source = outputDir.listFiles()?.filter { it.isFile && it.name !in before && !it.name.endsWith(".part", true) && !it.name.endsWith(".ytdl", true) && !it.name.endsWith(".temp", true) }?.maxByOrNull { it.lastModified() }
            ?: throw IllegalStateException("yt-dlp finished but no completed output file was found.")
        if (codec == OutputCodec.HEVC) {
            try {
                val hevc = transcodeToHevc(context, source, outputDir, item.durationSeconds.toInt()) { p, status -> onProgress(50f + p * 0.5f, null, status) }
                val saved = publishToDownloads(context, hevc); source.delete()
                DownloadOutcome(saved.location, "Download and HEVC conversion completed successfully.", saved.uri)
            } catch (e: Exception) {
                val saved = publishToDownloads(context, source)
                DownloadOutcome(saved.location, "HEVC conversion failed; original video saved. " + (e.message ?: "FFmpeg error").take(200), saved.uri)
            }
        } else {
            val saved = publishToDownloads(context, source)
            DownloadOutcome(saved.location, "Download completed successfully.", saved.uri)
        }
    }
}

class DownloadQueueWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        createDownloadNotificationChannel(applicationContext)
        var queue = loadDownloadQueue(applicationContext).map { if (it.status == "Downloading") it.copy(status = "Queued") else it }
        saveDownloadQueue(applicationContext, queue)
        val pending = queue.filter { it.status == "Queued" }
        if (pending.isEmpty()) return Result.success()
        setForeground(createDownloadForegroundInfo(applicationContext, "Starting download queue…", 0))
        var completed = 0
        var failed = 0
        for (queuedItem in pending) {
            if (isStopped) {
                queue = loadDownloadQueue(applicationContext).map { if (it.status == "Downloading") it.copy(status = "Queued") else it }
                saveDownloadQueue(applicationContext, queue)
                return Result.retry()
            }
            if (loadQueuePaused(applicationContext) || getQueueControl(applicationContext) == "pause") {
                setQueueControl(applicationContext, "")
                return Result.success()
            }
            val item = loadDownloadQueue(applicationContext).firstOrNull {
                it.id == queuedItem.id && it.status == "Queued"
            } ?: continue
            if (getQueueControl(applicationContext) == "cancel_current") {
                setQueueControl(applicationContext, "")
                queue = loadDownloadQueue(applicationContext).map {
                    if (it.id == item.id) it.copy(status = "Cancelled", error = "Cancelled by user.") else it
                }
                saveDownloadQueue(applicationContext, queue)
                continue
            }
            queue = loadDownloadQueue(applicationContext).map { if (it.id == item.id) it.copy(status = "Downloading", error = "") else it }
            saveDownloadQueue(applicationContext, queue)
            setProgress(Data.Builder().putFloat("progress", 0f).putLong("eta", 0L)
                .putString("status", "Downloading ${item.title}").putString("currentTitle", item.title).build())
            setForeground(createDownloadForegroundInfo(applicationContext, "Downloading: ${item.title}", 0))
            try {
                val outcome = performQueuedDownload(applicationContext, item) { progress, eta, line ->
                    val safeProgress = progress.coerceIn(0f, 100f)
                    runBlocking {
                        setProgress(Data.Builder().putFloat("progress", safeProgress).putLong("eta", eta ?: 0L)
                            .putString("status", line.take(180)).putString("currentTitle", item.title).build())
                        setForeground(createDownloadForegroundInfo(applicationContext,
                            if (line.isNotBlank()) line.take(100) else "Downloading: ${item.title}", safeProgress.toInt()))
                    }
                }
                // Clear a late cancel request so it cannot accidentally affect the next queue item.
                setQueueControl(applicationContext, "")
                val size = runCatching {
                    applicationContext.contentResolver.openFileDescriptor(Uri.parse(outcome.uri), "r")?.use { it.statSize }?.takeIf { it > 0L }
                        ?: outcome.location.takeIf { it.startsWith("/") }?.let { File(it).length() } ?: 0L
                }.getOrDefault(0L)
                val record = DownloadHistoryItem(System.currentTimeMillis().toString(), item.title, item.url,
                    outcome.location, outcome.uri, size, System.currentTimeMillis(), outcome.status)
                saveDownloadHistory(applicationContext, (listOf(record) + loadDownloadHistory(applicationContext)).take(100))
                queue = loadDownloadQueue(applicationContext).map { if (it.id == item.id) it.copy(status = "Completed", error = "") else it }
                saveDownloadQueue(applicationContext, queue)
                completed++
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                queue = loadDownloadQueue(applicationContext).map { if (it.id == item.id && it.status == "Downloading") it.copy(status = "Queued") else it }
                saveDownloadQueue(applicationContext, queue)
                throw cancelled
            } catch (e: Exception) {
                val command = getQueueControl(applicationContext)
                when (command) {
                    "pause" -> {
                        setQueueControl(applicationContext, "")
                        setQueuePaused(applicationContext, true)
                        queue = loadDownloadQueue(applicationContext).map {
                            if (it.id == item.id && it.status == "Downloading") it.copy(status = "Queued", error = "") else it
                        }
                        saveDownloadQueue(applicationContext, queue)
                        return Result.success()
                    }
                    "cancel_current" -> {
                        setQueueControl(applicationContext, "")
                        queue = loadDownloadQueue(applicationContext).map {
                            if (it.id == item.id && it.status == "Downloading") it.copy(status = "Cancelled", error = "Cancelled by user.") else it
                        }
                        saveDownloadQueue(applicationContext, queue)
                    }
                    else -> {
                        val message = e.message?.lineSequence()?.firstOrNull()?.take(250) ?: "Download failed."
                        queue = loadDownloadQueue(applicationContext).map { if (it.id == item.id) it.copy(status = "Failed", error = message) else it }
                        saveDownloadQueue(applicationContext, queue)
                        failed++
                    }
                }
            }
        }
        val summary = if (failed == 0) "Queue completed · ${completed} downloaded" else "Queue finished · ${completed} downloaded · ${failed} failed"
        setProgress(Data.Builder().putFloat("progress", 100f).putString("status", summary).build())
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(DOWNLOAD_COMPLETION_NOTIFICATION_ID, buildDownloadNotification(applicationContext, summary, 100, false))
        return Result.success()
    }
}

private fun createDownloadNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(DOWNLOAD_NOTIFICATION_CHANNEL, "Video downloads", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "Shows background video download progress and completion." })
    }
}

private fun buildDownloadNotification(context: Context, message: String, progress: Int, ongoing: Boolean): Notification {
    val openIntent = PendingIntent.getActivity(context, 7101, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
    return NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
        .setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("YTDLP Downloader")
        .setContentText(message).setContentIntent(openIntent).setOnlyAlertOnce(true)
        .setOngoing(ongoing).setAutoCancel(!ongoing).setProgress(100, progress.coerceIn(0, 100), false).build()
}

private fun createDownloadForegroundInfo(context: Context, message: String, progress: Int): ForegroundInfo {
    val notification = buildDownloadNotification(context, message, progress, true)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    else ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification)
}

private data class DownloadHistoryItem(
    val id: String,
    val title: String,
    val url: String,
    val location: String,
    val uri: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val status: String
)

private const val HISTORY_PREFS = "download_history"
private const val HISTORY_KEY = "items"

private fun loadDownloadHistory(context: android.content.Context): List<DownloadHistoryItem> {
    return runCatching {
        val raw = context.getSharedPreferences(HISTORY_PREFS, android.content.Context.MODE_PRIVATE)
            .getString(HISTORY_KEY, "[]") ?: "[]"
        val array = org.json.JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            DownloadHistoryItem(
                id = item.optString("id"),
                title = item.optString("title", "Downloaded video"),
                url = item.optString("url"),
                location = item.optString("location"),
                uri = item.optString("uri"),
                sizeBytes = item.optLong("sizeBytes"),
                createdAt = item.optLong("createdAt"),
                status = item.optString("status", "Completed")
            )
        }
    }.getOrDefault(emptyList())
}

private fun saveDownloadHistory(
    context: android.content.Context,
    items: List<DownloadHistoryItem>
) {
    val array = org.json.JSONArray()
    items.forEach { item ->
        array.put(org.json.JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("url", item.url)
            put("location", item.location)
            put("uri", item.uri)
            put("sizeBytes", item.sizeBytes)
            put("createdAt", item.createdAt)
            put("status", item.status)
        })
    }
    context.getSharedPreferences(HISTORY_PREFS, android.content.Context.MODE_PRIVATE)
        .edit().putString(HISTORY_KEY, array.toString()).apply()
}

@Composable
private fun DownloadHistoryCard(
    items: List<DownloadHistoryItem>,
    onOpen: (DownloadHistoryItem) -> Unit,
    onShare: (DownloadHistoryItem) -> Unit,
    onRemove: (DownloadHistoryItem) -> Unit,
    onClear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Download history", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                if (items.isNotEmpty()) {
                    TextButton(onClick = onClear) { Text("Clear history") }
                }
            }
            if (items.isEmpty()) {
                Text("Completed downloads will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                items.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.title, fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.createdAt))}" +
                                if (item.sizeBytes > 0L) " · ${formatBytes(item.sizeBytes)}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(item.location, style = MaterialTheme.typography.bodySmall)
                        Text(item.status, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onOpen(item) }) { Text("Open") }
                            OutlinedButton(onClick = { onShare(item) }) { Text("Share") }
                            TextButton(onClick = { onRemove(item) }) { Text("Remove") }
                        }
                    }
                    if (item != items.last()) {
                        androidx.compose.material3.HorizontalDivider()
                    }
                }
            }
            Text("Removing an entry only removes it from this list; it does not delete the downloaded file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun openDownloadedFile(
    context: android.content.Context,
    item: DownloadHistoryItem
) {
    val uri = Uri.parse(item.uri)
    val mimeType = URLConnection.guessContentTypeFromName(item.location)
        ?: "video/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Open video").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private fun shareDownloadedFile(
    context: android.content.Context,
    item: DownloadHistoryItem
) {
    val uri = Uri.parse(item.uri)
    val mimeType = URLConnection.guessContentTypeFromName(item.location)
        ?: "video/*"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share video").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

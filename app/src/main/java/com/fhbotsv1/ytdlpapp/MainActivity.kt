package com.fhbotsv1.ytdlpapp

import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var statusMessage by remember { mutableStateOf("Paste a video link to get started.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                                    statusMessage = "Video information loaded successfully."
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
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(statusMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Download settings", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                SettingLine("Quality", "Best Available (selection comes next)")
                SettingLine("Output codec", "Original (HEVC option comes later)")
                SettingLine("Audio", "Preserve original where possible")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Use this app only for content you are permitted to save. DRM-protected content is not supported.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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

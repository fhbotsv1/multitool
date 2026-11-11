
package com.fhbotsv1.multitool

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF101522)
private val SurfaceDark = Color(0xFF1B2433)
private val Accent = Color(0xFF64B5F6)
private val SecondaryText = Color(0xFF9AA9BD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiToolApp()
        }
    }
}

data class ToolItem(
    val name: String,
    val description: String,
    val emoji: String,
    val category: String,
    val color: Color
)

private val tools = listOf(
    ToolItem("Speedometer", "Live GPS speed", "🚗", "Measurement", Color(0xFF42A5F5)),
    ToolItem("Bubble Level", "Check horizontal and vertical level", "🫧", "Measurement", Color(0xFF66BB6A)),
    ToolItem("Ruler", "Measure length on screen", "📏", "Measurement", Color(0xFFFFB74D)),
    ToolItem("Protractor", "Measure angles in degrees", "📐", "Measurement", Color(0xFFAB80FF)),
    ToolItem("AR Measure", "Camera-based measurements", "📷", "Measurement", Color(0xFF26C6DA)),
    ToolItem("GPS Distance", "Measure distance travelled", "📍", "Measurement", Color(0xFFEC6C86)),

    ToolItem("Compass", "Find direction and heading", "🧭", "Sensors", Color(0xFF42A5F5)),
    ToolItem("Magnetic Detector", "Live magnetic field readings", "🧲", "Sensors", Color(0xFFEF5350)),
    ToolItem("Metal Detector", "Detect magnetic metal objects", "🔩", "Sensors", Color(0xFFFFA726)),
    ToolItem("Light Meter", "Measure ambient light in lux", "💡", "Sensors", Color(0xFFFFD54F)),
    ToolItem("Sound Meter", "Measure sound levels", "🔊", "Sensors", Color(0xFF26C6DA)),
    ToolItem("Barometer", "Measure atmospheric pressure", "🌡️", "Sensors", Color(0xFF66BB6A)),
    ToolItem("Vibration Meter", "Measure device acceleration", "📳", "Sensors", Color(0xFFAB80FF)),

    ToolItem("Stopwatch", "Precision time measurement", "⏱️", "Calculators", Color(0xFF42A5F5)),
    ToolItem("Timer", "Countdown timer", "⏳", "Calculators", Color(0xFFFFB74D)),
    ToolItem("Unit Converter", "Convert units and measurements", "🔄", "Calculators", Color(0xFF66BB6A)),

    ToolItem("Flashlight", "Torch, strobe and SOS", "🔦", "Phone Tools", Color(0xFFFFD54F)),
    ToolItem("QR Scanner", "Scan QR codes and barcodes", "▦", "Phone Tools", Color(0xFF42A5F5)),
    ToolItem("Color Detector", "Identify colours using camera", "🎨", "Phone Tools", Color(0xFFAB80FF)),
    ToolItem("Screen Tester", "Test display and touch", "📱", "Phone Tools", Color(0xFF26C6DA))
)

@Composable
fun MultiToolApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTool by remember { mutableStateOf<ToolItem?>(null) }

    val visibleTools = when (selectedTab) {
        1 -> tools.filter { it.category == "Sensors" }
        2 -> tools.filter { it.category == "Measurement" }
        3 -> tools.filter { it.category == "Calculators" || it.category == "Phone Tools" }
        else -> tools
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Background,
            surface = SurfaceDark
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = Color.White
                ) {
                    val navItems = listOf(
                        "Home" to "⌂",
                        "Sensors" to "◉",
                        "Measure" to "📐",
                        "More" to "▦",
                        "Settings" to "⚙"
                    )

                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Text(item.second, fontSize = 21.sp)
                            },
                            label = {
                                Text(item.first, fontSize = 11.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                indicatorColor = Color(0xFF293B54),
                                unselectedIconColor = SecondaryText,
                                unselectedTextColor = SecondaryText
                            )
                        )
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background)
            ) {
                if (selectedTab == 4) {
                    SettingsScreen()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "MultiTool",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Your smart utility kit",
                                    fontSize = 13.sp,
                                    color = SecondaryText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color(0xFF263B57),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛠️", fontSize = 25.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1D3450)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "20 TOOLS AVAILABLE",
                                        color = Color(0xFF80C8FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Everything you need,\nin one place.",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("⚡", fontSize = 42.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            when (selectedTab) {
                                1 -> "Sensor Tools"
                                2 -> "Measurement Tools"
                                3 -> "More Tools"
                                else -> "Explore Tools"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            when (selectedTab) {
                                1 -> "Explore your phone's built-in sensors"
                                2 -> "Measure the world around you"
                                3 -> "Calculators and everyday phone utilities"
                                else -> "Choose a tool to get started"
                            },
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 20.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleTools) { tool ->
                            ToolCard(
                                tool = tool,
                                onClick = { selectedTool = tool }
                            )
                        }
                    }
                }
            }
        }

        selectedTool?.let { tool ->
            if (tool.name == "Speedometer") {
                SpeedometerScreen(onBack = { selectedTool = null })
            } else AlertDialog(
                onDismissRequest = { selectedTool = null },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        "${tool.emoji}  ${tool.name}",
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        "This tool will be implemented in an upcoming development stage.",
                        color = SecondaryText
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { selectedTool = null }
                    ) {
                        Text("OK", color = Accent)
                    }
                }
            )
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        tool.color.copy(alpha = 0.15f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(tool.emoji, fontSize = 23.sp)
            }

            Column {
                Text(
                    tool.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    tool.description,
                    fontSize = 10.sp,
                    color = SecondaryText,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
    ) {
        Text(
            "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Customize your MultiTool experience",
            color = SecondaryText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        val settings = listOf(
            "Appearance" to "Dark and light themes",
            "Units" to "Metric and imperial",
            "Calibration" to "Calibrate measurement tools",
            "Sensor Information" to "Check available phone sensors",
            "Measurement History" to "View saved measurements",
            "About" to "MultiTool application information"
        )

        settings.forEach { (title, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { },
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        description,
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun SpeedometerScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isTracking by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(0f) }
    var maxSpeed by remember { mutableFloatStateOf(0f) }
    var tripDistance by remember { mutableFloatStateOf(0f) }
    var gpsAccuracy by remember { mutableFloatStateOf(0f) }
    var gpsStatus by remember { mutableStateOf("GPS not started") }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            isTracking = true
            gpsStatus = "Acquiring GPS signal…"
        } else gpsStatus = "Location permission denied"
    }

    DisposableEffect(isTracking, hasLocationPermission) {
        if (!isTracking || !hasLocationPermission) {
            onDispose { }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val rawSpeed = if (location.hasSpeed()) location.speed * 3.6f else {
                        val previous = lastLocation
                        if (previous != null) {
                            val seconds = (location.time - previous.time) / 1000f
                            if (seconds > 0f) previous.distanceTo(location) / seconds * 3.6f else 0f
                        } else 0f
                    }
                    currentSpeed = rawSpeed.coerceAtLeast(0f)
                    if (currentSpeed > maxSpeed) maxSpeed = currentSpeed
                    gpsAccuracy = location.accuracy
                    val previous = lastLocation
                    if (previous != null) {
                        val segment = previous.distanceTo(location)
                        if (segment in 0f..1000f) tripDistance += segment
                    }
                    lastLocation = Location(location)
                    gpsStatus = if (location.accuracy > 30f) "GPS connected • Low accuracy" else "GPS connected • Good accuracy"
                }
            }
            try {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    gpsStatus = "GPS is disabled. Turn on Location."
                } else {
                    gpsStatus = "Acquiring GPS signal…"
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper())
                }
            } catch (_: SecurityException) {
                gpsStatus = "Location permission unavailable"
            } catch (_: IllegalArgumentException) {
                gpsStatus = "GPS provider unavailable"
            }
            onDispose {
                try { locationManager.removeUpdates(listener) } catch (_: SecurityException) { }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("GPS SPEEDOMETER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(250.dp).background(Color(0xFF1B2B40), RoundedCornerShape(125.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentSpeed.toInt().toString(), color = Color.White, fontSize = 68.sp, fontWeight = FontWeight.Bold)
                Text("km/h", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("CURRENT SPEED", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(gpsStatus, color = if (gpsStatus.startsWith("GPS connected")) Color(0xFF81C784) else SecondaryText, fontSize = 13.sp)
        if (gpsAccuracy > 0f) Text("Accuracy ±${gpsAccuracy.toInt()} m", color = SecondaryText, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeedStatCard("MAX SPEED", "${maxSpeed}.toInt()} km/h", Modifier.weight(1f))
            SpeedStatCard("TRIP DISTANCE", String.format("%.2f km", tripDistance / 1000f), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = {
                if (!hasLocationPermission) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                } else {
                    isTracking = !isTracking
                    if (!isTracking) gpsStatus = "Paused"
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text(when {
                !hasLocationPermission -> "Allow location & Start"
                isTracking -> "Pause"
                else -> "Resume GPS"
            }, color = Color(0xFF101522), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                currentSpeed = 0f
                maxSpeed = 0f
                tripDistance = 0f
                lastLocation = null
                gpsAccuracy = 0f
                if (isTracking) gpsStatus = "Reset • Waiting for GPS"
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Reset trip", color = Color.White) }
        Spacer(modifier = Modifier.weight(1f))
        Text("For best results, use outdoors with Location set to High accuracy.", color = SecondaryText, fontSize = 11.sp)
    }
}

@Composable
private fun SpeedStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

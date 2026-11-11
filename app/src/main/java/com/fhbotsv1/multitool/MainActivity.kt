
package com.fhbotsv1.multitool

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
            when (tool.name) {
                "Speedometer" -> SpeedometerScreen(onBack = { selectedTool = null })
                "Compass" -> CompassScreen(onBack = { selectedTool = null })
                "Magnetic Detector" -> MagneticFieldScreen(onBack = { selectedTool = null })
                "Metal Detector" -> MetalDetectorScreen(onBack = { selectedTool = null })
                "Light Meter" -> LightMeterScreen(onBack = { selectedTool = null })
                "Sound Meter" -> SoundMeterScreen(onBack = { selectedTool = null })
                "Barometer" -> BarometerScreen(onBack = { selectedTool = null })
                "Vibration Meter" -> VibrationMeterScreen(onBack = { selectedTool = null })
                "Stopwatch" -> StopwatchScreen(onBack = { selectedTool = null })
                else -> AlertDialog(
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
            SpeedStatCard("MAX SPEED", "${maxSpeed.toInt()} km/h", Modifier.weight(1f))
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


@Composable
private fun CompassScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val rotationSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
    }

    var magneticAzimuth by remember { mutableFloatStateOf(0f) }
    var declination by remember { mutableFloatStateOf(0f) }
    var hasTrueNorthCorrection by remember { mutableStateOf(false) }
    var sensorReady by remember { mutableStateOf(rotationSensor != null) }
    var sensorAccuracy by remember { mutableStateOf("Waiting for sensor…") }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun updateDeclination() {
        if (!hasLocationPermission) {
            hasTrueNorthCorrection = false
            return
        }
        try {
            val location = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: SecurityException) {
                null
            } ?: try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: SecurityException) {
                null
            }
            if (location != null) {
                val field = android.hardware.GeomagneticField(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    location.altitude.toFloat(),
                    System.currentTimeMillis()
                )
                declination = field.declination
                hasTrueNorthCorrection = true
            } else {
                hasTrueNorthCorrection = false
            }
        } catch (_: Exception) {
            hasTrueNorthCorrection = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        updateDeclination()
    }

    LaunchedEffect(hasLocationPermission) {
        updateDeclination()
    }

    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            sensorReady = false
            sensorAccuracy = "Rotation-vector sensor unavailable"
            onDispose { }
        } else {
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val matrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    android.hardware.SensorManager.getRotationMatrixFromVector(matrix, event.values)
                    android.hardware.SensorManager.getOrientation(matrix, orientation)
                    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    magneticAzimuth = (degrees + 360f) % 360f
                    sensorReady = true
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    when (accuracy) {
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                            sensorAccuracy = "Sensor accuracy: High"
                            sensorReady = true
                        }
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                            sensorAccuracy = "Sensor accuracy: Medium"
                            sensorReady = true
                        }
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                            sensorAccuracy = "Sensor accuracy: Low • Calibrate phone"
                            sensorReady = true
                        }
                        android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                            sensorAccuracy = "Unreliable sensor • Move phone in a figure-eight"
                            sensorReady = false
                        }
                    }
                }
            }
            val registered = sensorManager.registerListener(
                listener,
                rotationSensor,
                android.hardware.SensorManager.SENSOR_DELAY_UI
            )
            sensorReady = registered
            if (!registered) sensorAccuracy = "Could not start orientation sensor"
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val trueAzimuth = (magneticAzimuth + (if (hasTrueNorthCorrection) declination else 0f) + 360f) % 360f
    val heading = if (hasTrueNorthCorrection) trueAzimuth else magneticAzimuth

    // Animate along the shortest angular path, including across 0°/360°.
    val needleRotation = remember { Animatable(0f) }
    LaunchedEffect(heading, sensorReady) {
        if (sensorReady) {
            val current = needleRotation.value
            val currentWrapped = ((current % 360f) + 360f) % 360f
            val difference = ((heading - currentWrapped + 540f) % 360f) - 180f
            needleRotation.animateTo(
                targetValue = current + difference,
                animationSpec = tween(durationMillis = 100, easing = LinearEasing)
            )
        }
    }

    val direction = when {
        heading >= 337.5f || heading < 22.5f -> "N"
        heading < 67.5f -> "NE"
        heading < 112.5f -> "E"
        heading < 157.5f -> "SE"
        heading < 202.5f -> "S"
        heading < 247.5f -> "SW"
        heading < 292.5f -> "W"
        else -> "NW"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("DIGITAL COMPASS", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            if (sensorReady) "Rotate your phone to find direction" else "Check sensor and calibrate",
            color = if (sensorReady) SecondaryText else Color(0xFFFFB74D),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.46f
                drawCircle(color = Color(0xFF263B57), radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx()))
                drawCircle(color = Color(0xFF1B2B40), radius = radius * 0.82f, center = center)
                for (i in 0 until 72) {
                    val angle = Math.toRadians(i * 5.0)
                    val outer = radius * 0.94f
                    val inner = if (i % 6 == 0) radius * 0.78f else radius * 0.86f
                    val x1 = center.x + kotlin.math.sin(angle).toFloat() * inner
                    val y1 = center.y - kotlin.math.cos(angle).toFloat() * inner
                    val x2 = center.x + kotlin.math.sin(angle).toFloat() * outer
                    val y2 = center.y - kotlin.math.cos(angle).toFloat() * outer
                    drawLine(
                        color = if (i % 18 == 0) Color.White else SecondaryText,
                        start = androidx.compose.ui.geometry.Offset(x1, y1),
                        end = androidx.compose.ui.geometry.Offset(x2, y2),
                        strokeWidth = if (i % 6 == 0) 2.5.dp.toPx() else 1.dp.toPx()
                    )
                }
                rotate(degrees = -needleRotation.value, pivot = center) {
                    val needle = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x, center.y - radius * 0.66f)
                        lineTo(center.x - radius * 0.12f, center.y + radius * 0.12f)
                        lineTo(center.x, center.y + radius * 0.04f)
                        lineTo(center.x + radius * 0.12f, center.y + radius * 0.12f)
                        close()
                    }
                    drawPath(needle, color = Color(0xFFFF5252))
                    drawCircle(color = Color.White, radius = radius * 0.035f, center = center)
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("N", color = Color(0xFFFF5252), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("S", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("W", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("E", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(if (sensorReady) direction else "--", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text(if (sensorReady) heading.toInt().toString() + "°" else "---°", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text("HEADING REFERENCE", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (hasTrueNorthCorrection) "True north • declination " + String.format("%.1f°", declination)
                    else "Magnetic north • true north correction unavailable",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(sensorAccuracy, color = SecondaryText, fontSize = 11.sp)
                Text("Magnetic heading: " + magneticAzimuth.toInt().toString() + "°", color = SecondaryText, fontSize = 11.sp)
            }
        }
        if (!hasLocationPermission) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable location for true north", color = Accent)
            }
        } else if (!hasTrueNorthCorrection) {
            Text("Waiting for a recent location to calculate true north.", color = SecondaryText, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Calibration: move the phone slowly in a figure-eight several times. Keep it away from magnetic cases, speakers, metal and other electronics.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun MagneticFieldScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val magneticSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)
    }

    var fieldX by remember { mutableFloatStateOf(0f) }
    var fieldY by remember { mutableFloatStateOf(0f) }
    var fieldZ by remember { mutableFloatStateOf(0f) }
    var fieldStrength by remember { mutableFloatStateOf(0f) }
    var baseline by remember { mutableStateOf<Float?>(null) }
    var peakChange by remember { mutableFloatStateOf(0f) }
    var sensorReady by remember { mutableStateOf(magneticSensor != null) }
    var sensorAccuracy by remember { mutableStateOf("Waiting for magnetometer…") }

    DisposableEffect(magneticSensor) {
        if (magneticSensor == null) {
            sensorReady = false
            sensorAccuracy = "Magnetometer not available on this device"
            onDispose { }
        } else {
            var smoothedStrength = 0f
            var hasFirstReading = false
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)

                    if (!hasFirstReading) {
                        smoothedStrength = magnitude
                        hasFirstReading = true
                    } else {
                        smoothedStrength += (magnitude - smoothedStrength) * 0.22f
                    }

                    fieldX = x
                    fieldY = y
                    fieldZ = z
                    fieldStrength = smoothedStrength

                    baseline?.let { reference ->
                        val change = kotlin.math.abs(smoothedStrength - reference)
                        if (change > peakChange) peakChange = change
                    }
                    sensorReady = true
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    sensorAccuracy = when (accuracy) {
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Sensor accuracy: High"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Sensor accuracy: Medium"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Sensor accuracy: Low • Calibrate phone"
                        android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> "Sensor unreliable • Move phone in a figure-eight"
                        else -> "Sensor active"
                    }
                }
            }

            val registered = sensorManager.registerListener(
                listener,
                magneticSensor,
                android.hardware.SensorManager.SENSOR_DELAY_GAME
            )
            sensorReady = registered
            if (!registered) sensorAccuracy = "Could not start magnetometer"
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val deviation = baseline?.let { kotlin.math.abs(fieldStrength - it) } ?: 0f
    val fieldStatus = when {
        baseline == null -> "Calibrate to establish baseline"
        deviation >= 15f -> "Large magnetic field change"
        deviation >= 5f -> "Noticeable magnetic field change"
        else -> "Field close to baseline"
    }
    val statusColor = when {
        baseline == null -> Accent
        deviation >= 15f -> Color(0xFFFF5252)
        deviation >= 5f -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("MAGNETIC FIELD", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (!sensorReady) {
            Text("Magnetometer unavailable", color = Color(0xFFFFB74D), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sensorAccuracy, color = SecondaryText, fontSize = 12.sp)
        } else {
            Text("LIVE MAGNETIC FIELD STRENGTH", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(String.format("%.1f", fieldStrength), color = Color.White, fontSize = 60.sp, fontWeight = FontWeight.Bold)
            Text("µT  •  microtesla", color = Accent, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("FIELD COMPONENTS", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MagneticAxisValue("X", fieldX)
                        MagneticAxisValue("Y", fieldY)
                        MagneticAxisValue("Z", fieldZ)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("CHANGE FROM BASELINE", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (baseline == null) "Not calibrated" else String.format("%.1f µT", deviation),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(fieldStatus, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Baseline: " + (baseline?.let { String.format("%.1f µT", it) } ?: "Not set"), color = SecondaryText, fontSize = 11.sp)
                        Text("Peak: " + String.format("%.1f µT", peakChange), color = SecondaryText, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    baseline = fieldStrength
                    peakChange = 0f
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(
                    if (baseline == null) "Calibrate baseline" else "Recalibrate baseline",
                    color = Color(0xFF101522),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    baseline = null
                    peakChange = 0f
                },
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Reset calibration", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(sensorAccuracy, color = SecondaryText, fontSize = 11.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Move the phone near a magnet or magnetic object to observe changes. Ferromagnetic metals usually affect readings more strongly. This is not a radiation or general-purpose metal detector.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun MagneticAxisValue(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(String.format("%+.1f", value), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text("µT", color = SecondaryText, fontSize = 10.sp)
    }
}



@Composable
private fun MetalDetectorScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val magneticSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)
    }

    var fieldStrength by remember { mutableFloatStateOf(0f) }
    var baseline by remember { mutableStateOf<Float?>(null) }
    var peakDeviation by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(magneticSensor != null) }
    var hasReading by remember { mutableStateOf(false) }
    var sensorMessage by remember { mutableStateOf("Waiting for magnetometer…") }

    DisposableEffect(magneticSensor) {
        if (magneticSensor == null) {
            sensorAvailable = false
            sensorMessage = "This phone does not have a magnetometer."
            onDispose { }
        } else {
            var smoothed = 0f
            var first = true
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
                    if (first) {
                        smoothed = magnitude
                        first = false
                    } else {
                        smoothed += (magnitude - smoothed) * 0.18f
                    }
                    fieldStrength = smoothed
                    hasReading = true
                    baseline?.let {
                        val deviation = kotlin.math.abs(smoothed - it)
                        if (deviation > peakDeviation) peakDeviation = deviation
                    }
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    sensorMessage = when (accuracy) {
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Magnetometer accuracy: High"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Magnetometer accuracy: Medium"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low accuracy • move phone in a figure-eight"
                        android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> "Sensor unreliable • recalibrate phone"
                        else -> "Magnetometer active"
                    }
                }
            }
            val registered = sensorManager.registerListener(
                listener,
                magneticSensor,
                android.hardware.SensorManager.SENSOR_DELAY_GAME
            )
            sensorAvailable = registered
            if (!registered) sensorMessage = "Could not start magnetometer."
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val deviation = baseline?.let { kotlin.math.abs(fieldStrength - it) } ?: 0f
    val detectionLevel = when {
        baseline == null -> "Calibrate first"
        deviation >= 25f -> "Strong magnetic disturbance"
        deviation >= 12f -> "Metal / magnetic object nearby"
        deviation >= 5f -> "Weak magnetic change"
        else -> "No significant change"
    }
    val levelColor = when {
        baseline == null -> Accent
        deviation >= 25f -> Color(0xFFFF5252)
        deviation >= 12f -> Color(0xFFFF8A50)
        deviation >= 5f -> Color(0xFFFFD54F)
        else -> Color(0xFF81C784)
    }
    val levelProgress = (deviation / 50f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("METAL DETECTOR", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!sensorAvailable) {
            Text("Magnetometer unavailable", color = Color(0xFFFFB74D), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sensorMessage, color = SecondaryText, fontSize = 12.sp)
        } else {
            Text("MAGNETIC FIELD", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasReading) String.format("%.1f", fieldStrength) else "—",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text("µT • microtesla", color = Accent, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("DETECTION RESPONSE", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF303B4D))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(levelProgress).fillMaxHeight()
                                .background(levelColor, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        if (hasReading) detectionLevel else "Waiting for sensor…",
                        color = levelColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (baseline == null) "Set a baseline with the phone away from magnetic objects."
                        else String.format("Change from baseline: %.1f µT", deviation),
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("READING HISTORY", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Baseline", color = SecondaryText, fontSize = 11.sp)
                            Text(baseline?.let { String.format("%.1f µT", it) } ?: "Not set", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Peak change", color = SecondaryText, fontSize = 11.sp)
                            Text(String.format("%.1f µT", peakDeviation), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    baseline = fieldStrength
                    peakDeviation = 0f
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = hasReading,
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(
                    if (baseline == null) "Calibrate detector" else "Recalibrate",
                    color = Color(0xFF101522),
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = {
                    baseline = null
                    peakDeviation = 0f
                },
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Reset baseline", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(sensorMessage, color = SecondaryText, fontSize = 11.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Calibrate away from metal, then slowly sweep the phone near the object. This detects magnetic disturbances, especially from ferromagnetic metals such as iron and steel. It cannot reliably detect non-magnetic metals such as aluminium, copper or gold. Remove magnetic phone cases and accessories for best results.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun SoundMeterScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var permission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var measuring by remember { mutableStateOf(false) }
    var db by remember { mutableFloatStateOf(0f) }
    var minDb by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxDb by remember { mutableFloatStateOf(0f) }
    var avgDb by remember { mutableFloatStateOf(0f) }
    var count by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Microphone not started") }
    var error by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permission = granted
        if (granted) {
            measuring = true
            status = "Starting microphone…"
        } else status = "Microphone permission denied"
    }

    DisposableEffect(permission, measuring) {
        if (!permission || !measuring) {
            onDispose { }
        } else {
            val running = java.util.concurrent.atomic.AtomicBoolean(true)
            val thread = Thread {
                var recorder: android.media.AudioRecord? = null
                try {
                    val rate = 44100
                    val bufferSize = android.media.AudioRecord.getMinBufferSize(
                        rate,
                        android.media.AudioFormat.CHANNEL_IN_MONO,
                        android.media.AudioFormat.ENCODING_PCM_16BIT
                    )
                    if (bufferSize <= 0) {
                        error = "Audio recording is not supported on this device."
                        measuring = false
                        return@Thread
                    }
                    val audio = android.media.AudioRecord(
                        android.media.MediaRecorder.AudioSource.MIC,
                        rate,
                        android.media.AudioFormat.CHANNEL_IN_MONO,
                        android.media.AudioFormat.ENCODING_PCM_16BIT,
                        maxOf(bufferSize * 2, 4096)
                    )
                    recorder = audio
                    if (audio.state != android.media.AudioRecord.STATE_INITIALIZED) {
                        error = "Could not initialize microphone."
                        measuring = false
                        return@Thread
                    }
                    audio.startRecording()
                    status = "Measuring live sound"
                    val samples = ShortArray(2048)
                    var smooth = 0f
                    var first = true
                    var low = Float.MAX_VALUE
                    var high = 0f
                    var total = 0f
                    var n = 0
                    while (running.get()) {
                        val read = audio.read(samples, 0, samples.size)
                        if (read > 0) {
                            var squares = 0.0
                            for (i in 0 until read) {
                                val sample = samples[i].toDouble()
                                squares += sample * sample
                            }
                            val rms = kotlin.math.sqrt(squares / read)
                            val value = if (rms > 0.0) {
                                (90.0 + 20.0 * kotlin.math.log10(rms / 32768.0)).toFloat().coerceIn(0f, 120f)
                            } else 0f
                            if (first) {
                                smooth = value
                                first = false
                            } else smooth += (value - smooth) * 0.25f
                            low = minOf(low, smooth)
                            high = maxOf(high, smooth)
                            total += smooth
                            n++
                            db = smooth
                            minDb = low
                            maxDb = high
                            avgDb = total / n
                            count = n
                        } else if (read < 0) {
                            error = "Microphone read error ($read)."
                            measuring = false
                            break
                        }
                    }
                } catch (e: SecurityException) {
                    error = "Microphone permission is required."
                    measuring = false
                } catch (e: Exception) {
                    error = "Audio error: " + (e.message ?: "unknown error")
                    measuring = false
                } finally {
                    try {
                        if (recorder?.recordingState == android.media.AudioRecord.RECORDSTATE_RECORDING) recorder?.stop()
                    } catch (_: Exception) { }
                    try { recorder?.release() } catch (_: Exception) { }
                }
            }
            thread.name = "MultiToolSoundMeter"
            thread.isDaemon = true
            thread.start()
            onDispose {
                running.set(false)
                try { thread.join(300) } catch (_: InterruptedException) { thread.interrupt() }
            }
        }
    }

    val label = when {
        db < 30f -> "Very quiet"
        db < 40f -> "Quiet"
        db < 60f -> "Moderate"
        db < 75f -> "Busy / loud"
        db < 90f -> "Very loud"
        else -> "Extremely loud"
    }
    val levelColor = when {
        db < 60f -> Color(0xFF81C784)
        db < 85f -> Color(0xFFFFB74D)
        else -> Color(0xFFFF5252)
    }
    val progress = ((db - 20f) / 100f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("SOUND METER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(22.dp))

        if (!permission) {
            Text("Microphone permission needed", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("The microphone is used only to calculate the live sound level. Audio is not saved.", color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Grant microphone permission", color = Color(0xFF101522), fontWeight = FontWeight.Bold)
            }
        } else {
            Text("ESTIMATED SOUND LEVEL", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (count > 0) String.format("%.1f", db) else "—", color = Color.White, fontSize = 62.sp, fontWeight = FontWeight.Bold)
            Text("dB • approximate", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (measuring && count > 0) label else status, color = if (measuring) levelColor else SecondaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(22.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("LIVE LEVEL", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).background(Color(0xFF303B4D))) {
                        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(levelColor, RoundedCornerShape(50)))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("20 dB", color = SecondaryText, fontSize = 10.sp)
                        Text("120 dB", color = SecondaryText, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "MIN" to if (minDb == Float.MAX_VALUE) "—" else String.format("%.1f", minDb),
                    "AVG" to if (count == 0) "—" else String.format("%.1f", avgDb),
                    "MAX" to if (count == 0) "—" else String.format("%.1f", maxDb)
                ).forEach { item ->
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.first, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(item.second, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("dB", color = SecondaryText, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        error = null
                        measuring = !measuring
                        status = if (measuring) "Starting microphone…" else "Paused"
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(if (measuring) "Pause" else "Resume", color = Color(0xFF101522), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        minDb = Float.MAX_VALUE
                        maxDb = 0f
                        avgDb = 0f
                        count = 0
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Reset stats", color = Color.White) }
            }
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error ?: "", color = Color(0xFFFF8A80), fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Approximate only, not a calibrated sound-pressure meter. Microphone sensitivity, automatic gain control, phone case and distance affect readings. Do not use for workplace safety or hearing-protection decisions. No audio is recorded or stored.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun LightMeterScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val lightSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)
    }

    var lux by remember { mutableFloatStateOf(0f) }
    var minLux by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxLux by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(lightSensor != null) }
    var hasReading by remember { mutableStateOf(false) }

    DisposableEffect(lightSensor) {
        if (lightSensor == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val reading = event.values.firstOrNull()?.coerceAtLeast(0f) ?: return
                    lux = reading
                    minLux = minOf(minLux, reading)
                    maxLux = maxOf(maxLux, reading)
                    hasReading = true
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) = Unit
            }
            val registered = sensorManager.registerListener(
                listener,
                lightSensor,
                android.hardware.SensorManager.SENSOR_DELAY_UI
            )
            sensorAvailable = registered
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val environment = when {
        lux < 1f -> "Very dark"
        lux < 50f -> "Dim"
        lux < 200f -> "Indoor"
        lux < 500f -> "Bright indoor"
        lux < 10000f -> "Outdoor"
        else -> "Very bright sunlight"
    }
    val progress = (kotlin.math.ln(1f + lux) / kotlin.math.ln(100001f)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("LIGHT METER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!sensorAvailable) {
            Text("Ambient light sensor unavailable", color = Color(0xFFFFB74D), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This phone does not expose a working light sensor.", color = SecondaryText, fontSize = 12.sp)
        } else {
            Text("AMBIENT ILLUMINANCE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (hasReading) String.format("%,.1f", lux) else "—",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold
            )
            Text("lux", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (hasReading) environment else "Waiting for sensor…",
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("LIGHT LEVEL", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF303B4D))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .background(Accent, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dark", color = SecondaryText, fontSize = 10.sp)
                        Text("Bright", color = SecondaryText, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("MINIMUM", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(if (hasReading) String.format("%.1f", minLux) else "—", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("lux", color = SecondaryText, fontSize = 11.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("MAXIMUM", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(if (hasReading) String.format("%.1f", maxLux) else "—", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("lux", color = SecondaryText, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = {
                    minLux = lux
                    maxLux = lux
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = hasReading
            ) {
                Text("Reset min / max readings", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "The sensor is usually near the front camera or earpiece. Cover and uncover that area to test the reading. Values are approximate and depend on sensor placement and calibration.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun BarometerScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val pressureSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_PRESSURE)
    }

    var pressure by remember { mutableFloatStateOf(0f) }
    var hasReading by remember { mutableStateOf(false) }
    var sensorAvailable by remember { mutableStateOf(pressureSensor != null) }
    var sensorStatus by remember { mutableStateOf("Waiting for pressure sensor…") }
    var seaLevelText by remember { mutableStateOf("1013.25") }
    var seaLevelPressure by remember { mutableFloatStateOf(1013.25f) }
    var trend by remember { mutableStateOf("Collecting trend data…") }
    var trendChange by remember { mutableFloatStateOf(0f) }
    val pressureHistory = remember { mutableStateListOf<Pair<Long, Float>>() }

    DisposableEffect(pressureSensor) {
        if (pressureSensor == null) {
            sensorAvailable = false
            sensorStatus = "This phone does not have a barometer sensor."
            onDispose { }
        } else {
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val value = event.values.firstOrNull() ?: return
                    if (!value.isFinite() || value <= 0f) return
                    pressure = value
                    hasReading = true
                    sensorStatus = "Barometer active"
                    val now = android.os.SystemClock.elapsedRealtime()
                    pressureHistory.add(now to value)
                    while (pressureHistory.isNotEmpty() && now - pressureHistory.first().first > 600_000L) {
                        pressureHistory.removeAt(0)
                    }
                    val reference = pressureHistory.lastOrNull { now - it.first >= 180_000L }
                    if (reference != null) {
                        trendChange = value - reference.second
                        trend = when {
                            trendChange > 0.5f -> "Rising"
                            trendChange < -0.5f -> "Falling"
                            else -> "Stable"
                        }
                    } else {
                        trend = "Collecting 3-minute trend…"
                    }
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    sensorStatus = when (accuracy) {
                        android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> "Sensor accuracy unreliable"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Sensor accuracy low"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Sensor accuracy medium"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Sensor accuracy high"
                        else -> "Barometer active"
                    }
                }
            }
            val registered = sensorManager.registerListener(
                listener,
                pressureSensor,
                android.hardware.SensorManager.SENSOR_DELAY_NORMAL
            )
            sensorAvailable = registered
            if (!registered) sensorStatus = "Could not start barometer."
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val altitudeMeters = if (hasReading) {
        android.hardware.SensorManager.getAltitude(seaLevelPressure, pressure)
    } else 0f
    val altitudeFeet = altitudeMeters * 3.28084f
    val trendColor = when {
        trend == "Rising" -> Color(0xFF81C784)
        trend == "Falling" -> Color(0xFFFFB74D)
        trend == "Stable" -> Accent
        else -> SecondaryText
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("BAROMETER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (!sensorAvailable) {
            Text("Barometer unavailable", color = Color(0xFFFFB74D), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sensorStatus, color = SecondaryText, fontSize = 12.sp)
        } else {
            Text("ATMOSPHERIC PRESSURE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasReading) String.format("%.2f", pressure) else "—",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold
            )
            Text("hPa  •  mbar", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sensorStatus, color = SecondaryText, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("EST. ALTITUDE", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(if (hasReading) String.format("%.1f m", altitudeMeters) else "—", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (hasReading) String.format("%.0f ft", altitudeFeet) else "—", color = SecondaryText, fontSize = 12.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("3-MIN PRESSURE TREND", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(trend, color = trendColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (pressureHistory.size > 1 && trendChange != 0f) String.format("%+.2f hPa", trendChange) else "Building history", color = SecondaryText, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("ALTITUDE CALIBRATION", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Set local sea-level pressure from a trusted weather station for a more useful altitude estimate.",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = seaLevelText,
                        onValueChange = { seaLevelText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(7) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Sea-level pressure (hPa)") },
                        suffix = { Text("hPa") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = SecondaryText,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = SecondaryText,
                            cursorColor = Accent
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val entered = seaLevelText.toFloatOrNull()
                                if (entered != null && entered in 800f..1100f) seaLevelPressure = entered
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) {
                            Text("Apply", color = Color(0xFF101522), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                seaLevelText = "1013.25"
                                seaLevelPressure = 1013.25f
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Standard", color = Color.White) }
                    }
                    Text(
                        String.format("Altitude reference: %.2f hPa", seaLevelPressure),
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Altitude is estimated from pressure and the selected sea-level reference; weather changes affect accuracy. Pressure readings are local sensor values, not a weather forecast. No location permission is needed.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun VibrationMeterScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val linearSensor = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LINEAR_ACCELERATION)
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
    }
    val activeSensor = linearSensor ?: accelerometer

    var measuring by remember { mutableStateOf(true) }
    var sensorAvailable by remember { mutableStateOf(activeSensor != null) }
    var hasReading by remember { mutableStateOf(false) }
    var current by remember { mutableFloatStateOf(0f) }
    var peak by remember { mutableFloatStateOf(0f) }
    var rms by remember { mutableFloatStateOf(0f) }
    var sampleCount by remember { mutableIntStateOf(0) }
    var sensorStatus by remember {
        mutableStateOf(
            when {
                linearSensor != null -> "Linear acceleration sensor"
                accelerometer != null -> "Accelerometer with gravity filtering"
                else -> "No compatible motion sensor found"
            }
        )
    }

    DisposableEffect(activeSensor, measuring) {
        if (activeSensor == null || !measuring) {
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            var sumSquares = 0.0
            var count = 0
            var smoothed = 0f
            var first = true
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val values = event.values
                    if (values.size < 3) return
                    val ax: Float
                    val ay: Float
                    val az: Float
                    if (linearSensor != null) {
                        ax = values[0]
                        ay = values[1]
                        az = values[2]
                    } else {
                        val alpha = 0.85f
                        gravity[0] = alpha * gravity[0] + (1f - alpha) * values[0]
                        gravity[1] = alpha * gravity[1] + (1f - alpha) * values[1]
                        gravity[2] = alpha * gravity[2] + (1f - alpha) * values[2]
                        ax = values[0] - gravity[0]
                        ay = values[1] - gravity[1]
                        az = values[2] - gravity[2]
                    }
                    val magnitude = kotlin.math.sqrt(ax * ax + ay * ay + az * az)
                    if (!magnitude.isFinite()) return
                    smoothed = if (first) {
                        first = false
                        magnitude
                    } else {
                        smoothed + (magnitude - smoothed) * 0.35f
                    }
                    current = smoothed
                    peak = maxOf(peak, smoothed)
                    sumSquares += magnitude.toDouble() * magnitude.toDouble()
                    count++
                    if (count >= 20) {
                        rms = kotlin.math.sqrt(sumSquares / count).toFloat()
                        sampleCount += count
                        sumSquares = 0.0
                        count = 0
                    }
                    hasReading = true
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    sensorStatus = when (accuracy) {
                        android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> "Sensor accuracy unreliable"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Sensor accuracy low"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Sensor accuracy medium"
                        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Sensor accuracy high"
                        else -> if (linearSensor != null) "Linear acceleration sensor" else "Gravity-filtered accelerometer"
                    }
                }
            }
            val registered = sensorManager.registerListener(
                listener,
                activeSensor,
                android.hardware.SensorManager.SENSOR_DELAY_GAME
            )
            sensorAvailable = registered
            if (!registered) sensorStatus = "Could not start motion sensor."
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val level = when {
        current < 0.15f -> "Very low"
        current < 0.5f -> "Low"
        current < 1.5f -> "Moderate"
        current < 3f -> "Strong"
        else -> "Very strong"
    }
    val levelColor = when {
        current < 0.5f -> Color(0xFF81C784)
        current < 1.5f -> Color(0xFFFFD54F)
        current < 3f -> Color(0xFFFFB74D)
        else -> Color(0xFFFF5252)
    }
    val progress = (current / 5f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("VIBRATION METER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (!sensorAvailable) {
            Text("Motion sensor unavailable", color = Color(0xFFFFB74D), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sensorStatus, color = SecondaryText, fontSize = 12.sp)
        } else {
            Text("LIVE VIBRATION", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasReading) String.format("%.3f", current) else "—",
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold
            )
            Text("m/s²  •  acceleration", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (!measuring) "Paused" else if (hasReading) level else "Waiting for motion…", color = if (measuring) levelColor else SecondaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(22.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("VIBRATION INTENSITY", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF303B4D))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .background(levelColor, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0 m/s²", color = SecondaryText, fontSize = 10.sp)
                        Text("5+ m/s²", color = SecondaryText, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "PEAK" to if (hasReading) String.format("%.3f", peak) else "—",
                    "RMS" to if (hasReading) String.format("%.3f", rms) else "—"
                ).forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.first, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.second, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("m/s²", color = SecondaryText, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(sensorStatus, color = SecondaryText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { measuring = !measuring },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(if (measuring) "Pause" else "Resume", color = Color(0xFF101522), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        current = 0f
                        peak = 0f
                        rms = 0f
                        sampleCount = 0
                        hasReading = false
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Reset stats", color = Color.White) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Place the phone on a stable surface, then gently tap or move the surface to test. Values are approximate and depend on sensor quality, mounting and phone movement. This is not a certified machinery or structural vibration analyser.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun StopwatchScreen(onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedBeforeStart by remember { mutableLongStateOf(0L) }
    var displayMillis by remember { mutableLongStateOf(0L) }
    var startMark by remember { mutableLongStateOf(0L) }
    var lastLapMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                displayMillis = elapsedBeforeStart + (SystemClock.elapsedRealtime() - startMark)
                kotlinx.coroutines.delay(16L)
            }
        }
    }

    fun formatTime(totalMillis: Long): String {
        val minutes = totalMillis / 60000L
        val seconds = (totalMillis % 60000L) / 1000L
        val centiseconds = (totalMillis % 1000L) / 10L
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("‹  Back", color = Accent, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "STOPWATCH",
                color = SecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(54.dp))

        Text(
            "ELAPSED TIME",
            color = SecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            formatTime(displayMillis),
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            "Minutes : Seconds . Centiseconds",
            color = SecondaryText,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(42.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isRunning) {
                        elapsedBeforeStart = displayMillis
                        isRunning = false
                    } else {
                        startMark = SystemClock.elapsedRealtime()
                        isRunning = true
                    }
                },
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isRunning) "Pause" else if (elapsedBeforeStart > 0L) "Resume" else "Start",
                    color = Color(0xFF101522),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = {
                    if (isRunning) {
                        val currentElapsed = elapsedBeforeStart +
                            (SystemClock.elapsedRealtime() - startMark)
                        laps.add(currentElapsed - lastLapMillis)
                        lastLapMillis = currentElapsed
                    } else {
                        elapsedBeforeStart = 0L
                        displayMillis = 0L
                        lastLapMillis = 0L
                        laps.clear()
                    }
                },
                enabled = isRunning || displayMillis > 0L || laps.isNotEmpty(),
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isRunning) "Lap" else "Reset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LAP TIMES",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "\${laps.size} laps",
                        color = Accent,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (laps.isEmpty()) {
                    Text(
                        "Lap times will appear here while the stopwatch is running.",
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                } else {
                    laps.forEachIndexed { index, lapMillis ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Lap \${index + 1}",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                formatTime(lapMillis),
                                color = Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (index != laps.lastIndex) {
                            HorizontalDivider(color = Color(0xFF303B4D))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "The stopwatch uses Android's monotonic elapsed-time clock, so it continues accurately even if the phone's wall-clock time changes.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

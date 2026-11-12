
package com.fhbotsv1.multitool

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

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
    ToolItem("Screen Tester", "Test display and touch", "📱", "Phone Tools", Color(0xFF26C6DA)),
    ToolItem("Battery Ampere", "Live battery charge and discharge current", "🔋", "Phone Tools", Color(0xFF66BB6A)),
    ToolItem("Coin Toss", "Flip a coin: heads or tails", "🪙", "Calculators", Color(0xFFFFD54F)),
    ToolItem("Dice Roll", "Roll a six-sided dice", "🎲", "Calculators", Color(0xFFAB80FF)),
    ToolItem("Internet Speed", "Test download, upload and latency", "🌐", "Phone Tools", Color(0xFF26C6DA))
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
                                        "24 TOOLS AVAILABLE",
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
                "Timer" -> CountdownTimerScreen(onBack = { selectedTool = null })
                "Unit Converter" -> UnitConverterScreen(onBack = { selectedTool = null })
                "Flashlight" -> FlashlightScreen(onBack = { selectedTool = null })
                "QR Scanner" -> QrScannerScreen(onBack = { selectedTool = null })
                "Color Detector" -> ColorDetectorScreen(onBack = { selectedTool = null })
                "Screen Tester" -> ScreenTesterScreen(onBack = { selectedTool = null })
                "Ruler" -> ScreenRulerScreen(onBack = { selectedTool = null })
                "Protractor" -> ProtractorScreen(onBack = { selectedTool = null })
                "Bubble Level" -> BubbleLevelScreen(onBack = { selectedTool = null })
                "Battery Ampere" -> BatteryAmpereScreen(onBack = { selectedTool = null })
                "Coin Toss" -> CoinTossScreen(onBack = { selectedTool = null })
                "Dice Roll" -> DiceRollScreen(onBack = { selectedTool = null })
                "Internet Speed" -> InternetSpeedScreen(onBack = { selectedTool = null })
                "AR Measure" -> ARMeasureScreen(onBack = { selectedTool = null })
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
                        "${laps.size} laps",
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
                                "Lap ${index + 1}",
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


@Composable
private fun CountdownTimerScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    var hoursText by remember { mutableStateOf("00") }
    var minutesText by remember { mutableStateOf("05") }
    var secondsText by remember { mutableStateOf("00") }
    var remainingMillis by remember { mutableLongStateOf(0L) }
    var deadline by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var showFinished by remember { mutableStateOf(false) }

    fun formatRemaining(value: Long): String {
        val totalSeconds = (value.coerceAtLeast(0L) + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun readInputMillis(): Long? {
        val hours = hoursText.toLongOrNull() ?: return null
        val minutes = minutesText.toLongOrNull() ?: return null
        val seconds = secondsText.toLongOrNull() ?: return null
        if (hours < 0L || minutes !in 0L..59L || seconds !in 0L..59L) return null
        if (hours > 99L) return null
        val total = (hours * 3600L + minutes * 60L + seconds) * 1000L
        return total.takeIf { it > 0L }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                val left = deadline - SystemClock.elapsedRealtime()
                if (left <= 0L) {
                    remainingMillis = 0L
                    isRunning = false
                    showFinished = true
                    break
                }
                remainingMillis = left
                kotlinx.coroutines.delay(100L)
            }
        }
    }

    LaunchedEffect(showFinished) {
        if (showFinished) {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 85)
            try {
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0L, 500L, 250L, 500L, 250L),
                            0
                        )
                    )
                }
                while (true) {
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 700)
                    kotlinx.coroutines.delay(1100L)
                }
            } finally {
                tone.stopTone()
                tone.release()
                vibrator.cancel()
            }
        }
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
            Text("COUNTDOWN TIMER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(34.dp))
        Text("TIME REMAINING", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            formatRemaining(remainingMillis),
            color = if (remainingMillis == 0L) Color(0xFFFFB74D) else Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            if (isRunning) "Counting down…" else if (remainingMillis == 0L) "Finished" else "Ready",
            color = if (isRunning) Accent else SecondaryText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("SET DURATION", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    @Composable
                    fun TimeInput(label: String, value: String, onValueChange: (String) -> Unit) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { input ->
                                    onValueChange(input.filter { it.isDigit() }.take(2))
                                },
                                enabled = !isRunning,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text(label) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    disabledTextColor = SecondaryText,
                                    focusedBorderColor = Accent,
                                    unfocusedBorderColor = SecondaryText,
                                    focusedLabelColor = Accent,
                                    unfocusedLabelColor = SecondaryText,
                                    cursorColor = Accent
                                )
                            )
                        }
                    }
                    TimeInput("Hours", hoursText) { hoursText = it; remainingMillis = 0L }
                    TimeInput("Minutes", minutesText) { minutesText = it; remainingMillis = 0L }
                    TimeInput("Seconds", secondsText) { secondsText = it; remainingMillis = 0L }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Minutes and seconds must be between 00 and 59.", color = SecondaryText, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isRunning) {
                        remainingMillis = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                        isRunning = false
                    } else {
                        val duration = if (remainingMillis > 0L) remainingMillis else readInputMillis()
                        if (duration != null && duration > 0L) {
                            remainingMillis = duration
                            deadline = SystemClock.elapsedRealtime() + duration
                            showFinished = false
                            isRunning = true
                        }
                    }
                },
                enabled = isRunning || remainingMillis > 0L || readInputMillis() != null,
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isRunning) "Pause" else if (remainingMillis > 0L) "Resume" else "Start",
                    color = Color(0xFF101522),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    val duration = readInputMillis() ?: 0L
                    remainingMillis = duration
                    showFinished = false
                },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Reset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "When the countdown reaches zero, an on-screen alert will appear. Keep MultiTool open to see the alert.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }

    if (showFinished) {
        AlertDialog(
            onDismissRequest = { showFinished = false },
            containerColor = SurfaceDark,
            title = { Text("Timer finished", color = Color.White) },
            text = { Text("Your countdown has reached zero.", color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = { showFinished = false }) {
                    Text("OK", color = Accent)
                }
            }
        )
    }
}


private data class ConverterUnit(
    val name: String,
    val symbol: String,
    val factor: Double = 1.0
)

private fun converterUnits(category: String): List<ConverterUnit> = when (category) {
    "Length" -> listOf(
        ConverterUnit("Millimetre", "mm", 0.001),
        ConverterUnit("Centimetre", "cm", 0.01),
        ConverterUnit("Metre", "m", 1.0),
        ConverterUnit("Kilometre", "km", 1000.0),
        ConverterUnit("Inch", "in", 0.0254),
        ConverterUnit("Foot", "ft", 0.3048),
        ConverterUnit("Yard", "yd", 0.9144),
        ConverterUnit("Mile", "mi", 1609.344)
    )
    "Weight" -> listOf(
        ConverterUnit("Milligram", "mg", 0.000001),
        ConverterUnit("Gram", "g", 0.001),
        ConverterUnit("Kilogram", "kg", 1.0),
        ConverterUnit("Metric tonne", "t", 1000.0),
        ConverterUnit("Ounce", "oz", 0.028349523125),
        ConverterUnit("Pound", "lb", 0.45359237)
    )
    "Temperature" -> listOf(
        ConverterUnit("Celsius", "°C"),
        ConverterUnit("Fahrenheit", "°F"),
        ConverterUnit("Kelvin", "K")
    )
    "Speed" -> listOf(
        ConverterUnit("Metres per second", "m/s", 1.0),
        ConverterUnit("Kilometres per hour", "km/h", 1.0 / 3.6),
        ConverterUnit("Miles per hour", "mph", 0.44704),
        ConverterUnit("Knot", "kn", 0.514444444444)
    )
    "Area" -> listOf(
        ConverterUnit("Square millimetre", "mm²", 0.000001),
        ConverterUnit("Square centimetre", "cm²", 0.0001),
        ConverterUnit("Square metre", "m²", 1.0),
        ConverterUnit("Square kilometre", "km²", 1_000_000.0),
        ConverterUnit("Hectare", "ha", 10_000.0),
        ConverterUnit("Acre", "ac", 4046.8564224),
        ConverterUnit("Square foot", "ft²", 0.09290304)
    )
    "Volume" -> listOf(
        ConverterUnit("Millilitre", "mL", 0.001),
        ConverterUnit("Litre", "L", 1.0),
        ConverterUnit("Cubic metre", "m³", 1000.0),
        ConverterUnit("US gallon", "US gal", 3.785411784),
        ConverterUnit("US cup", "US cup", 0.2365882365)
    )
    else -> listOf(
        ConverterUnit("Millisecond", "ms", 0.001),
        ConverterUnit("Second", "s", 1.0),
        ConverterUnit("Minute", "min", 60.0),
        ConverterUnit("Hour", "h", 3600.0),
        ConverterUnit("Day", "day", 86400.0)
    )
}

private fun convertUnitValue(
    value: Double,
    category: String,
    from: ConverterUnit,
    to: ConverterUnit
): Double {
    if (category == "Temperature") {
        val celsius = when (from.symbol) {
            "°F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }
        return when (to.symbol) {
            "°F" -> celsius * 9.0 / 5.0 + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
    return value * from.factor / to.factor
}

@Composable
private fun UnitConverterScreen(onBack: () -> Unit) {
    val categories = listOf("Length", "Weight", "Temperature", "Speed", "Area", "Volume", "Time")
    var category by remember { mutableStateOf("Length") }
    var amount by remember { mutableStateOf("1") }
    var fromUnit by remember { mutableStateOf("Metre") }
    var toUnit by remember { mutableStateOf("Kilometre") }
    var categoryMenu by remember { mutableStateOf(false) }

    val units = converterUnits(category)
    val from = units.firstOrNull { it.name == fromUnit } ?: units.first()
    val to = units.firstOrNull { it.name == toUnit } ?: units.getOrElse(1) { units.first() }
    val parsedAmount = amount.toDoubleOrNull()
    val converted = parsedAmount?.let { convertUnitValue(it, category, from, to) }
    val result = if (converted != null && converted.isFinite()) {
        java.math.BigDecimal.valueOf(converted).stripTrailingZeros().toPlainString()
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("‹  Back", color = Accent, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("UNIT CONVERTER", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Convert measurements", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Choose a category, enter a value and select the units.", color = SecondaryText, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Text("CATEGORY", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(
                onClick = { categoryMenu = true },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(category, color = Color.White, modifier = Modifier.weight(1f))
                Text("▾", color = Accent)
            }
            DropdownMenu(
                expanded = categoryMenu,
                onDismissRequest = { categoryMenu = false },
                modifier = Modifier.background(SurfaceDark)
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, color = Color.White) },
                        onClick = {
                            category = item
                            val newUnits = converterUnits(item)
                            fromUnit = newUnits.first().name
                            toUnit = newUnits.getOrElse(1) { newUnits.first() }.name
                            categoryMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("VALUE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }.take(20) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter a value") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = SecondaryText,
                        focusedLabelColor = Accent,
                        cursorColor = Accent
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))
                Text("FROM", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                ConverterUnitPicker(
                    selected = from,
                    units = units,
                    onSelected = { fromUnit = it.name }
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("TO", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                ConverterUnitPicker(
                    selected = to,
                    units = units,
                    onSelected = { toUnit = it.name }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3450)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text("RESULT", color = Color(0xFF80C8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    result ?: "Enter a valid number",
                    color = Color.White,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold
                )
                if (result != null) {
                    Text(to.symbol, color = Color(0xFF80C8FF), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        OutlinedButton(
            onClick = {
                val oldFrom = fromUnit
                fromUnit = toUnit
                toUnit = oldFrom
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = fromUnit != toUnit
        ) {
            Text("⇅  Swap units", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Conversions use standard metric and imperial unit definitions. Temperature conversions use the exact Celsius, Fahrenheit and Kelvin relationships.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ConverterUnitPicker(
    selected: ConverterUnit,
    units: List<ConverterUnit>,
    onSelected: (ConverterUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(
                selected.name + " (" + selected.symbol + ")",
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text("▾", color = Accent)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceDark)
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name + " (" + unit.symbol + ")", color = Color.White) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun FlashlightScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
    }
    val cameraId = remember {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }
    val flashAvailable = cameraId != null
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var mode by remember { mutableStateOf("OFF") }
    var strobeHz by remember { mutableFloatStateOf(4f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) errorMessage = "Camera permission is required to control the LED flash."
    }

    fun setFlash(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, enabled)
            errorMessage = null
        } catch (exception: Exception) {
            errorMessage = exception.localizedMessage ?: "Unable to control the flashlight."
        }
    }

    LaunchedEffect(mode, hasCameraPermission, cameraId, strobeHz) {
        if (!flashAvailable || !hasCameraPermission || mode == "OFF") {
            if (hasCameraPermission) setFlash(false)
        } else {
            when (mode) {
                "ON" -> setFlash(true)
                "STROBE" -> {
                    val interval = (1000f / (strobeHz * 2f)).toLong().coerceAtLeast(25L)
                    try {
                        while (true) {
                            setFlash(true)
                            kotlinx.coroutines.delay(interval)
                            setFlash(false)
                            kotlinx.coroutines.delay(interval)
                        }
                    } finally {
                        setFlash(false)
                    }
                }
                "SOS" -> {
                    val pattern = listOf(
                        180L, 180L, 180L, 180L, 180L, 540L,
                        540L, 180L, 540L, 180L, 540L, 540L,
                        180L, 180L, 180L, 180L, 180L, 900L
                    )
                    try {
                        while (true) {
                            for (index in pattern.indices) {
                                setFlash(index % 2 == 0)
                                kotlinx.coroutines.delay(pattern[index])
                            }
                        }
                    } finally {
                        setFlash(false)
                    }
                }
            }
        }
    }

    DisposableEffect(cameraId, hasCameraPermission) {
        onDispose {
            if (cameraId != null && hasCameraPermission) {
                runCatching { cameraManager.setTorchMode(cameraId, false) }
            }
        }
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
            Text("FLASHLIGHT", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(34.dp))
        Box(
            modifier = Modifier
                .size(176.dp)
                .background(
                    if (mode == "ON") Color(0xFF344B2D) else SurfaceDark,
                    RoundedCornerShape(88.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (mode == "ON") "🔦" else "💡", fontSize = 76.sp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            when (mode) {
                "ON" -> "Flashlight is ON"
                "STROBE" -> "Strobe mode active"
                "SOS" -> "SOS signal active"
                else -> "Flashlight is OFF"
            },
            color = if (mode == "OFF") SecondaryText else Accent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("FLASHLIGHT CONTROL", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                        else mode = if (mode == "ON") "OFF" else "ON"
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == "ON") Color(0xFF455A64) else Accent
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        if (mode == "ON") "Turn OFF" else "Turn ON",
                        color = Color(0xFF101522),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                            else mode = if (mode == "STROBE") "OFF" else "STROBE"
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(if (mode == "STROBE") "Stop Strobe" else "Strobe", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = {
                            if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                            else mode = if (mode == "SOS") "OFF" else "SOS"
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(if (mode == "SOS") "Stop SOS" else "SOS", color = Color.White)
                    }
                }

                if (mode == "STROBE") {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Strobe speed: " + strobeHz.toInt() + " flashes/second", color = Color.White, fontSize = 13.sp)
                    Slider(
                        value = strobeHz,
                        onValueChange = { strobeHz = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        if (!flashAvailable) {
            Text("This device does not report an available camera LED flash.", color = Color(0xFFFFB74D), fontSize = 12.sp)
        } else if (!hasCameraPermission) {
            Text("Allow camera permission when prompted. MultiTool uses it only to control the LED flash; it does not capture photos or video.", color = SecondaryText, fontSize = 12.sp, lineHeight = 17.sp)
        } else {
            Text("LED flash detected. Strobe speed is adjustable from 1 to 10 flashes per second.", color = SecondaryText, fontSize = 12.sp)
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color(0xFFFFB74D), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Avoid looking directly into the LED. Rapid flashing may trigger photosensitive reactions; use strobe and SOS carefully.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}


@Composable
private fun QrScannerScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    var previewView by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var latestResult by remember { mutableStateOf<String?>(null) }
    val history = remember { mutableStateListOf<String>() }
    val scanner = remember {
        com.google.mlkit.vision.barcode.BarcodeScanning.getClient(
            com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
                .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }

    DisposableEffect(scanner) {
        onDispose { scanner.close() }
    }

    LaunchedEffect(previewView, hasCameraPermission) {
        val view = previewView
        if (view != null && hasCameraPermission) {
            try {
                val providerFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    try {
                        val provider = providerFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.surfaceProvider = view.surfaceProvider
                        }
                        val analysis = androidx.camera.core.ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                            )
                            .build()
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null) {
                                imageProxy.close()
                            } else {
                                val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener(ContextCompat.getMainExecutor(context)) { barcodes ->
                                        val value = barcodes.firstNotNullOfOrNull { barcode ->
                                            barcode.rawValue?.takeIf { it.isNotBlank() }
                                        }
                                        if (value != null && value != latestResult) {
                                            latestResult = value
                                            if (history.firstOrNull() != value) {
                                                history.add(0, value)
                                                while (history.size > 20) history.removeAt(history.lastIndex)
                                            }
                                        }
                                    }
                                    .addOnFailureListener(ContextCompat.getMainExecutor(context)) {
                                        cameraError = it.localizedMessage ?: "Barcode scanning failed."
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            }
                        }
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                        cameraError = null
                    } catch (exception: Exception) {
                        cameraError = exception.localizedMessage ?: "Unable to start the camera preview."
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (exception: Exception) {
                cameraError = exception.localizedMessage ?: "Camera is unavailable."
            }
        }
    }

    DisposableEffect(previewView, hasCameraPermission) {
        onDispose {
            runCatching {
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
        }
    }

    fun copyResult(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Scanned code", value))
    }

    fun openResult(value: String) {
        val candidate = value.trim()
        val url = if (candidate.startsWith("https://", true) || candidate.startsWith("http://", true)) {
            candidate
        } else if (candidate.contains(".") && !candidate.contains(" ") && candidate.substringBefore("/").contains(".")) {
            "https://$candidate"
        } else null
        if (url != null) {
            runCatching {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            }.onFailure {
                cameraError = "No app is available to open this link."
            }
        } else {
            cameraError = "The scanned content is not a web link."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent) }
            Spacer(modifier = Modifier.weight(1f))
            Text("QR & BARCODE SCANNER", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (hasCameraPermission) {
            AndroidView(
                factory = { viewContext ->
                    androidx.camera.view.PreviewView(viewContext).apply {
                        scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                        implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
                        previewView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera access is needed to scan codes.", color = Color.White)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant camera access")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Point the camera at a QR code or barcode. Scanning happens live; no photo or video is saved.", color = SecondaryText, fontSize = 12.sp, lineHeight = 17.sp)
        cameraError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color(0xFFFFB74D), fontSize = 12.sp)
        }

        latestResult?.let { value ->
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF20364D)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("LATEST SCAN", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(value, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { copyResult(value) }) {
                            Text("Copy", color = Color.White)
                        }
                        OutlinedButton(onClick = { openResult(value) }) {
                            Text("Open link", color = Color.White)
                        }
                        TextButton(onClick = { latestResult = null }) {
                            Text("Dismiss", color = Accent)
                        }
                    }
                }
            }
        }

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SCAN HISTORY", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { history.clear(); latestResult = null }) {
                    Text("Clear", color = Accent)
                }
            }
            history.forEachIndexed { index, value ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(value, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 2)
                        TextButton(onClick = { copyResult(value) }) {
                            Text("Copy", color = Accent)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ColorDetectorScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    var previewView by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var frozen by remember { mutableStateOf(false) }
    var red by remember { mutableIntStateOf(0) }
    var green by remember { mutableIntStateOf(0) }
    var blue by remember { mutableIntStateOf(0) }
    val favorites = remember { mutableStateListOf<IntArray>() }

    fun colorName(r: Int, g: Int, b: Int): String {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        if (max < 35) return "Black"
        if (min > 220) return "White"
        if (delta < 22) return when {
            max < 85 -> "Dark gray"
            max < 170 -> "Gray"
            else -> "Light gray"
        }
        if (r >= g && r >= b) return when {
            g > 150 && b < 100 -> "Orange"
            g > 100 && b < 90 -> "Yellow-orange"
            g > 150 && b > 130 -> "Pink"
            g > 90 -> "Yellow"
            else -> "Red"
        }
        if (g >= r && g >= b) return when {
            r > 150 && b < 100 -> "Yellow"
            b > 100 -> "Teal"
            else -> "Green"
        }
        return when {
            r > 150 && g < 100 -> "Purple"
            g > 100 -> "Blue"
            else -> "Blue"
        }
    }

    fun copyColor(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Color value", value))
    }

    LaunchedEffect(previewView, hasCameraPermission) {
        val view = previewView
        if (view != null && hasCameraPermission) {
            try {
                val providerFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    try {
                        val provider = providerFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.surfaceProvider = view.surfaceProvider
                        }
                        val analysis = androidx.camera.core.ImageAnalysis.Builder()
                            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { proxy ->
                            try {
                                if (!frozen && proxy.format == android.graphics.ImageFormat.YUV_420_888) {
                                    val planeY = proxy.planes[0]
                                    val planeU = proxy.planes[1]
                                    val planeV = proxy.planes[2]
                                    val cx = proxy.width / 2
                                    val cy = proxy.height / 2
                                    var sumR = 0
                                    var sumG = 0
                                    var sumB = 0
                                    var samples = 0
                                    for (dy in -2..2) {
                                        for (dx in -2..2) {
                                            val x = (cx + dx).coerceIn(0, proxy.width - 1)
                                            val y = (cy + dy).coerceIn(0, proxy.height - 1)
                                            val yIndex = y * planeY.rowStride + x * planeY.pixelStride
                                            val uvX = x / 2
                                            val uvY = y / 2
                                            val uIndex = uvY * planeU.rowStride + uvX * planeU.pixelStride
                                            val vIndex = uvY * planeV.rowStride + uvX * planeV.pixelStride
                                            val yy = planeY.buffer.get(yIndex).toInt() and 0xFF
                                            val uu = (planeU.buffer.get(uIndex).toInt() and 0xFF) - 128
                                            val vv = (planeV.buffer.get(vIndex).toInt() and 0xFF) - 128
                                            val yValue = (yy - 16).coerceAtLeast(0) * 1.164f
                                            val rr = (yValue + 1.596f * vv).toInt().coerceIn(0, 255)
                                            val gg = (yValue - 0.392f * uu - 0.813f * vv).toInt().coerceIn(0, 255)
                                            val bb = (yValue + 2.017f * uu).toInt().coerceIn(0, 255)
                                            sumR += rr
                                            sumG += gg
                                            sumB += bb
                                            samples++
                                        }
                                    }
                                    if (samples > 0 && !frozen) {
                                        red = sumR / samples
                                        green = sumG / samples
                                        blue = sumB / samples
                                    }
                                }
                            } catch (_: Exception) {
                                cameraError = "Unable to read the camera color sample."
                            } finally {
                                proxy.close()
                            }
                        }
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                        cameraError = null
                    } catch (exception: Exception) {
                        cameraError = exception.localizedMessage ?: "Unable to start the camera."
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (exception: Exception) {
                cameraError = exception.localizedMessage ?: "Camera is unavailable."
            }
        }
    }

    DisposableEffect(previewView, hasCameraPermission) {
        onDispose {
            runCatching {
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
        }
    }

    val sampledColor = Color(red, green, blue)
    val hex = "#%02X%02X%02X".format(red, green, blue)
    val rgb = "RGB($red, $green, $blue)"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent) }
            Spacer(modifier = Modifier.weight(1f))
            Text("COLOR DETECTOR", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (hasCameraPermission) {
            Box {
                AndroidView(
                    factory = { viewContext ->
                        androidx.camera.view.PreviewView(viewContext).apply {
                            scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                            implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(310.dp).clip(RoundedCornerShape(20.dp))
                )
                Box(
                    modifier = Modifier.align(Alignment.Center).size(34.dp)
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.White, RoundedCornerShape(4.dp)))
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera access is needed to detect colors.", color = Color.White)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant camera access")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Aim the centre marker at a surface. The detector samples a small area around the centre of the live camera image.", color = SecondaryText, fontSize = 12.sp, lineHeight = 17.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(66.dp)
                            .background(sampledColor, RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(colorName(red, green, blue), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (frozen) "Sample frozen" else "Live color sample", color = SecondaryText, fontSize = 12.sp)
                    }
                    TextButton(onClick = { frozen = !frozen }) {
                        Text(if (frozen) "Resume" else "Freeze", color = Accent)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("HEX", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hex, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { copyColor(hex) }) { Text("Copy", color = Accent) }
                }
                Text("RGB", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rgb, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { copyColor(rgb) }) { Text("Copy", color = Accent) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val exists = favorites.any { it.contentEquals(intArrayOf(red, green, blue)) }
                        if (!exists) favorites.add(intArrayOf(red, green, blue))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save favorite color")
                }
            }
        }

        if (favorites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FAVORITE COLORS", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { favorites.clear() }) { Text("Clear", color = Accent) }
            }
            favorites.forEach { item ->
                val itemHex = "#%02X%02X%02X".format(item[0], item[1], item[2])
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(34.dp).background(Color(item[0], item[1], item[2]), RoundedCornerShape(8.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(itemHex, color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = { copyColor(itemHex) }) { Text("Copy", color = Accent) }
                }
            }
        }
        cameraError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color(0xFFFFB74D), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Color readings are approximate. Lighting, camera white balance, reflections and screen calibration can change the result.", color = SecondaryText, fontSize = 11.sp, lineHeight = 16.sp)
    }
}


@Composable
private fun ScreenTesterScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var testMode by remember { mutableStateOf("MENU") }
    var colorIndex by remember { mutableIntStateOf(0) }
    val testColors = listOf("White" to Color.White, "Black" to Color.Black, "Red" to Color.Red, "Green" to Color.Green, "Blue" to Color.Blue)
    val touchedCells = remember { mutableStateListOf<Int>() }
    val touchMarks = remember { mutableStateListOf<Offset>() }
    val activeTouches = remember { mutableStateListOf<Offset>() }
    val metrics = remember {
        val dm = context.resources.displayMetrics
        @Suppress("DEPRECATION")
        val refresh = (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager)
            .defaultDisplay.refreshRate
        Triple(dm.widthPixels, dm.heightPixels, refresh)
    }

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (testMode == "MENU") onBack() else testMode = "MENU" }) {
                Text(if (testMode == "MENU") "‹  Back" else "‹  Tests", color = Accent)
            }
            Spacer(Modifier.weight(1f))
            Text("SCREEN TESTER", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        when (testMode) {
            "DISPLAY" -> {
                Text("Display colour test", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Look for dead pixels, discoloration or uneven brightness.", color = SecondaryText, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.weight(1f).fillMaxWidth().background(testColors[colorIndex].second, RoundedCornerShape(12.dp))
                        .clickable { colorIndex = (colorIndex + 1) % testColors.size },
                    contentAlignment = Alignment.Center
                ) {
                    Text(testColors[colorIndex].first, color = if (colorIndex == 1) Color.White else Color.DarkGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { colorIndex = (colorIndex - 1 + testColors.size) % testColors.size }, modifier = Modifier.weight(1f)) { Text("Previous", color = Color.White) }
                    Button(onClick = { colorIndex = (colorIndex + 1) % testColors.size }, modifier = Modifier.weight(1f)) { Text("Next colour") }
                }
            }
            "TOUCH" -> {
                Text("Touch drawing test", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Drag your finger across the area. Use multiple fingers to test multi-touch.", color = SecondaryText, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.weight(1f).fillMaxWidth().background(Color(0xFF080D16), RoundedCornerShape(16.dp))
                        .pointerInteropFilter { event ->
                            when (event.actionMasked) {
                                android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_POINTER_DOWN, android.view.MotionEvent.ACTION_MOVE -> {
                                    activeTouches.clear()
                                    for (i in 0 until event.pointerCount) {
                                        val point = Offset(event.getX(i), event.getY(i))
                                        activeTouches.add(point)
                                        if (event.actionMasked != android.view.MotionEvent.ACTION_MOVE || i == event.actionIndex) touchMarks.add(point)
                                    }
                                    while (touchMarks.size > 1800) touchMarks.removeAt(0)
                                }
                                android.view.MotionEvent.ACTION_POINTER_UP -> {
                                    activeTouches.clear()
                                    for (i in 0 until event.pointerCount) if (i != event.actionIndex) activeTouches.add(Offset(event.getX(i), event.getY(i)))
                                }
                                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> activeTouches.clear()
                            }
                            true
                        }
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        touchMarks.forEach { drawCircle(Color(0xFF64B5F6), radius = 5f, center = it) }
                        activeTouches.forEach { drawCircle(Color(0xFFFFD54F), radius = 24f, center = it, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)) }
                    }
                    Text("${activeTouches.size} active touch point(s)", color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(12.dp), fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Touch points recorded: ${touchMarks.size}", color = SecondaryText, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    TextButton(onClick = { touchMarks.clear(); activeTouches.clear() }) { Text("Clear", color = Accent) }
                }
            }
            "GRID" -> {
                Text("Touch grid test", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tap each square. Each should change colour when touched.", color = SecondaryText, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(8) { row ->
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(5) { column ->
                                val index = row * 5 + column
                                Box(
                                    Modifier.weight(1f).fillMaxHeight().background(if (touchedCells.contains(index)) Color(0xFF2E7D5B) else SurfaceDark, RoundedCornerShape(7.dp))
                                        .clickable { if (!touchedCells.contains(index)) touchedCells.add(index) },
                                    contentAlignment = Alignment.Center
                                ) { Text(if (touchedCells.contains(index)) "✓" else "${index + 1}", color = Color.White, fontSize = 12.sp) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${touchedCells.size}/40 areas tested", color = SecondaryText, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    TextButton(onClick = { touchedCells.clear() }) { Text("Reset", color = Accent) }
                }
            }
            else -> {
                Text("Test your display and touchscreen", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Run these checks to find display or touch issues.", color = SecondaryText, fontSize = 13.sp)
                Spacer(Modifier.height(22.dp))
                listOf(
                    Triple("Display colour test", "Show white, black, red, green and blue screens", "DISPLAY"),
                    Triple("Touch drawing test", "Draw with one or more fingers", "TOUCH"),
                    Triple("Touch grid test", "Check all 40 screen areas", "GRID")
                ).forEach { item ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                        testMode = item.third
                        if (item.third == "TOUCH") { touchMarks.clear(); activeTouches.clear() }
                    }, colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(17.dp)) {
                            Text(item.first, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text(item.second, color = SecondaryText, fontSize = 12.sp)
                        }
                    }
                }
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("SCREEN INFORMATION", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text("Resolution: ${metrics.first} × ${metrics.second} pixels", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("Reported refresh rate: ${"%.1f".format(metrics.third)} Hz", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


@Composable
private fun ScreenRulerScreen(onBack: () -> Unit) {
    val activity = androidx.compose.ui.platform.LocalContext.current as Activity
    DisposableEffect(activity) {
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    var unit by remember { mutableStateOf("cm") }
    var calibration by remember { mutableFloatStateOf(1f) }
    val scrollState = rememberScrollState()
    val pxPerMm = with(density) { 160f / 25.4f * density.density } * calibration

    Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent) }
            Spacer(Modifier.weight(1f))
            Text("SCREEN RULER", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("On-screen ruler", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Place a small object against the scale. Scroll horizontally to see more.", color = SecondaryText, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = unit == "cm", onClick = { unit = "cm" }, label = { Text("Centimetres") })
            FilterChip(selected = unit == "inch", onClick = { unit = "inch" }, label = { Text("Inches") })
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (unit == "cm") "Millimetre ticks · numbered every centimetre" else "Millimetre ticks · numbered every inch",
            color = SecondaryText,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF5F2E9))
                .horizontalScroll(scrollState)
        ) {
            Canvas(Modifier.width(1600.dp).fillMaxHeight()) {
                val step = pxPerMm
                val rulerHeight = size.height
                val totalTicks = (size.width / step).toInt()
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 12f * density.density
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                for (mm in 0..totalTicks) {
                    val x = mm * step
                    val inchNumber = (mm / 25.4f + 0.5f).toInt()
                    val isMajor = if (unit == "cm") mm % 10 == 0 else mm == (inchNumber * 25.4f + 0.5f).toInt()
                    val isMid = mm % 5 == 0
                    val tickHeight = when {
                        isMajor -> rulerHeight * 0.62f
                        isMid -> rulerHeight * 0.38f
                        else -> rulerHeight * 0.20f
                    }
                    drawLine(
                        color = Color.Black,
                        start = Offset(x, 0f),
                        end = Offset(x, tickHeight),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                    if (isMajor) {
                        val label = if (unit == "cm") (mm / 10).toString() else inchNumber.toString()
                        drawContext.canvas.nativeCanvas.drawText(label, x + 1.dp.toPx(), rulerHeight * 0.88f, paint)
                    }
                }
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, rulerHeight * 0.64f),
                    end = Offset(size.width, rulerHeight * 0.64f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("← Scroll ruler →", color = SecondaryText, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(20.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("CALIBRATION", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text("Adjust the scale to match a known physical measurement.", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Calibration: " + String.format(java.util.Locale.US, "%.0f", calibration * 100f) + "%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Slider(value = calibration, onValueChange = { calibration = it }, valueRange = 0.75f..1.25f)
                Text("Tip: Use a bank card's standard 85.6 mm width as a reference. Align its edge with zero and adjust until its other edge matches 85.6 mm.", color = SecondaryText, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { calibration = 1f }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reset calibration", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Accuracy depends on your phone's reported screen density. Calibrate before measuring; this is not a precision instrument.", color = SecondaryText, fontSize = 11.sp)
    }
}


@Composable
private fun ProtractorScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
    var angle by remember { mutableFloatStateOf(45f) }
    var orientation by remember { mutableStateOf("Portrait") }
    var cameraMode by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var previewView by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) cameraError = "Camera permission was not granted."
    }

    DisposableEffect(activity) {
        val previousOrientation = activity.requestedOrientation
        onDispose { activity.requestedOrientation = previousOrientation }
    }

    LaunchedEffect(orientation) {
        activity.requestedOrientation = if (orientation == "Landscape") {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(previewView, cameraMode, hasCameraPermission) {
        val view = previewView
        if (cameraMode && hasCameraPermission && view != null) {
            try {
                val providerFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    try {
                        val provider = providerFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.surfaceProvider = view.surfaceProvider
                        }
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                            preview
                        )
                        cameraError = null
                    } catch (exception: Exception) {
                        cameraError = exception.localizedMessage ?: "Unable to start the camera preview."
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (exception: Exception) {
                cameraError = exception.localizedMessage ?: "Camera is unavailable."
            }
        }
    }

    DisposableEffect(previewView, cameraMode, hasCameraPermission) {
        onDispose {
            if (cameraMode && hasCameraPermission) {
                runCatching {
                    androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context).get().unbindAll()
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent) }
            Spacer(Modifier.weight(1f))
            Text("PROTRACTOR", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("Angle protractor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Choose the screen orientation and measurement view.", color = SecondaryText, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        Text("SCREEN ORIENTATION", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = orientation == "Portrait",
                onClick = { orientation = "Portrait" },
                label = { Text("Portrait") }
            )
            FilterChip(
                selected = orientation == "Landscape",
                onClick = { orientation = "Landscape" },
                label = { Text("Landscape") }
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("MEASUREMENT VIEW", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            FilterChip(
                selected = !cameraMode,
                onClick = { cameraMode = false },
                label = { Text("Scale") }
            )
            Spacer(Modifier.width(6.dp))
            FilterChip(
                selected = cameraMode,
                onClick = {
                    cameraMode = true
                    if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                label = { Text("Live camera") }
            )
        }
        Spacer(Modifier.height(12.dp))

        if (cameraMode && !hasCameraPermission) {
            Card(
                Modifier.fillMaxWidth().height(230.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera access is needed to show the live view.", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant camera access")
                    }
                }
            }
        } else if (cameraMode) {
            Box(
                Modifier.fillMaxWidth()
                    .height(if (orientation == "Landscape") 250.dp else 320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { viewContext ->
                        androidx.camera.view.PreviewView(viewContext).apply {
                            scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                            implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(Modifier.fillMaxSize()) {
                    val radius = (size.width * 0.46f).coerceAtMost(size.height * 0.82f)
                    val center = Offset(size.width / 2f, size.height - 18.dp.toPx())
                    val protractorTop = center.y - radius
                    drawArc(
                        color = Color.White.copy(alpha = 0.92f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, protractorTop),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    drawLine(Color.White.copy(alpha = 0.92f), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), 2.dp.toPx())
                    for (degree in 0..180) {
                        val radians = Math.toRadians((180 - degree).toDouble())
                        val cosA = kotlin.math.cos(radians).toFloat()
                        val sinA = kotlin.math.sin(radians).toFloat()
                        val tickLength = when {
                            degree % 10 == 0 -> 15.dp.toPx()
                            degree % 5 == 0 -> 10.dp.toPx()
                            else -> 5.dp.toPx()
                        }
                        drawLine(
                            Color.White.copy(alpha = 0.95f),
                            Offset(center.x + (radius - tickLength) * cosA, center.y - (radius - tickLength) * sinA),
                            Offset(center.x + radius * cosA, center.y - radius * sinA),
                            if (degree % 10 == 0) 1.5.dp.toPx() else 0.8.dp.toPx()
                        )
                        if (degree % 10 == 0) {
                            val labelRadius = radius - 27.dp.toPx()
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 10.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                degree.toString(),
                                center.x + labelRadius * cosA,
                                center.y - labelRadius * sinA + 4.dp.toPx(),
                                paint
                            )
                        }
                    }
                    val pointerRadians = Math.toRadians((180f - angle).toDouble())
                    val pointerEnd = Offset(
                        center.x + (radius - 18.dp.toPx()) * kotlin.math.cos(pointerRadians).toFloat(),
                        center.y - (radius - 18.dp.toPx()) * kotlin.math.sin(pointerRadians).toFloat()
                    )
                    drawLine(Color(0xFFFF5252), center, pointerEnd, 3.dp.toPx())
                    drawCircle(Color(0xFFFF5252), radius = 5.dp.toPx(), center = center)
                }
            }
            cameraError?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = Color(0xFFFFB74D), fontSize = 11.sp)
            }
            Text("The protractor is overlaid on the live rear-camera image. Align its baseline with one edge and the red arm with the other.", color = SecondaryText, fontSize = 11.sp, lineHeight = 16.sp)
        } else {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Canvas(Modifier.fillMaxWidth().height(230.dp).padding(horizontal = 8.dp, vertical = 8.dp)) {
                    val radius = (size.width / 2f - 14.dp.toPx()).coerceAtMost(size.height - 18.dp.toPx())
                    val center = Offset(size.width / 2f, size.height - 12.dp.toPx())
                    drawArc(
                        color = Color(0xFF263238),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    drawLine(Color(0xFF263238), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), 2.dp.toPx())
                    for (degree in 0..180) {
                        val radians = Math.toRadians((180 - degree).toDouble())
                        val cosA = kotlin.math.cos(radians).toFloat()
                        val sinA = kotlin.math.sin(radians).toFloat()
                        val tickLength = when {
                            degree % 10 == 0 -> 17.dp.toPx()
                            degree % 5 == 0 -> 11.dp.toPx()
                            else -> 6.dp.toPx()
                        }
                        drawLine(
                            Color(0xFF263238),
                            Offset(center.x + (radius - tickLength) * cosA, center.y - (radius - tickLength) * sinA),
                            Offset(center.x + radius * cosA, center.y - radius * sinA),
                            if (degree % 10 == 0) 1.5.dp.toPx() else 0.8.dp.toPx()
                        )
                        if (degree % 10 == 0) {
                            val labelRadius = radius - 31.dp.toPx()
                            drawContext.canvas.nativeCanvas.drawText(
                                degree.toString(),
                                center.x + labelRadius * cosA,
                                center.y - labelRadius * sinA + 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 10.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                    val pointerRadians = Math.toRadians((180f - angle).toDouble())
                    val pointerEnd = Offset(
                        center.x + (radius - 22.dp.toPx()) * kotlin.math.cos(pointerRadians).toFloat(),
                        center.y - (radius - 22.dp.toPx()) * kotlin.math.sin(pointerRadians).toFloat()
                    )
                    drawLine(Color(0xFFD32F2F), center, pointerEnd, 3.dp.toPx())
                    drawCircle(Color(0xFFD32F2F), radius = 5.dp.toPx(), center = center)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("MEASURED ANGLE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(angle.toInt().toString(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(5.dp))
                    Text("°", color = Accent, fontSize = 30.sp)
                }
                Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..180f, steps = 179)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0°", color = SecondaryText, fontSize = 11.sp)
                    Text("90°", color = SecondaryText, fontSize = 11.sp)
                    Text("180°", color = SecondaryText, fontSize = 11.sp)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { angle = 90f }, modifier = Modifier.fillMaxWidth()) {
                    Text("Set to 90°", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "This is an alignment guide, not automatic angle recognition. Use the slider to position the red arm; the camera view is live and is not recorded.",
            color = SecondaryText,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}



@Composable
private fun ARMeasureScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }
    var glView by remember { mutableStateOf<android.opengl.GLSurfaceView?>(null) }
    var renderer by remember { mutableStateOf<ARMeasureRenderer?>(null) }
    var arSession by remember { mutableStateOf<com.google.ar.core.Session?>(null) }
    var status by remember { mutableStateOf("Tap Start AR to initialise plane detection.") }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var firstPoint by remember { mutableStateOf<Offset?>(null) }
    var secondPoint by remember { mutableStateOf<Offset?>(null) }
    var distanceMeters by remember { mutableStateOf<Float?>(null) }
    var viewSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var installRequested by remember { mutableStateOf(false) }

    fun stopAr() {
        runCatching { glView?.onPause() }
        runCatching { arSession?.pause() }
        runCatching { arSession?.close() }
        glView = null
        renderer = null
        arSession = null
    }

    val currentArSession by rememberUpdatedState(arSession)
    val currentGlView by rememberUpdatedState(glView)

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    runCatching { currentArSession?.resume() }
                    runCatching { currentGlView?.onResume() }
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    runCatching { currentGlView?.onPause() }
                    runCatching { currentArSession?.pause() }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopAr()
        }
    }

    fun startAr() {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        try {
            val installStatus = com.google.ar.core.ArCoreApk.getInstance()
                .requestInstall(activity, !installRequested)
            if (installStatus == com.google.ar.core.ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                installRequested = true
                status = "Install or update Google Play Services for AR, then open AR Measure again."
                return
            }
            val session = com.google.ar.core.Session(context)
            val config = com.google.ar.core.Config(session).apply {
                planeFindingMode = com.google.ar.core.Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                updateMode = com.google.ar.core.Config.UpdateMode.LATEST_CAMERA_IMAGE
            }
            session.configure(config)
            val gl = android.opengl.GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                preserveEGLContextOnPause = true
                val newRenderer = ARMeasureRenderer(
                    session,
                    onPointAdded = { x, y, metres ->
                        activity.runOnUiThread {
                            val point = Offset(x * viewSize.width, y * viewSize.height)
                            if (firstPoint == null || secondPoint != null) {
                                firstPoint = point
                                secondPoint = null
                                distanceMeters = null
                                status = "First point placed. Tap a second point on the detected surface."
                            } else {
                                secondPoint = point
                                distanceMeters = metres
                                status = if (metres != null) "Measurement complete. Tap again to measure another distance."
                                    else "Second point placed. Distance unavailable; try again."
                            }
                        }
                    },
                    onTapMiss = {
                        activity.runOnUiThread {
                            status = "No detected surface at that spot. Move the phone slowly, wait for tracking, then tap a visible floor or wall."
                        }
                    }
                )
                renderer = newRenderer
                setRenderer(newRenderer)
                renderMode = android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
                setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        newRenderer.queueHitTest(this, event.x, event.y, width.toFloat().coerceAtLeast(1f), height.toFloat().coerceAtLeast(1f))
                    }
                    true
                }
            }
            arSession = session
            glView = gl
            session.resume()
            gl.onResume()
            status = "Move the phone slowly until ARCore detects a surface."
            cameraError = null
        } catch (e: com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException) {
            cameraError = "Google Play Services for AR is not installed. Install it from Play Store."
        } catch (e: com.google.ar.core.exceptions.UnavailableApkTooOldException) {
            cameraError = "Update Google Play Services for AR."
        } catch (e: com.google.ar.core.exceptions.UnavailableSdkTooOldException) {
            cameraError = "This ARCore SDK is too old for the installed AR service."
        } catch (e: com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException) {
            cameraError = "This phone does not support ARCore."
        } catch (e: Exception) {
            cameraError = e.localizedMessage ?: "Unable to start AR measurement."
        }
    }

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Accent) }
            Spacer(Modifier.weight(1f))
            Text("AR MEASURE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("Measure real-world distance", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("ARCore detects surfaces and measures between two points.", color = SecondaryText, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        if (glView == null) {
            Card(Modifier.fillMaxWidth().height(210.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷", fontSize = 38.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Camera-based AR measurement", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Requires an ARCore-compatible Android phone and Google Play Services for AR.", color = SecondaryText, fontSize = 11.sp)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Accent)
                            .clickable { startAr() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (hasCameraPermission) "START AR" else "GRANT CAMERA ACCESS",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF101522),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            Box(
                Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black)
                    .onSizeChanged { viewSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
            ) {
                AndroidView(factory = { glView!! }, modifier = Modifier.fillMaxSize())
                Canvas(Modifier.fillMaxSize()) {
                    val p1 = firstPoint
                    val p2 = secondPoint
                    if (p1 != null && p2 != null) drawLine(Color(0xFFFF5252), p1, p2, 3.dp.toPx())
                    listOfNotNull(p1, p2).forEach {
                        drawCircle(Color(0xFFFF5252), 9.dp.toPx(), it)
                        drawCircle(Color.White, 3.dp.toPx(), it)
                    }
                }
                Text("Tap two points on a detected surface", Modifier.align(Alignment.TopCenter).padding(10.dp).background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp)
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = {
                firstPoint = null
                secondPoint = null
                distanceMeters = null
                renderer?.resetMeasurement(glView!!)
                status = "Measurement cleared. Tap the first point."
            }) { Text("Reset measurement", color = Accent) }
            OutlinedButton(onClick = {
                stopAr()
                status = "AR session stopped."
            }, modifier = Modifier.fillMaxWidth()) { Text("Stop AR", color = Color.White) }
        }

        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("MEASUREMENT", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(distanceMeters?.let { "%.2f m  ·  %.1f cm".format(it, it * 100f) } ?: "—", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(status, color = SecondaryText, fontSize = 12.sp, lineHeight = 17.sp)
                cameraError?.let { Spacer(Modifier.height(6.dp)); Text(it, color = Color(0xFFFFB74D), fontSize = 12.sp) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("How to use: start AR, slowly move the phone to scan the floor, wall or another textured surface. Tap the first point, then the second point. Measurements are estimates and depend on lighting, surface texture and tracking quality.", color = SecondaryText, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

private class ARMeasureRenderer(
    private val session: com.google.ar.core.Session,
    private val onPointAdded: (Float, Float, Float?) -> Unit,
    private val onTapMiss: () -> Unit
) : android.opengl.GLSurfaceView.Renderer {
    @Volatile private var frame: com.google.ar.core.Frame? = null
    private val anchors = mutableListOf<com.google.ar.core.Anchor>()
    private var textureId = 0
    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private val vertices = floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f)
    // Flip the camera texture vertically so the AR preview is upright on portrait devices.
    private val uvs = floatArrayOf(0f,1f, 1f,1f, 0f,0f, 1f,0f)
    private val vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
    private val uvBuffer = java.nio.ByteBuffer.allocateDirect(uvs.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer().apply { put(uvs); position(0) }

    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        val textures = IntArray(1)
        android.opengl.GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        android.opengl.GLES20.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        android.opengl.GLES20.glTexParameteri(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, android.opengl.GLES20.GL_TEXTURE_MIN_FILTER, android.opengl.GLES20.GL_LINEAR)
        android.opengl.GLES20.glTexParameteri(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, android.opengl.GLES20.GL_TEXTURE_MAG_FILTER, android.opengl.GLES20.GL_LINEAR)
        android.opengl.GLES20.glTexParameteri(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, android.opengl.GLES20.GL_TEXTURE_WRAP_S, android.opengl.GLES20.GL_CLAMP_TO_EDGE)
        android.opengl.GLES20.glTexParameteri(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, android.opengl.GLES20.GL_TEXTURE_WRAP_T, android.opengl.GLES20.GL_CLAMP_TO_EDGE)
        session.setCameraTextureName(textureId)
        val vertexShader = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() { gl_Position = a_Position; v_TexCoord = a_TexCoord; }
        """
        val fragmentShader = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES u_Texture;
            varying vec2 v_TexCoord;
            void main() { gl_FragColor = texture2D(u_Texture, v_TexCoord); }
        """
        program = createProgram(vertexShader, fragmentShader)
        positionHandle = android.opengl.GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = android.opengl.GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureHandle = android.opengl.GLES20.glGetUniformLocation(program, "u_Texture")
    }

    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
        android.opengl.GLES20.glViewport(0, 0, width, height)
        session.setDisplayGeometry(android.view.Surface.ROTATION_0, width, height)
    }

    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
        try {
            val current = session.update()
            frame = current
            android.opengl.GLES20.glClear(android.opengl.GLES20.GL_COLOR_BUFFER_BIT or android.opengl.GLES20.GL_DEPTH_BUFFER_BIT)
            android.opengl.GLES20.glUseProgram(program)
            val transformed = java.nio.ByteBuffer.allocateDirect(uvs.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
            current.transformDisplayUvCoords(uvBuffer, transformed)
            vertexBuffer.position(0)
            transformed.position(0)
            android.opengl.GLES20.glEnableVertexAttribArray(positionHandle)
            android.opengl.GLES20.glVertexAttribPointer(positionHandle, 2, android.opengl.GLES20.GL_FLOAT, false, 0, vertexBuffer)
            android.opengl.GLES20.glEnableVertexAttribArray(texCoordHandle)
            android.opengl.GLES20.glVertexAttribPointer(texCoordHandle, 2, android.opengl.GLES20.GL_FLOAT, false, 0, transformed)
            android.opengl.GLES20.glActiveTexture(android.opengl.GLES20.GL_TEXTURE0)
            android.opengl.GLES20.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            android.opengl.GLES20.glUniform1i(textureHandle, 0)
            android.opengl.GLES20.glDrawArrays(android.opengl.GLES20.GL_TRIANGLE_STRIP, 0, 4)
            android.opengl.GLES20.glDisableVertexAttribArray(positionHandle)
            android.opengl.GLES20.glDisableVertexAttribArray(texCoordHandle)
        } catch (_: com.google.ar.core.exceptions.CameraNotAvailableException) {
            // The activity may be transitioning between foreground and background.
        } catch (_: Exception) {
            // Wait for the next camera frame.
        }
    }

        fun queueHitTest(view: android.opengl.GLSurfaceView, x: Float, y: Float, width: Float, height: Float) {
        view.queueEvent {
            val current = frame ?: return@queueEvent
            try {
                val hit = current.hitTest(x, y).firstOrNull { result ->
                    val trackable = result.trackable
                    trackable is com.google.ar.core.Plane &&
                        trackable.trackingState == com.google.ar.core.TrackingState.TRACKING &&
                        trackable.isPoseInPolygon(result.hitPose)
                } ?: run {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onTapMiss() }
                    return@queueEvent
                }
                val anchor = hit.createAnchor()
                if (anchors.size >= 2) {
                    anchors.forEach { runCatching { it.detach() } }
                    anchors.clear()
                }
                anchors.add(anchor)
                val distance = if (anchors.size == 2) {
                    val a = anchors[0].pose
                    val b = anchors[1].pose
                    val dx = a.tx() - b.tx()
                    val dy = a.ty() - b.ty()
                    val dz = a.tz() - b.tz()
                    kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                } else null
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onPointAdded((x / width).coerceIn(0f, 1f), (y / height).coerceIn(0f, 1f), distance)
                }
            } catch (_: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { onTapMiss() }
            }
        }
    }

    fun resetMeasurement(view: android.opengl.GLSurfaceView) {
        view.queueEvent {
            anchors.forEach { runCatching { it.detach() } }
            anchors.clear()
        }
    }

    private fun createProgram(vertex: String, fragment: String): Int {
        fun compile(type: Int, source: String): Int {
            val shader = android.opengl.GLES20.glCreateShader(type)
            android.opengl.GLES20.glShaderSource(shader, source)
            android.opengl.GLES20.glCompileShader(shader)
            return shader
        }
        val vs = compile(android.opengl.GLES20.GL_VERTEX_SHADER, vertex)
        val fs = compile(android.opengl.GLES20.GL_FRAGMENT_SHADER, fragment)
        return android.opengl.GLES20.glCreateProgram().also {
            android.opengl.GLES20.glAttachShader(it, vs)
            android.opengl.GLES20.glAttachShader(it, fs)
            android.opengl.GLES20.glLinkProgram(it)
        }
    }
}


@Composable
fun BubbleLevelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
    }

    var rawX by remember { mutableFloatStateOf(0f) }
    var rawY by remember { mutableFloatStateOf(0f) }
    var calibratedX by remember { mutableFloatStateOf(0f) }
    var calibratedY by remember { mutableFloatStateOf(0f) }
    var sensorReady by remember { mutableStateOf(false) }
    var sensorAccuracy by remember { mutableIntStateOf(android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE) }

    DisposableEffect(sensorManager, accelerometer) {
        if (accelerometer != null) {
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    val values = event.values
                    rawX = Math.toDegrees(kotlin.math.atan2(values[0].toDouble(), values[2].toDouble())).toFloat()
                    rawY = Math.toDegrees(kotlin.math.atan2(values[1].toDouble(), values[2].toDouble())).toFloat()
                    sensorReady = true
                }

                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                    sensorAccuracy = accuracy
                }
            }
            sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        } else {
            onDispose { }
        }
    }

    val xAngle = rawX - calibratedX
    val yAngle = rawY - calibratedY
    val isLevel = kotlin.math.abs(xAngle) < 1.0f && kotlin.math.abs(yAngle) < 1.0f
    val accuracyText = when (sensorAccuracy) {
        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
        android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
        else -> "Uncalibrated"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 14.dp))
            Column {
                Text("Bubble Level", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Check whether a surface is level", color = SecondaryText, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(22.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (!sensorReady) "Waiting for accelerometer…" else if (isLevel) "LEVEL" else "NOT LEVEL",
                    color = if (isLevel && sensorReady) Color(0xFF66BB6A) else if (sensorReady) Color(0xFFFFB74D) else SecondaryText,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val radius = size.minDimension * 0.43f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(Color(0xFF263B57), radius, center)
                    drawCircle(Color(0xFF7892AE), radius, center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                    drawCircle(Color(0xFF9AA9BD), radius * 0.55f, center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                    drawLine(Color(0xFF9AA9BD), Offset(center.x - radius, center.y),
                        Offset(center.x + radius, center.y), 1.dp.toPx())
                    drawLine(Color(0xFF9AA9BD), Offset(center.x, center.y - radius),
                        Offset(center.x, center.y + radius), 1.dp.toPx())
                    val bubbleX = (xAngle / 15f).coerceIn(-1f, 1f) * radius * 0.72f
                    val bubbleY = (yAngle / 15f).coerceIn(-1f, 1f) * radius * 0.72f
                    val bubbleColor = if (isLevel) Color(0xFF66BB6A) else Accent
                    drawCircle(bubbleColor.copy(alpha = 0.25f), radius * 0.16f, center + Offset(bubbleX, bubbleY))
                    drawCircle(bubbleColor, radius * 0.105f, center + Offset(bubbleX, bubbleY))
                }
                Text("Place the phone flat on the surface", color = SecondaryText, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("X AXIS", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(String.format(java.util.Locale.US, "%.1f", xAngle) + "°",
                        color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Y AXIS", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(String.format(java.util.Locale.US, "%.1f", yAngle) + "°",
                        color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            calibratedX = rawX
            calibratedY = rawY
        }, modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(14.dp)) {
            Text("CALIBRATE ZERO", color = Color(0xFF101522), fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = {
            calibratedX = 0f
            calibratedY = 0f
        }) {
            Text("Reset calibration", color = SecondaryText)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (accelerometer == null) "This phone does not have an accelerometer."
            else "Sensor accuracy: " + accuracyText,
            color = SecondaryText, fontSize = 12.sp
        )
        Text(
            "For best results, place the phone flat and still. Calibration sets the current position as zero; it does not correct the phone's physical sensor bias.",
            color = SecondaryText, fontSize = 12.sp
        )
    }
}



@Composable
fun InternetSpeedScreen(onBack: () -> Unit) {
    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("Ready to test your connection") }
    var ping by remember { mutableStateOf("--") }
    var down by remember { mutableStateOf("--") }
    var up by remember { mutableStateOf("--") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun transfer(urlText: String, upload: Boolean): Double =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val payload = if (upload) ByteArray(2_000_000) { (it % 251).toByte() } else null
            val url = java.net.URL(urlText + (if (urlText.contains("?")) "&" else "?") + "cacheBust=" + System.nanoTime())
            val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = if (upload) "POST" else "GET"
                connectTimeout = 15000
                readTimeout = 30000
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("Accept-Encoding", "identity")
                if (upload) {
                    doOutput = true
                    setFixedLengthStreamingMode(payload!!.size)
                    setRequestProperty("Content-Type", "application/octet-stream")
                }
            }
            try {
                val start = System.nanoTime()
                var bytes = 0L
                if (upload) {
                    connection.outputStream.use { it.write(payload!!) }
                    bytes = payload!!.size.toLong()
                } else {
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(65536)
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            bytes += n
                        }
                    }
                }
                val code = connection.responseCode
                if (code !in 200..299 || bytes == 0L) throw IllegalStateException("Speed-test server returned HTTP " + code)
                if (upload) connection.inputStream.use { input -> while (input.read() != -1) { } }
                val seconds = (System.nanoTime() - start).toDouble() / 1_000_000_000.0
                bytes * 8.0 / seconds / 1_000_000.0
            } finally {
                connection.disconnect()
            }
        }

    suspend fun pingOnce(): Long = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = java.net.URL("https://speed.cloudflare.com/__down?bytes=1&cacheBust=" + System.nanoTime())
        val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            useCaches = false
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            val start = System.nanoTime()
            connection.inputStream.use { input -> while (input.read() != -1) { } }
            if (connection.responseCode !in 200..299) throw IllegalStateException("Latency request failed")
            (System.nanoTime() - start) / 1_000_000L
        } finally {
            connection.disconnect()
        }
    }

    fun formatted(value: Double) = String.format(java.util.Locale.US, "%.1f Mbps", value)

    Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.clickable { if (!running) onBack() }.padding(end = 14.dp))
            Column {
                Text("Internet Speed Test", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Check your current network performance", color = SecondaryText, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AVERAGE LATENCY", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(ping, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("Ping / response time", color = SecondaryText, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.weight(1f), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↓ DOWNLOAD", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(down, color = Color(0xFF66BB6A), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(Modifier.weight(1f), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↑ UPLOAD", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(up, color = Color(0xFFFFB74D), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (running) CircularProgressIndicator(color = Accent)
        Spacer(Modifier.height(10.dp))
        Text(phase, color = if (error == null) SecondaryText else Color(0xFFFF8A80), fontSize = 13.sp)
        if (error != null) Text(error!!, color = Color(0xFFFF8A80), fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = {
            if (!running) scope.launch {
                running = true
                error = null
                ping = "--"; down = "--"; up = "--"
                try {
                    phase = "Measuring latency…"
                    val samples = mutableListOf<Long>()
                    repeat(3) { samples.add(pingOnce()) }
                    ping = String.format(java.util.Locale.US, "%.0f ms", samples.average())
                    phase = "Measuring download speed…"
                    down = formatted(transfer("https://speed.cloudflare.com/__down?bytes=8000000", false))
                    phase = "Measuring upload speed…"
                    up = formatted(transfer("https://speed.cloudflare.com/__up", true))
                    phase = "Test completed"
                } catch (e: Exception) {
                    phase = "Test could not be completed"
                    error = e.message ?: "Check your internet connection and try again."
                } finally { running = false }
            }
        }, enabled = !running, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent), shape = RoundedCornerShape(14.dp)) {
            Text(if (running) "TESTING…" else "START SPEED TEST",
                color = Color(0xFF101522), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF172131))) {
            Column(Modifier.padding(16.dp)) {
                Text("About this test", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Uses Cloudflare's speed-test service. Each run transfers approximately 10 MB (8 MB download and 2 MB upload), plus small latency requests. Results vary with signal strength, congestion and server distance.",
                    color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("For a reliable result, pause downloads and VPNs and run the test more than once.",
                    color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun BatteryAmpereScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val batteryManager = remember {
        context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
    }
    var currentMicroAmps by remember { mutableLongStateOf(Long.MIN_VALUE) }
    var batteryPercent by remember { mutableIntStateOf(-1) }
    var batteryStatus by remember { mutableIntStateOf(-1) }
    var updated by remember { mutableStateOf(false) }

    LaunchedEffect(batteryManager) {
        while (true) {
            val current = runCatching {
                batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            }.getOrDefault(Int.MIN_VALUE)
            currentMicroAmps = current.toLong()

            val batteryIntent = context.registerReceiver(
                null,
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            )
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100)
                batteryPercent = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
                batteryStatus = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            }
            updated = true
            kotlinx.coroutines.delay(1000L)
        }
    }

    val supported = currentMicroAmps != Int.MIN_VALUE.toLong()
    val amps = if (supported) currentMicroAmps.toDouble() / 1_000_000.0 else 0.0
    val charging = amps > 0.0005
    val discharging = amps < -0.0005
    val currentColor = when {
        charging -> Color(0xFF66BB6A)
        discharging -> Color(0xFFFF8A65)
        else -> Color.White
    }
    val currentText = if (supported) {
        String.format(java.util.Locale.US, "%+.3f A", amps)
    } else {
        "N/A"
    }
    val stateText = when {
        !updated -> "Reading battery…"
        !supported -> "Current measurement unavailable"
        charging -> "CHARGING"
        discharging -> "DISCHARGING"
        batteryStatus == android.os.BatteryManager.BATTERY_STATUS_FULL -> "FULL"
        batteryStatus == android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "CONNECTED / IDLE"
        else -> "IDLE"
    }
    val statusDetail = when (batteryStatus) {
        android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charger connected"
        android.os.BatteryManager.BATTERY_STATUS_FULL -> "Battery fully charged"
        android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Running on battery"
        android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Battery status"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 14.dp))
            Column {
                Text("Battery Ampere", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Live charging and discharge current", color = SecondaryText, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BATTERY CURRENT", color = SecondaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(currentText, color = currentColor, fontSize = 46.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(stateText, color = currentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Updates approximately every second", color = SecondaryText, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Battery level", color = SecondaryText, fontSize = 14.sp)
                    Text(if (batteryPercent >= 0) "$batteryPercent%" else "—",
                        color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                if (batteryPercent >= 0) {
                    LinearProgressIndicator(
                        progress = { batteryPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                        color = Accent,
                        trackColor = Color(0xFF334155)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(statusDetail, color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("How to read the value", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("+ positive A  •  Battery charging", color = Color(0xFF66BB6A), fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("− negative A  •  Battery discharging", color = Color(0xFFFF8A65), fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("0.000 A  •  Very low current or idle", color = SecondaryText, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "The reading comes from the phone's battery-current sensor via Android. Some devices do not expose this value, and readings can fluctuate with screen use, temperature and charger behaviour. It is an estimate, not a calibrated power meter.",
            color = SecondaryText, fontSize = 12.sp
        )
    }
}


@Composable
fun CoinTossScreen(onBack: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isFlipping by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var nextResult by remember { mutableStateOf("HEADS") }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 14.dp))
            Column {
                Text("Coin Toss", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("A quick virtual coin flip", color = SecondaryText, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(42.dp))
        Text("TAP THE COIN TO FLIP", color = SecondaryText, fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    rotationY = rotation.value % 360f
                    cameraDistance = 18f * density
                }
                .clip(RoundedCornerShape(120.dp))
                .background(Color(0xFFD69A2D), RoundedCornerShape(120.dp))
                .clickable(enabled = !isFlipping) {
                    if (!isFlipping) {
                        scope.launch {
                            isFlipping = true
                            result = null
                            nextResult = if (kotlin.random.Random.nextBoolean()) "HEADS" else "TAILS"
                            rotation.snapTo(0f)
                            val finalAngle = 1800f + if (nextResult == "TAILS") 180f else 0f
                            rotation.animateTo(
                                targetValue = finalAngle,
                                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
                            )
                            result = nextResult
                            isFlipping = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val showingBack = (rotation.value % 360f) > 90f && (rotation.value % 360f) < 270f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(Color(0xFFFFD66B), RoundedCornerShape(120.dp))
                    .graphicsLayer { scaleX = if (showingBack) -1f else 1f },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (isFlipping) {
                        if (showingBack) "TAILS" else "HEADS"
                    } else {
                        result ?: "FLIP"
                    },
                    color = Color(0xFF70420D),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black
                )
                Text("✦", color = Color(0xFF9B6415), fontSize = 32.sp)
            }
        }

        Spacer(Modifier.height(30.dp))
        Text(
            when {
                isFlipping -> "Flipping…"
                result != null -> "Result: $result"
                else -> "Tap the coin to start"
            },
            color = if (result != null) Color(0xFFFFD166) else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text("Each flip is random. Tap again for another toss.",
            color = SecondaryText, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                if (!isFlipping) {
                    scope.launch {
                        isFlipping = true
                        result = null
                        nextResult = if (kotlin.random.Random.nextBoolean()) "HEADS" else "TAILS"
                        rotation.snapTo(0f)
                        val finalAngle = 1800f + if (nextResult == "TAILS") 180f else 0f
                        rotation.animateTo(finalAngle, tween(1200, easing = LinearEasing))
                        result = nextResult
                        isFlipping = false
                    }
                }
            },
            enabled = !isFlipping,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (isFlipping) "FLIPPING…" else "FLIP COIN",
                color = Color(0xFF101522), fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun DiceRollScreen(onBack: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isRolling by remember { mutableStateOf(false) }
    var diceValue by remember { mutableIntStateOf(1) }
    var hasRolled by remember { mutableStateOf(false) }

    val pipPositions = when (diceValue) {
        1 -> listOf(Offset(0.5f, 0.5f))
        2 -> listOf(Offset(0.25f, 0.25f), Offset(0.75f, 0.75f))
        3 -> listOf(Offset(0.25f, 0.25f), Offset(0.5f, 0.5f), Offset(0.75f, 0.75f))
        4 -> listOf(Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
            Offset(0.25f, 0.75f), Offset(0.75f, 0.75f))
        5 -> listOf(Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
            Offset(0.5f, 0.5f), Offset(0.25f, 0.75f), Offset(0.75f, 0.75f))
        else -> listOf(Offset(0.25f, 0.2f), Offset(0.75f, 0.2f),
            Offset(0.25f, 0.5f), Offset(0.75f, 0.5f),
            Offset(0.25f, 0.8f), Offset(0.75f, 0.8f))
    }

    fun rollDice() {
        if (isRolling) return
        scope.launch {
            isRolling = true
            hasRolled = false
            val result = kotlin.random.Random.nextInt(1, 7)
            rotation.snapTo(0f)
            rotation.animateTo(
                targetValue = 1080f + kotlin.random.Random.nextInt(0, 4) * 90f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            diceValue = result
            hasRolled = true
            isRolling = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 14.dp))
            Column {
                Text("Dice Roll", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Roll a virtual six-sided dice", color = SecondaryText, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(42.dp))
        Text("TAP THE DICE TO ROLL", color = SecondaryText, fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = if (isRolling) 0.88f else 1f
                    scaleY = if (isRolling) 0.88f else 1f
                }
                .clip(RoundedCornerShape(34.dp))
                .background(Color(0xFFF4F6FA), RoundedCornerShape(34.dp))
                .clickable(enabled = !isRolling) { rollDice() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(22.dp)) {
                val dotRadius = size.minDimension * 0.075f
                pipPositions.forEach { position ->
                    drawCircle(
                        color = Color(0xFF172033),
                        radius = dotRadius,
                        center = Offset(size.width * position.x, size.height * position.y)
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        Text(
            when {
                isRolling -> "Rolling…"
                hasRolled -> "You rolled: $diceValue"
                else -> "Tap the dice to start"
            },
            color = if (hasRolled) Color(0xFFFFD166) else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text("Every roll is random. The dice has six possible outcomes.",
            color = SecondaryText, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { rollDice() },
            enabled = !isRolling,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (isRolling) "ROLLING…" else "ROLL DICE",
                color = Color(0xFF101522), fontWeight = FontWeight.Bold)
        }
    }
}


package com.fhbotsv1.multitool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                if (selectedTab == 3) {
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
            AlertDialog(
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

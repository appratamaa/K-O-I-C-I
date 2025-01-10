package org.d3ifcool.koici.smartkoici

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.d3ifcool.koici.smartkoici.ui.theme.SMARTKOICITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMARTKOICITheme {
                AppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val isDarkTheme = remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.k_o_i_c_i),
                                contentDescription = "KOICI Logo",
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDarkTheme.value = !isDarkTheme.value }) {
                            Icon(
                                painter = painterResource(id = if (isDarkTheme.value) R.drawable.baseline_light_mode_24 else R.drawable.baseline_mode_night_24),
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme.value) Color.Black else Color.White)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(if (isDarkTheme.value) Color.Black else Color.White),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SunMoonAnimation(isDarkTheme)
                Spacer(modifier = Modifier.height(16.dp))
                MainContent(isDarkTheme)
            }
        }
    }
}

@Composable
fun SunMoonAnimation(isDarkTheme: MutableState<Boolean>) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer(rotationZ = rotation)
            .shadow(32.dp, shape = CircleShape, clip = false, ambientColor = if (isDarkTheme.value) Color.White else Color.Yellow, spotColor = if (isDarkTheme.value) Color.White else Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        if (isDarkTheme.value) {
            Canvas(modifier = Modifier.size(130.dp)) {
                drawCircle(
                    color = Color.Gray,
                    radius = size.minDimension / 2
                )
            }
            CloudAnimation(isDarkTheme, cloudSize = 60.dp, animationRange = 100f)
        } else {
            Canvas(modifier = Modifier.size(130.dp)) {
                drawCircle(
                    color = Color.Yellow,
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = Color(0xFFFFA000),
                    radius = size.minDimension / 3
                )
            }
            CloudAnimation(isDarkTheme, cloudSize = 60.dp, animationRange = 100f, cloudColor = Color(0xFFFFA000))
        }
    }
}

@Composable
fun CloudAnimation(isDarkTheme: MutableState<Boolean>, cloudSize: Dp, animationRange: Float, cloudColor: Color = Color.Gray) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = if (isDarkTheme.value) -animationRange else animationRange,
        targetValue = if (isDarkTheme.value) animationRange else -animationRange,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = if (isDarkTheme.value) RepeatMode.Restart else RepeatMode.Reverse
        ), label = ""
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp)
                .size(cloudSize)
                .shadow(16.dp, shape = CircleShape, clip = false, ambientColor = cloudColor, spotColor = cloudColor),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(cloudSize - 10.dp)) {
                drawCircle(
                    color = cloudColor,
                    radius = size.minDimension / 2
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = -offsetX.dp)
                .size(cloudSize)
                .shadow(16.dp, shape = CircleShape, clip = false, ambientColor = cloudColor, spotColor = cloudColor),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(cloudSize - 10.dp)) {
                drawCircle(
                    color = cloudColor,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}

@Composable
fun MainContent(isDarkTheme: MutableState<Boolean>) {
    val smokeGuardStatus = remember { mutableStateOf("") }
    val smartBreezeTemp = remember { mutableStateOf("") }
    val pumpify = remember { mutableStateOf("") }
    val waterTankSensor = remember { mutableStateOf("") }
    val isSmokeDetected = remember { mutableStateOf(false) }
    val isWaterFull = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var lastTemperatureNotification by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        fetchFirebaseData("status") {
            smokeGuardStatus.value = it
            if (it == "Terdeteksi asap") {
                isSmokeDetected.value = true
                vibratePhone(context, true)
                showSmokeDetectedNotification(context)
            } else if (it == "Tidak ada asap") {
                isSmokeDetected.value = false
                vibratePhone(context, false)
            }
        }
        fetchFirebaseData("temperature") {
            smartBreezeTemp.value = "$it°C"
            val temperature = it.toFloatOrNull() ?: 0f
            if (lastTemperatureNotification == null || (lastTemperatureNotification!! < 30.0 && temperature >= 30.0) || (lastTemperatureNotification!! >= 30.0 && temperature < 30.0)) {
                showFanStatusNotification(context, if (temperature >= 30.0) "Kipas Menyala. Suhu saat ini $temperature°C" else "Kipas Mati. Suhu saat ini $temperature°C")
                lastTemperatureNotification = temperature
            }
        }
        fetchFirebaseData("pompa") { pumpify.value = it }
        fetchFirebaseData("volume-water-detector/status") {
            waterTankSensor.value = it
            showWaterVolumeNotification(context, it)
            if (it == "Penuh") {
                isWaterFull.value = true
                vibratePhone(context, true)
            } else {
                isWaterFull.value = false
                vibratePhone(context, false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(if (isDarkTheme.value) Color.Black else Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cards = listOf(
            "Smoke Guard" to smokeGuardStatus.value,
            "Smart Breeze" to smartBreezeTemp.value,
            "Pumpify" to pumpify.value,
            "Water Volume" to waterTankSensor.value
        )
        InfoCardGrid(cards = cards, isDarkTheme = isDarkTheme, isSmokeDetected = isSmokeDetected, isWaterFull = isWaterFull)
    }
}

@Composable
fun InfoCardGrid(cards: List<Pair<String, String>>, isDarkTheme: MutableState<Boolean>, isSmokeDetected: MutableState<Boolean>, isWaterFull: MutableState<Boolean>) {
    val poppinsRegular = FontFamily(Font(R.font.poppinsregular))
    val context = LocalContext.current
    var previousTemperature by remember { mutableStateOf<Float?>(null) }
    var previousWaterStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (i in cards.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val cardColor by animateColorAsState(
                    targetValue = if ((isSmokeDetected.value && cards[i].first == "Smoke Guard") || (isWaterFull.value && cards[i].first == "Water Volume")) Color.Red else Color.White,
                    animationSpec = if ((isSmokeDetected.value && cards[i].first == "Smoke Guard") || (isWaterFull.value && cards[i].first == "Water Volume")) infiniteRepeatable(
                        animation = tween(durationMillis = 500),
                        repeatMode = RepeatMode.Reverse
                    ) else tween(durationMillis = 500), label = ""
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .shadow(16.dp, shape = RoundedCornerShape(20.dp), clip = false, ambientColor = if (isDarkTheme.value) Color.White else Color.Black, spotColor = if (isDarkTheme.value) Color.White else Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp,
                        pressedElevation = 20.dp,
                        focusedElevation = 20.dp,
                        hoveredElevation = 20.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cards[i].first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = poppinsRegular
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when (cards[i].first) {
                            "Smart Breeze" -> {
                                val temperature = cards[i].second.removeSuffix("°C").toFloatOrNull() ?: 0f
                                val tempColor = if (temperature >= 30.0) Color.Red else Color.Blue
                                val fanStatus = if (temperature >= 30.0) "Kipas Menyala" else "Kipas Mati"
                                Text(
                                    text = cards[i].second,
                                    fontSize = 32.sp,
                                    color = tempColor,
                                    fontFamily = poppinsRegular,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fanStatus,
                                    fontSize = 14.sp,
                                    color = tempColor,
                                    fontFamily = poppinsRegular,
                                    textAlign = TextAlign.Center
                                )
                                if (previousTemperature != null && previousTemperature!! < 30.0 && temperature >= 30.0) {
                                    showFanStatusNotification(context, "Kipas Menyala. Suhu saat ini $temperature°C")
                                } else if (previousTemperature != null && previousTemperature!! >= 30.0 && temperature < 30.0) {
                                    showFanStatusNotification(context, "Kipas Mati. Suhu saat ini $temperature°C")
                                }
                                previousTemperature = temperature
                            }
                            "Water Volume" -> {
                                Text(
                                    text = cards[i].second,
                                    fontSize = 18.sp,
                                    color = Color.Gray,
                                    fontFamily = poppinsRegular,
                                    textAlign = TextAlign.Center
                                )
                                isWaterFull.value = cards[i].second == "Penuh"
                                if (previousWaterStatus != null && previousWaterStatus != cards[i].second) {
                                    showWaterVolumeNotification(context, cards[i].second)
                                }
                                previousWaterStatus = cards[i].second
                            }
                            else -> {
                                Text(
                                    text = cards[i].second,
                                    fontSize = 18.sp,
                                    color = Color.Gray,
                                    fontFamily = poppinsRegular,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (i + 1 < cards.size) {
                    Spacer(modifier = Modifier.width(16.dp))

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(20.dp), clip = false, ambientColor = if (isDarkTheme.value) Color.White else Color.Black, spotColor = if (isDarkTheme.value) Color.White else Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 16.dp,
                            pressedElevation = 20.dp,
                            focusedElevation = 20.dp,
                            hoveredElevation = 20.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = cards[i + 1].first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontFamily = poppinsRegular
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            when (cards[i + 1].first) {
                                "Smart Breeze" -> {
                                    val temperature = cards[i + 1].second.removeSuffix("°C").toFloatOrNull() ?: 0f
                                    val tempColor = if (temperature >= 30.0) Color.Red else Color.Blue
                                    val fanStatus = if (temperature >= 30.0) "Kipas Menyala" else "Kipas Mati"
                                    Text(
                                        text = cards[i + 1].second,
                                        fontSize = 32.sp,
                                        color = tempColor,
                                        fontFamily = poppinsRegular,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = fanStatus,
                                        fontSize = 14.sp,
                                        color = tempColor,
                                        fontFamily = poppinsRegular,
                                        textAlign = TextAlign.Center
                                    )
                                    if (previousTemperature != null && previousTemperature!! < 30.0 && temperature >= 30.0) {
                                        showFanStatusNotification(context, "Kipas Menyala. Suhu saat ini $temperature°C")
                                    } else if (previousTemperature != null && previousTemperature!! >= 30.0 && temperature < 30.0) {
                                        showFanStatusNotification(context, "Kipas Mati. Suhu saat ini $temperature°C")
                                    }
                                    previousTemperature = temperature
                                }
                                "Water Volume" -> {
                                    Text(
                                        text = cards[i + 1].second,
                                        fontSize = 18.sp,
                                        color = Color.Gray,
                                        fontFamily = poppinsRegular,
                                        textAlign = TextAlign.Center
                                    )
                                    isWaterFull.value = cards[i + 1].second == "Penuh"
                                    if (previousWaterStatus != null && previousWaterStatus != cards[i + 1].second) {
                                        showWaterVolumeNotification(context, cards[i + 1].second)
                                    }
                                    previousWaterStatus = cards[i + 1].second
                                }
                                else -> {
                                    Text(
                                        text = cards[i + 1].second,
                                        fontSize = 18.sp,
                                        color = Color.Gray,
                                        fontFamily = poppinsRegular,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun vibratePhone(context: Context, shouldVibrate: Boolean) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (shouldVibrate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 500), 0)
        }
    } else {
        vibrator.cancel()
    }
}

fun showSmokeDetectedNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "smoke_detection_channel"
    val channelName = "Smoke Detection Alerts"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel for smoke detection alerts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.baseline_warning_24)
        .setContentTitle("Terdeteksi Asap!")
        .setContentText("Terdeteksi asap di rumah Anda!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    notificationManager.notify(1, notificationBuilder.build())
}

fun showFanStatusNotification(context: Context, status: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "fan_status_channel"
    val channelName = "Fan Status Alerts"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel for fan status alerts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.baseline_notifications_24)
        .setContentTitle("Status Kipas")
        .setContentText(status)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    notificationManager.notify(2, notificationBuilder.build())
}

fun showWaterVolumeNotification(context: Context, status: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "water_volume_channel"
    val channelName = "Water Volume Alerts"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel for water volume alerts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(if (status == "Penuh") R.drawable.baseline_warning_24 else R.drawable.baseline_water_drop_24)
        .setContentTitle(if (status == "Penuh") "Segera Matikan Air." else "Status Volume Air")
        .setContentText(status)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    notificationManager.notify(3, notificationBuilder.build())
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(it)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppContent() {
    AppContent()
}

fun fetchFirebaseData(path: String, onDataReceived: (String) -> Unit) {
    val database = FirebaseDatabase.getInstance("https://tubesiot-1c21b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val reference = database.getReference(path)

    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.value?.toString() ?: "No Data"
            onDataReceived(value)
        }

        override fun onCancelled(error: DatabaseError) {
            onDataReceived("Error: ${error.message}")
        }
    })
}
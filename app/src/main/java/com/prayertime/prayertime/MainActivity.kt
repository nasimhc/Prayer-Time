package com.prayertime.prayertime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertime.prayertime.ui.theme.*
import com.prayertime.prayertime.ui.viewmodel.PrayerTimeViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayerTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightDeep
                ) {
                    PrayerTimeScreen()
                }
            }
        }
    }
}

// Map English prayer names to Bengali
private fun getPrayerNameInBengali(prayerName: String?): String {
    return when (prayerName) {
        "fajr" -> "ফজর"
        "dhuhr" -> "জোহর"
        "asr" -> "আসর"
        "maghrib" -> "মাগরিব"
        "isha" -> "ইশা"
        else -> ""
    }
}

// Get accent color based on prayer
private fun getPrayerAccentColor(prayerName: String?): Color {
    return when (prayerName) {
        "fajr" -> FajrAccent
        "dhuhr" -> DhuhrColor
        "asr" -> AsrColor
        "maghrib" -> MaghribColor
        "isha" -> CelestialViolet
        else -> GoldBright
    }
}

@Composable
fun CelestialBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MidnightDeep,
                    GradientMid,
                    MidnightBase
                )
            )
        )

        // Geometric pattern overlay - Islamic star pattern
        val patternSize = 120.dp.toPx()
        val rows = (size.height / patternSize).toInt() + 1
        val cols = (size.width / patternSize).toInt() + 1

        for (row in 0..rows) {
            for (col in 0..cols) {
                val centerX = col * patternSize + patternSize / 2
                val centerY = row * patternSize + patternSize / 2
                val offset = if (row % 2 == 0) 0f else patternSize / 2

                // Draw subtle 8-pointed star
                drawEightPointedStar(
                    center = Offset(centerX + offset, centerY),
                    outerRadius = patternSize * 0.15f,
                    innerRadius = patternSize * 0.08f,
                    color = GoldSubtle.copy(alpha = 0.15f)
                )
            }
        }

        // Floating orbs with glow effect
        val orbPositions = listOf(
            Offset(size.width * 0.2f, size.height * 0.15f),
            Offset(size.width * 0.8f, size.height * 0.3f),
            Offset(size.width * 0.1f, size.height * 0.6f),
            Offset(size.width * 0.9f, size.height * 0.75f),
        )

        orbPositions.forEachIndexed { index, pos ->
            val orbAlpha = starAlpha * (0.3f + index * 0.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CelestialLight.copy(alpha = orbAlpha * 0.5f),
                        CelestialViolet.copy(alpha = orbAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = 80.dp.toPx()
                ),
                center = pos,
                radius = 80.dp.toPx()
            )
        }

        // Crescent moon decoration
        rotate(rotation * 0.01f, pivot = Offset(size.width * 0.85f, size.height * 0.08f)) {
            drawArc(
                color = GoldWarm.copy(alpha = 0.3f),
                startAngle = 45f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(size.width * 0.78f, size.height * 0.02f),
                size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 60.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEightPointedStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    val points = 8
    val angleStep = 360f / points / 2

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = Math.toRadians((i * angleStep - 90).toDouble())
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

@Composable
fun PrayerTimeScreen(
    modifier: Modifier = Modifier,
    viewModel: PrayerTimeViewModel = viewModel()
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val sunriseSunset by viewModel.sunriseSunset.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPrayer by viewModel.currentPrayer.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val countdown by viewModel.countdownToNextPrayer.collectAsState()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Celestial Background
        CelestialBackground()

        when {
            isLoading -> {
                LoadingAnimation(modifier = Modifier.align(Alignment.Center))
            }
            error != null -> {
                ErrorDisplay(
                    error = error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            prayerTimes != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    animationSpec = tween(600),
                                    initialOffsetY = { -40 }
                                )
                    ) {
                        AppHeader()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Countdown Card
                    if (nextPrayer != null && countdown != null) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(800, delayMillis = 150)) +
                                    slideInVertically(
                                        animationSpec = tween(800, delayMillis = 150),
                                        initialOffsetY = { 60 }
                                    )
                        ) {
                            HeroCountdownCard(
                                nextPrayerName = getPrayerNameInBengali(nextPrayer),
                                countdown = countdown ?: "",
                                accentColor = getPrayerAccentColor(nextPrayer)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prayer Time Cards
                    val prayers = listOf(
                        Triple("ফজর", prayerTimes?.fajr ?: "", "fajr"),
                        Triple("সূর্যোদয়", sunriseSunset?.first ?: "", "sunrise"),
                        Triple("জোহর", prayerTimes?.dhuhr ?: "", "dhuhr"),
                        Triple("আসর", prayerTimes?.asr ?: "", "asr"),
                        Triple("সূর্যাস্ত", sunriseSunset?.second ?: "", "sunset"),
                        Triple("মাগরিব", prayerTimes?.maghrib ?: "", "maghrib"),
                        Triple("ইশা", prayerTimes?.isha ?: "", "isha")
                    )

                    prayers.forEachIndexed { index, (name, time, key) ->
                        if (time.isNotEmpty()) {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(animationSpec = tween(600, delayMillis = 300 + index * 80)) +
                                        slideInVertically(
                                            animationSpec = tween(600, delayMillis = 300 + index * 80),
                                            initialOffsetY = { 40 }
                                        )
                            ) {
                                PrayerTimeCard(
                                    name = name,
                                    time = time,
                                    isCurrent = currentPrayer == key,
                                    isSpecial = key == "sunrise" || key == "sunset",
                                    accentColor = getPrayerAccentColor(key)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Decorative line
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldBright,
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "নামাজের সময়",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Prayer Times",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Decorative line
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldBright,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun HeroCountdownCard(
    nextPrayerName: String,
    countdown: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale)
    ) {
        // Outer glow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .blur(20.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = glowAlpha * 0.3f),
                            CelestialPurple.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        // Main card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            GoldSubtle,
                            accentColor.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceCardElevated.copy(alpha = 0.95f),
                            SurfaceCard.copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "পরবর্তী নামাজ",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prayer name
                Text(
                    text = nextPrayerName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Countdown timer
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "বাকি আছে",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun PrayerTimeCard(
    name: String,
    time: String,
    isCurrent: Boolean,
    isSpecial: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "current")

    val currentGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "currentGlow"
    )

    val borderColor = when {
        isCurrent -> GoldBright.copy(alpha = currentGlow)
        isSpecial -> SunriseColor.copy(alpha = 0.4f)
        else -> MidnightLight.copy(alpha = 0.5f)
    }

    val backgroundColor = when {
        isCurrent -> SurfaceCardElevated
        else -> SurfaceCard.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GoldBright.copy(alpha = currentGlow * 0.15f),
                                    CelestialViolet.copy(alpha = currentGlow * 0.1f),
                                    GoldBright.copy(alpha = currentGlow * 0.15f)
                                )
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                        )
                    }
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left accent indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isCurrent) {
                                    listOf(GoldBright, GoldMuted)
                                } else if (isSpecial) {
                                    listOf(SunriseColor, AsrColor)
                                } else {
                                    listOf(accentColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.2f))
                                }
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Icon for sunrise/sunset
                if (isSpecial) {
                    val iconRes = if (name == "সূর্যোদয়") R.drawable.ic_sunrise else R.drawable.ic_sunset
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        tint = SunriseColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Prayer name
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) GoldWarm else TextPrimary
                )

                // Current prayer indicator
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                GoldBright.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "চলমান",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldWarm
                        )
                    }
                }
            }

            // Time display
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                color = if (isCurrent) GoldBright else TextSecondary
            )
        }
    }
}

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            // Outer ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GoldBright,
                            CelestialViolet,
                            GoldBright.copy(alpha = 0.3f),
                            GoldBright
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Inner geometric pattern
            Canvas(modifier = Modifier.size(40.dp)) {
                drawEightPointedStar(
                    center = Offset(size.width / 2, size.height / 2),
                    outerRadius = size.minDimension / 2,
                    innerRadius = size.minDimension / 4,
                    color = GoldWarm.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "লোড হচ্ছে...",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )
    }
}

@Composable
fun ErrorDisplay(
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaghribColor.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaghribColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "সমস্যা হয়েছে",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error ?: "Unknown error",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

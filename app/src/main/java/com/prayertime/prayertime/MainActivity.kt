package com.prayertime.prayertime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.prayertime.prayertime.data.PreferencesManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertime.prayertime.ui.theme.*
import com.prayertime.prayertime.ui.viewmodel.PrayerTimeViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(preferencesManager.isDarkTheme) }

            PrayerTimeTheme(darkTheme = isDarkTheme) {
                PrayerTimeScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        preferencesManager.isDarkTheme = isDarkTheme
                    }
                )
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
@Composable
private fun getPrayerAccentColor(prayerName: String?): Color {
    val colors = LocalPrayerTimeColors.current
    return when (prayerName) {
        "fajr" -> FajrAccent
        "dhuhr" -> colors.goldBright
        "asr" -> AsrColor
        "maghrib" -> MaghribColor
        "isha" -> colors.accentViolet
        else -> colors.goldBright
    }
}

@Composable
fun PrayerTimeScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: PrayerTimeViewModel = viewModel()
) {
    val colors = LocalPrayerTimeColors.current
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with theme toggle
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    animationSpec = tween(600),
                                    initialOffsetY = { -40 }
                                )
                    ) {
                        AppHeader(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

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

                    // Sunrise & Sunset Row
                    if (sunriseSunset != null) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(600, delayMillis = 250)) +
                                    slideInVertically(
                                        animationSpec = tween(600, delayMillis = 250),
                                        initialOffsetY = { 40 }
                                    )
                        ) {
                            SunriseSunsetRow(
                                sunrise = sunriseSunset?.first ?: "",
                                sunset = sunriseSunset?.second ?: ""
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Prayer Time Cards (without sunrise/sunset)
                    val prayers = listOf(
                        Triple("ফজর", prayerTimes?.fajr ?: "", "fajr"),
                        Triple("জোহর", prayerTimes?.dhuhr ?: "", "dhuhr"),
                        Triple("আসর", prayerTimes?.asr ?: "", "asr"),
                        Triple("মাগরিব", prayerTimes?.maghrib ?: "", "maghrib"),
                        Triple("ইশা", prayerTimes?.isha ?: "", "isha")
                    )

                    prayers.forEachIndexed { index, (name, time, key) ->
                        if (time.isNotEmpty()) {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(animationSpec = tween(600, delayMillis = 350 + index * 70)) +
                                        slideInVertically(
                                            animationSpec = tween(600, delayMillis = 350 + index * 70),
                                            initialOffsetY = { 40 }
                                        )
                            ) {
                                PrayerTimeCard(
                                    name = name,
                                    time = time,
                                    isCurrent = currentPrayer == key,
                                    accentColor = getPrayerAccentColor(key)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val colors = LocalPrayerTimeColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "নামাজের সময়",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = "Prayer Times",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted,
                letterSpacing = 2.sp
            )
        }

        // Theme toggle button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.surfaceCard)
                .border(1.dp, colors.borderColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onThemeToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            ThemeIcon(
                isDarkTheme = isDarkTheme,
                tint = colors.goldBright,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HeroCountdownCard(
    nextPrayerName: String,
    countdown: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalPrayerTimeColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        colors.borderColor,
                        accentColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .background(colors.surfaceCardElevated, RoundedCornerShape(20.dp))
            .padding(24.dp)
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
                        .background(accentColor.copy(alpha = glowAlpha), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "পরবর্তী নামাজ",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textMuted
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

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown timer
            Text(
                text = countdown,
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "বাকি আছে",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted
            )
        }
    }
}

@Composable
fun SunriseSunsetRow(
    sunrise: String,
    sunset: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalPrayerTimeColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sunrise Card
        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = SunriseColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(colors.surfaceCard, RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sunrise),
                    contentDescription = "Sunrise",
                    tint = SunriseColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সূর্যোদয়",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sunrise,
                style = MaterialTheme.typography.titleMedium,
                color = SunriseColor
            )
        }

        // Sunset Card
        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = MaghribColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(colors.surfaceCard, RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sunset),
                    contentDescription = "Sunset",
                    tint = MaghribColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সূর্যাস্ত",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sunset,
                style = MaterialTheme.typography.titleMedium,
                color = MaghribColor
            )
        }
    }
}

@Composable
fun PrayerTimeCard(
    name: String,
    time: String,
    isCurrent: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalPrayerTimeColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "current")
    val currentGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "currentGlow"
    )

    val borderColor = if (isCurrent) {
        colors.goldBright.copy(alpha = currentGlow)
    } else {
        colors.borderColor.copy(alpha = 0.6f)
    }

    val backgroundColor = if (isCurrent) {
        colors.surfaceCardElevated
    } else {
        colors.surfaceCard
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isCurrent) {
                                listOf(colors.goldBright, colors.goldMuted)
                            } else {
                                listOf(accentColor.copy(alpha = 0.7f), accentColor.copy(alpha = 0.3f))
                            }
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Prayer name
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) colors.goldWarm else colors.textPrimary
            )

            // Current prayer badge
            if (isCurrent) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .background(
                            colors.goldBright.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "চলমান",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.goldWarm
                    )
                }
            }
        }

        // Time display
        Text(
            text = time,
            style = MaterialTheme.typography.titleLarge,
            color = if (isCurrent) colors.goldBright else colors.textSecondary
        )
    }
}

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val colors = LocalPrayerTimeColors.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = colors.goldBright,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "লোড হচ্ছে...",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMuted
        )
    }
}

@Composable
fun ErrorDisplay(
    error: String?,
    modifier: Modifier = Modifier
) {
    val colors = LocalPrayerTimeColors.current

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaghribColor.copy(alpha = 0.15f),
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
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error ?: "Unknown error",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ThemeIcon(
    isDarkTheme: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 3

        if (isDarkTheme) {
            // Sun icon (show when in dark mode to switch to light)
            // Center circle
            drawCircle(
                color = tint,
                radius = radius * 0.6f,
                center = Offset(centerX, centerY)
            )
            // Sun rays
            val rayLength = radius * 0.4f
            val rayStart = radius * 0.8f
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                val startX = centerX + (rayStart * kotlin.math.cos(angle)).toFloat()
                val startY = centerY + (rayStart * kotlin.math.sin(angle)).toFloat()
                val endX = centerX + ((rayStart + rayLength) * kotlin.math.cos(angle)).toFloat()
                val endY = centerY + ((rayStart + rayLength) * kotlin.math.sin(angle)).toFloat()
                drawLine(
                    color = tint,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Moon icon (show when in light mode to switch to dark)
            drawArc(
                color = tint,
                startAngle = 40f,
                sweepAngle = 280f,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            // Cut out for crescent effect
            drawCircle(
                color = if (isDarkTheme) MidnightDeep else IvoryDeep,
                radius = radius * 0.7f,
                center = Offset(centerX + radius * 0.4f, centerY - radius * 0.3f)
            )
        }
    }
}

package com.prayertime.prayertime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertime.prayertime.ui.theme.PrayerTimeTheme
import com.prayertime.prayertime.ui.viewmodel.PrayerTimeViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayerTimeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    "নামাজের সময়",
                                    style = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                ) 
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF4A90E2),
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    PrayerTimeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PrayerTimeScreen(
    modifier: Modifier = Modifier,
    viewModel: PrayerTimeViewModel = viewModel()
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPrayer by viewModel.currentPrayer.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            prayerTimes != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrayerTimeCard(
                        "ফজর", 
                        prayerTimes?.fajr ?: "", 
                        backgroundColor = if (currentPrayer == "fajr") Color(0xFF4A90E2) else Color(0xFFE3F2FD),
                        textColor = if (currentPrayer == "fajr") Color.White else Color(0xFF424242)
                    )
                    PrayerTimeCard(
                        "জোহর", 
                        prayerTimes?.dhuhr ?: "", 
                        backgroundColor = if (currentPrayer == "dhuhr") Color(0xFF9B59B6) else Color(0xFFF3E5F5),
                        textColor = if (currentPrayer == "dhuhr") Color.White else Color(0xFF424242)
                    )
                    PrayerTimeCard(
                        "আসর", 
                        prayerTimes?.asr ?: "", 
                        backgroundColor = if (currentPrayer == "asr") Color(0xFF2ECC71) else Color(0xFFE8F5E9),
                        textColor = if (currentPrayer == "asr") Color.White else Color(0xFF424242)
                    )
                    PrayerTimeCard(
                        "মাগরিব", 
                        prayerTimes?.maghrib ?: "", 
                        backgroundColor = if (currentPrayer == "maghrib") Color(0xFFE67E22) else Color(0xFFFFF3E0),
                        textColor = if (currentPrayer == "maghrib") Color.White else Color(0xFF424242)
                    )
                    PrayerTimeCard(
                        "ইশা", 
                        prayerTimes?.isha ?: "", 
                        backgroundColor = if (currentPrayer == "isha") Color(0xFF34495E) else Color(0xFFEFEBE9),
                        textColor = if (currentPrayer == "isha") Color.White else Color(0xFF424242)
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerTimeCard(
    name: String, 
    time: String, 
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                style = TextStyle(
                    fontSize = 24.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            )
        }
    }
}
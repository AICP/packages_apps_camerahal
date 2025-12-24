@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lineageos.camerahalcheck.ui

import android.content.pm.ApplicationInfo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lineageos.camerahalcheck.CameraStressTest
import com.lineageos.camerahalcheck.NativeCameraProbe

@Composable
fun StatusScreen(
    appInfo: ApplicationInfo,
    onCaptureLogcat: () -> Unit
) {
    val context = LocalContext.current

    var status by remember { mutableStateOf("Idle") }
    var busy by remember { mutableStateOf(false) }

    val isSuccess = status.contains("OK", true) || status.contains("captured", true)
    val isError = status.contains("ERROR", true) || status.contains("FAILED", true) || status.contains("CRASHED", true)

    val statusColor by animateColorAsState(
        targetValue = when {
            isSuccess -> Color(0xFF4CAF50)
            isError -> Color(0xFFE53935)
            busy -> Color(0xFFFFA726)
            else -> Color(0xFF90A4AE)
        },
        label = "statusColor"
    )

    val indicatorScale by animateFloatAsState(
        targetValue = if (busy) 1.2f else 1f,
        label = "indicatorScale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Camera HAL Checker",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
                .padding(pad)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Status indicator circle
                Box(
                    modifier = Modifier
                        .size((100 * indicatorScale).dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((60 * indicatorScale).dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status text
                Text(
                    text = status,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    textAlign = TextAlign.Center
                )

                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = statusColor,
                        strokeWidth = 2.dp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Diagnostics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        FilledTonalButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF3F51B5),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF3F51B5).copy(alpha = 0.4f)
                            ),
                            onClick = {
                                busy = true
                                status = "Probing HAL..."
                                try {
                                    val cams = NativeCameraProbe.probeCameraServiceNative()
                                    status =
                                        if (cams >= 0) "HAL OK ($cams cameras)"
                                        else "HAL ERROR"
                                } catch (_: Throwable) {
                                    status = "HAL CRASHED"
                                }
                                busy = false
                            }
                        ) {
                            Text("Check Camera HAL", fontWeight = FontWeight.Medium)
                        }

                        FilledTonalButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF7C4DFF),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF7C4DFF).copy(alpha = 0.4f)
                            ),
                            onClick = {
                                busy = true
                                status = "Stress testing cameras..."
                                CameraStressTest.run(
                                    context = context,
                                    onUpdate = { status = it },
                                    onDone = {
                                        status = if (it) "Stress test OK" else "Stress test FAILED"
                                        busy = false
                                    }
                                )
                            }
                        ) {
                            Text("Camera Stress Test", fontWeight = FontWeight.Medium)
                        }

                        FilledTonalButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF00897B),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF00897B).copy(alpha = 0.4f)
                            ),
                            onClick = {
                                onCaptureLogcat()
                                status = "Logcat captured (-b all)"
                            }
                        ) {
                            Text("Capture Logcat (-b all)", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

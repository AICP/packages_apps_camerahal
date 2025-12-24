@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lineageos.camerahalcheck.ui

import android.content.pm.ApplicationInfo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lineageos.camerahalcheck.CameraStressTest
import com.lineageos.camerahalcheck.NativeCameraProbe
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D0D1A),
                    Color(0xFF1A1A2E),
                    Color(0xFF16213E)
                )
            )
        )

        val rad1 = Math.toRadians(offset1.toDouble())
        val rad2 = Math.toRadians(offset2.toDouble())

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6C63FF).copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = Offset(
                    x = w * 0.3f + (cos(rad1) * w * 0.2f).toFloat(),
                    y = h * 0.2f + (sin(rad1) * h * 0.1f).toFloat()
                ),
                radius = w * 0.6f
            ),
            center = Offset(
                x = w * 0.3f + (cos(rad1) * w * 0.2f).toFloat(),
                y = h * 0.2f + (sin(rad1) * h * 0.1f).toFloat()
            ),
            radius = w * 0.6f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE040FB).copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = Offset(
                    x = w * 0.8f + (cos(rad2) * w * 0.15f).toFloat(),
                    y = h * 0.7f + (sin(rad2) * h * 0.1f).toFloat()
                ),
                radius = w * 0.5f
            ),
            center = Offset(
                x = w * 0.8f + (cos(rad2) * w * 0.15f).toFloat(),
                y = h * 0.7f + (sin(rad2) * h * 0.1f).toFloat()
            ),
            radius = w * 0.5f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00BCD4).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(
                    x = w * 0.5f + (sin(rad1) * w * 0.1f).toFloat(),
                    y = h * 0.5f + (cos(rad2) * h * 0.15f).toFloat()
                ),
                radius = w * 0.4f
            ),
            center = Offset(
                x = w * 0.5f + (sin(rad1) * w * 0.1f).toFloat(),
                y = h * 0.5f + (cos(rad2) * h * 0.15f).toFloat()
            ),
            radius = w * 0.4f
        )
    }
}

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
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBlur(20f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
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
                                .height(56.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF6C63FF),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.4f)
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
                                .height(56.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFE040FB),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE040FB).copy(alpha = 0.4f)
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
                                .height(56.dp),
                            enabled = !busy,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF00BCD4),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF00BCD4).copy(alpha = 0.4f)
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

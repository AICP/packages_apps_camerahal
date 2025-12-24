@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aicp.camerahalcheck.ui

import android.content.pm.ApplicationInfo
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicp.camerahalcheck.CameraStressTest
import com.aicp.camerahalcheck.NativeCameraProbe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var status by remember { mutableStateOf("Ready to diagnose") }
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
        animationSpec = tween(300),
        label = "statusColor"
    )

    val indicatorScale by animateFloatAsState(
        targetValue = if (busy) 1.15f else 1f,
        animationSpec = tween(300),
        label = "indicatorScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Camera HAL Checker",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size((100 * indicatorScale).dp)
                        .scale(if (busy) pulseScale else 1f)
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

                Text(
                    text = status,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                AnimatedVisibility(
                    visible = busy,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = statusColor,
                        strokeWidth = 3.dp
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

                        DiagnosticButton(
                            text = "Check Camera HAL",
                            icon = Icons.Rounded.Memory,
                            color = Color(0xFF6C63FF),
                            enabled = !busy,
                            onClick = {
                                performHaptic()
                                busy = true
                                status = "Probing HAL..."
                                scope.launch {
                                    delay(100)
                                    try {
                                        val cams = NativeCameraProbe.probeCameraServiceNative()
                                        status = if (cams >= 0) "HAL OK • $cams camera(s) detected" else "HAL ERROR"
                                        performHaptic()
                                    } catch (_: Throwable) {
                                        status = "HAL CRASHED"
                                        performHaptic()
                                    }
                                    busy = false
                                }
                            }
                        )

                        DiagnosticButton(
                            text = "Camera Stress Test",
                            icon = Icons.Rounded.Camera,
                            color = Color(0xFFE040FB),
                            enabled = !busy,
                            onClick = {
                                performHaptic()
                                busy = true
                                status = "Stress testing cameras..."
                                CameraStressTest.run(
                                    context = context,
                                    onUpdate = { status = it },
                                    onDone = { success ->
                                        status = if (success) "Stress test OK" else "Stress test FAILED"
                                        performHaptic()
                                        busy = false
                                    }
                                )
                            }
                        )

                        DiagnosticButton(
                            text = "Capture Logcat",
                            icon = Icons.Rounded.Description,
                            color = Color(0xFF00BCD4),
                            enabled = !busy,
                            onClick = {
                                performHaptic()
                                onCaptureLogcat()
                                status = "Logcat saved to Downloads"
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Logcat saved to Downloads/CameraHALChecker",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DiagnosticButton(
    text: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}

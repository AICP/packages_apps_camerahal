@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lineageos.camerahalcheck.ui

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Camera HAL Checker") })
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .systemBlur(18f)
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text(
                    text = status,
                    color = if (status.contains("OK", true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Button(
                    enabled = !busy,
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
                    Text("Check Camera HAL")
                }

                Button(
                    enabled = !busy,
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
                    Text("Camera stress test")
                }

                Button(
                    enabled = !busy,
                    onClick = {
                        onCaptureLogcat()
                        status = "Logcat captured (-b all)"
                    }
                ) {
                    Text("Capture logcat (-b all)")
                }
            }
        }
    }
}

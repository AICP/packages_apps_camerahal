package com.lineageos.camerahalcheck

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.lineageos.camerahalcheck.ui.StatusScreen

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled in CameraStressTest */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            MaterialTheme {
                Surface {
                    StatusScreen(
                        appInfo = applicationInfo,
                        onCaptureLogcat = {
                            RootLogcat.captureAll()
                        }
                    )
                }
            }
        }
    }
}

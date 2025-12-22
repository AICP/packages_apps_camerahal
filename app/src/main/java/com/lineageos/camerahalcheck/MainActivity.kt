package com.lineageos.camerahalcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.lineageos.camerahalcheck.ui.StatusScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

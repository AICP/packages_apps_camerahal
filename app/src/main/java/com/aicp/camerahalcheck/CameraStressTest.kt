package com.aicp.camerahalcheck

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.core.content.ContextCompat

object CameraStressTest {

    fun run(
        context: Context,
        rounds: Int = 5,
        onUpdate: (String) -> Unit,
        onDone: (Boolean) -> Unit
    ) {
        val mainHandler = Handler(Looper.getMainLooper())

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            mainHandler.post {
                onUpdate("Camera permission not granted")
                onDone(false)
            }
            return
        }

        val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameras = try {
            camMgr.cameraIdList.toList()
        } catch (e: Exception) {
            mainHandler.post {
                onUpdate("Failed to get camera list")
                onDone(false)
            }
            return
        }

        if (cameras.isEmpty()) {
            mainHandler.post {
                onUpdate("No cameras found")
                onDone(false)
            }
            return
        }

        val cameraThread = HandlerThread("CameraStressTest").apply { start() }
        val cameraHandler = Handler(cameraThread.looper)

        var ok = true
        var step = 0
        val totalSteps = rounds * cameras.size

        fun cleanup() {
            cameraThread.quitSafely()
        }

        fun next() {
            if (step >= totalSteps) {
                cleanup()
                mainHandler.post { onDone(ok) }
                return
            }

            val camId = cameras[step % cameras.size]
            mainHandler.post { onUpdate("Opening camera $camId (${step + 1}/$totalSteps)") }

            try {
                camMgr.openCamera(
                    camId,
                    object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            camera.close()
                            step++
                            cameraHandler.postDelayed({ next() }, 250)
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            camera.close()
                            ok = false
                            step++
                            cameraHandler.post { next() }
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            camera.close()
                            ok = false
                            mainHandler.post { onUpdate("Camera $camId error=$error") }
                            step++
                            cameraHandler.post { next() }
                        }
                    },
                    cameraHandler
                )
            } catch (e: SecurityException) {
                ok = false
                mainHandler.post { onUpdate("Camera permission denied") }
                cleanup()
                mainHandler.post { onDone(false) }
            } catch (t: Throwable) {
                ok = false
                mainHandler.post { onUpdate("Camera $camId: ${t.message ?: "crashed"}") }
                step++
                cameraHandler.post { next() }
            }
        }

        cameraHandler.post { next() }
    }
}

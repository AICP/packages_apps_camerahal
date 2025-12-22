package com.lineageos.camerahalcheck

import android.hardware.camera2.CameraManager
import android.content.Context
import android.os.Handler
import android.os.Looper

object CameraStressTest {

    fun run(
        context: Context,
        rounds: Int = 5,
        onUpdate: (String) -> Unit,
        onDone: (Boolean) -> Unit
    ) {
        val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameras = camMgr.cameraIdList.toList()

        if (cameras.isEmpty()) {
            onUpdate("No cameras found")
            onDone(false)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var ok = true
        var step = 0

        fun next() {
            if (step >= rounds * cameras.size) {
                onDone(ok)
                return
            }

            val camId = cameras[step % cameras.size]
            onUpdate("Opening camera $camId (${step + 1}/${rounds * cameras.size})")

            try {
                camMgr.openCamera(
                    camId,
                    object : android.hardware.camera2.CameraDevice.StateCallback() {
                        override fun onOpened(camera: android.hardware.camera2.CameraDevice) {
                            camera.close()
                            handler.postDelayed({ step++; next() }, 250)
                        }

                        override fun onDisconnected(camera: android.hardware.camera2.CameraDevice) {
                            camera.close()
                            ok = false
                            handler.post { step++; next() }
                        }

                        override fun onError(camera: android.hardware.camera2.CameraDevice, error: Int) {
                            camera.close()
                            ok = false
                            onUpdate("Camera $camId error=$error")
                            handler.post { step++; next() }
                        }
                    },
                    handler
                )
            } catch (t: Throwable) {
                ok = false
                onUpdate("Camera $camId crashed")
                handler.post { step++; next() }
            }
        }

        next()
    }
}

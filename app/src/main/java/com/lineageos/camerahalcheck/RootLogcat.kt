package com.lineageos.camerahalcheck

import java.io.File

object RootLogcat {

    fun captureAll(): File? {
        return try {
            val out = File(
                "/sdcard/Download/CameraHALChecker",
                "logcat_${System.currentTimeMillis()}.txt"
            )
            out.parentFile?.mkdirs()

            Runtime.getRuntime().exec(
                arrayOf(
                    "su",
                    "-c",
                    "logcat -b all -d > ${out.absolutePath}"
                )
            ).waitFor()

            out
        } catch (t: Throwable) {
            null
        }
    }
}

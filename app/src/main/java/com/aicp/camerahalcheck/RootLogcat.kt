package com.aicp.camerahalcheck

import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RootLogcat {

    fun captureAll(): File? {
        val baseDir = File("/storage/emulated/0/Download/CameraHALChecker")
        if (!baseDir.exists()) baseDir.mkdirs()

        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(baseDir, "logcat_all_$ts.txt")

        val cmd = if (hasRoot()) {
            arrayOf(
                "su", "-c",
                "logcat -b all -d -v threadtime > ${outFile.absolutePath}"
            )
        } else {
            arrayOf(
                "sh", "-c",
                "logcat -b all -d -v threadtime > ${outFile.absolutePath}"
            )
        }

        return try {
            Runtime.getRuntime().exec(cmd).waitFor()
            outFile
        } catch (t: Throwable) {
            null
        }
    }

    private fun hasRoot(): Boolean {
        return try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id")).waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }
}

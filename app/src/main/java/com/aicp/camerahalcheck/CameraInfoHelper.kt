package com.aicp.camerahalcheck

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

data class CameraInfo(
    val id: String,
    val facing: String,
    val megapixels: Float,
    val hasFlash: Boolean
)

object CameraInfoHelper {

    fun getCameraList(context: Context): List<CameraInfo> {
        val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            camMgr.cameraIdList.map { id ->
                val chars = camMgr.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }
                val configs = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val sizes = configs?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                val maxSize = sizes?.maxByOrNull { it.width * it.height }
                val mp = if (maxSize != null) {
                    (maxSize.width * maxSize.height) / 1_000_000f
                } else 0f
                val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                CameraInfo(id, facing, mp, flash)
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}

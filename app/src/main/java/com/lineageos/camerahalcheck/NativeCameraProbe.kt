package com.lineageos.camerahalcheck

object NativeCameraProbe {
    init {
        System.loadLibrary("tomaslib")
    }

    external fun probeCameraServiceNative(): Int
}

package com.lineageos.camerahalcheck.ui

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import android.graphics.RenderEffect
import android.graphics.Shader

fun Modifier.systemBlur(radius: Float): Modifier {
    return if (Build.VERSION.SDK_INT >= 31) {
        this.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        this
    }
}

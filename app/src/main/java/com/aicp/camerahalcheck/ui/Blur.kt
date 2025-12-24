package com.aicp.camerahalcheck.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.systemBlur(
    radius: Float,
    fallbackRadius: Dp = 16.dp
): Modifier {
    return if (Build.VERSION.SDK_INT >= 31) {
        this.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(radius, radius, Shader.TileMode.DECAL)
                .asComposeRenderEffect()
        }
    } else {
        this.blur(fallbackRadius)
    }
}

fun Modifier.glassEffect(
    blurRadius: Float = 25f,
    tintColor: Color = Color.White.copy(alpha = 0.1f)
): Modifier {
    return this
        .systemBlur(blurRadius)
        .background(tintColor)
}

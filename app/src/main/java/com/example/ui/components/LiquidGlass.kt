package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceElevated

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0xDD151224),
    borderColor: Color = Color(0x33FFFFFF),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    isPowerSaver: Boolean = false
): Modifier {
    return if (isPowerSaver) {
        this
            .clip(shape)
            .background(DarkSurfaceElevated)
            .border(borderWidth, borderColor.copy(alpha = 0.2f), shape)
    } else {
        this
            .shadow(elevation, shape, ambientColor = Color(0x66000000), spotColor = Color(0x88000000))
            .clip(shape)
            .background(backgroundColor)
            .border(
                borderWidth,
                Brush.verticalGradient(
                    listOf(
                        borderColor.copy(alpha = 0.4f),
                        borderColor.copy(alpha = 0.08f)
                    )
                ),
                shape
            )
    }
}

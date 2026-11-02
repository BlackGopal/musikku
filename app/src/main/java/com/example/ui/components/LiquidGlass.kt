package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceElevated

/**
 * Ultra-Clear Liquid Glass (Kaca Cair Transparan Organik):
 * - Tingkat keburaman minimal (5–10%), objek di belakang kaca tetap terlihat sangat jelas.
 * - Pembiasan optik dan pantulan glossy seperti tetesan air / cairan kental.
 * - Pantulan kilau putih kebiruan (#E0F2FE), refleksi internal, dan rim dispersi kromatik (biru, ungu, lavender).
 * - Tidak berkabut, tidak frosted, dan tidak tebal.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    backgroundColor: Color = Color(0x1C120E26), // Hanya ~11% opacity, sangat transparan
    borderColor: Color = Color(0x60FFFFFF),
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 10.dp,
    isPowerSaver: Boolean = false
): Modifier {
    return if (isPowerSaver) {
        this
            .clip(shape)
            .background(DarkSurfaceElevated)
            .border(borderWidth, borderColor.copy(alpha = 0.25f), shape)
    } else {
        this
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x55000000),
                spotColor = Color(0x77000000)
            )
            .clip(shape)
            // Latar kristal cair: hampir bening dengan gradasi bias halus
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x28FFFFFF), // kilau transparan tipis di atas
                        backgroundColor.copy(alpha = 0.12f),
                        backgroundColor.copy(alpha = 0.18f),
                        Color(0x181A1435)  // bias bayangan halus di dasar
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 600f)
                )
            )
            // Rim kromatik prismatik: pembiasan tepi biru, ungu, dan lavender
            .border(
                borderWidth,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xE6FFFFFF), // Kilauan putih kebiruan specular di sudut atas
                        Color(0x9993C5FD), // Pembiasan biru langit / cyan
                        Color(0x66C084FC), // Pembiasan ungu elektrik
                        Color(0x40DDD6FE), // Refleksi lavender lembut
                        Color(0x2038BDF8), // Transparansi tepi samping
                        Color(0x15FFFFFF)  // Tepi bawah lembut
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(600f, 800f)
                ),
                shape
            )
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Refleksi internal: Kilau bias oval di sudut kiri atas (subsurface optical lens)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4DFFFFFF), // Putih specular
                            Color(0x2693C5FD), // Pendar biru lembut
                            Color(0x12C084FC), // Lavender pudar
                            Color.Transparent
                        ),
                        center = Offset(w * 0.25f, h * 0.18f),
                        radius = (w * 0.5f).coerceAtLeast(60f)
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(w * 0.65f, h * 0.5f)
                )

                // 2. Garis pelengkungan cairan organik (liquid meniscus specular curve)
                val causticPath = Path().apply {
                    moveTo(16f, 2f)
                    // Lengkungan organik gelombang cairan
                    cubicTo(
                        w * 0.3f, 3f,
                        w * 0.7f, 1.2f,
                        w - 20f, 2.5f
                    )
                }
                drawPath(
                    path = causticPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x55E0F2FE), // Kilau putih kebiruan tajam
                            Color(0xEEFFFFFF), // Titik kilau terik cairan
                            Color(0x8893C5FD), // Pembiasan biru muda
                            Color.Transparent
                        )
                    ),
                    style = Stroke(width = 1.8f)
                )

                // 3. Pembiasan tepi bawah: distorsi bias lavender sekunder
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x28A855F7), // Ungu lembut
                            Color(0x38C084FC), // Lavender
                            Color(0x2060A5FA), // Biru
                            Color.Transparent
                        )
                    ),
                    start = Offset(w * 0.15f, h - 2f),
                    end = Offset(w * 0.85f, h - 2f),
                    strokeWidth = 1.4f
                )
            }
    }
}

/**
 * iOS-style smooth bouncy tap interaction dengan deformasi elastis cairan
 */
@Composable
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.93f,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncy_scale"
    )

    return this
        .scale(scale)
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val upOrCancel = waitForUpOrCancellation()
                    isPressed = false
                    if (upOrCancel != null) {
                        onClick()
                    }
                }
            }
        }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun WaveformScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 42
) {
    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else 0f

    // Pre-calculate waveform normalized heights
    val waveHeights = remember(barCount) {
        (0 until barCount).map { i ->
            val phase = (i.toFloat() / barCount) * 4 * Math.PI
            val base = (sin(phase).toFloat() * 0.35f + 0.65f)
            val flutter = if (i % 3 == 0) 0.85f else if (i % 2 == 0) 0.65f else 1.0f
            (base * flutter).coerceIn(0.2f, 1f)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("waveform_scrubber")
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val totalBars = waveHeights.size
                val barSpacing = 3.dp.toPx()
                val totalSpacing = barSpacing * (totalBars - 1)
                val barWidth = ((canvasWidth - totalSpacing) / totalBars).coerceAtLeast(2f)

                val activeCutoffX = canvasWidth * progress

                for (i in 0 until totalBars) {
                    val x = i * (barWidth + barSpacing)
                    val heightFactor = waveHeights[i]
                    val barHeight = (canvasHeight * 0.85f * heightFactor).coerceAtLeast(4f)
                    val y = (canvasHeight - barHeight) / 2f

                    val isPlayed = (x + barWidth / 2f) <= activeCutoffX
                    val color = if (isPlayed) NeonLime else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.22f)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }
        }

        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = formatDuration(durationMs),
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSecs = (millis / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "%d:%02d".format(mins, secs)
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.NeonLime
import kotlin.math.abs

@Composable
fun SongArtwork(
    coverUri: String?,
    title: String,
    modifier: Modifier = Modifier,
    artist: String = "",
    size: Dp = 56.dp,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp)
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (size >= 120.dp) 16.dp else 4.dp,
                shape = shape,
                ambientColor = Color(0x66000000),
                spotColor = Color(0x88000000)
            )
            .clip(shape)
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color(0x55FFFFFF), Color(0x15FFFFFF), Color(0x05FFFFFF))
                ),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUri.isNullOrBlank()) {
            AsyncImage(
                model = coverUri,
                contentDescription = "Cover lagu $title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Distinctive generated artwork per song
            val palette = remember(title) {
                val code = abs(title.hashCode()) % 5
                when (code) {
                    0 -> listOf(Color(0xFF2E0854), Color(0xFF6B11B0), Color(0xFFC724B1), Color(0xFFFF85E0)) // Violet dream
                    1 -> listOf(Color(0xFF0D1B2A), Color(0xFF1B4965), Color(0xFF62B6CB), Color(0xFFD4FF28)) // Deep ocean & lime
                    2 -> listOf(Color(0xFF3A015C), Color(0xFF4F0147), Color(0xFF941B0C), Color(0xFFF95738)) // Sunset flame
                    3 -> listOf(Color(0xFF03254C), Color(0xFF1167B1), Color(0xFF187BCD), Color(0xFF2A9D8F)) // Electric azure
                    else -> listOf(Color(0xFF1F1235), Color(0xFF33275E), Color(0xFF8F7193), Color(0xFFB599FE)) // Midnight lavender
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(palette)),
                contentAlignment = Alignment.Center
            ) {
                // Subtle vinyl record circular groove effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.toPx() / 2f
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)

                    // Multiple concentric sound waves / grooves
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = radius * 0.82f,
                        center = center,
                        style = Stroke(width = 1.2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius * 0.65f,
                        center = center,
                        style = Stroke(width = 1.0f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius * 0.48f,
                        center = center,
                        style = Stroke(width = 1.0f)
                    )

                    // Glass sheen diagonal gloss
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.toPx() * 0.7f, size.toPx() * 0.7f),
                        strokeWidth = size.toPx() * 0.2f
                    )
                }

                if (size >= 120.dp) {
                    // Large Now Playing Artwork
                    Box(
                        modifier = Modifier
                            .size(size * 0.45f)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(size * 0.24f)
                        )
                    }

                    // Bottom title tag on large artwork
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = title.take(20),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // List thumbnail size
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(size * 0.48f)
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RadiantPink
import com.example.ui.theme.SoftLavender

@Composable
fun SongArtwork(
    coverUri: String?,
    title: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape),
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
            // Elegant gradient with initials
            val hash = title.hashCode()
            val gradient = when (Math.abs(hash) % 4) {
                0 -> listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                1 -> listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                2 -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                else -> listOf(Color(0xFF134E5E), Color(0xFF71B280))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                val initial = title.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "M"
                if (size >= 80.dp) {
                    Text(
                        text = initial,
                        fontSize = (size.value * 0.4f).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(size * 0.5f)
                    )
                }
            }
        }
    }
}

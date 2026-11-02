package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LyricsView(
    song: Song,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit,
    onSaveLyrics: (String) -> Unit,
    onClose: () -> Unit,
    isPowerSaver: Boolean,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val lyricLines = remember(song.customLyrics) {
        MusicRepository.parseLrcLyrics(song.customLyrics)
    }

    val activeIndex = remember(lyricLines, currentPositionMs) {
        if (lyricLines.isEmpty()) -1
        else {
            val idx = lyricLines.indexOfLast { it.timestampMs <= currentPositionMs }
            if (idx == -1 && lyricLines.isNotEmpty() && lyricLines[0].timestampMs == 0L) 0 else idx
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyricLines.size) {
            val target = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_lyrics_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Lirik Lagu",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("edit_lyrics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sunting Lirik",
                        tint = NeonLime
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (lyricLines.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum ada lirik untuk lagu ini",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                            modifier = Modifier.testTag("add_lyrics_button")
                        ) {
                            Text("Tambah / Tempel Lirik", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(lyricLines) { index, line ->
                        val isActive = index == activeIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isActive) NeonLime else Color.White.copy(alpha = 0.35f),
                            label = "lyric_color"
                        )
                        val fontSize = if (isActive) 22.sp else 16.sp
                        val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium

                        Text(
                            text = line.text,
                            color = textColor,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (line.timestampMs > 0) onSeek(line.timestampMs)
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Edit lyrics dialog
        if (showEditDialog) {
            var lyricsInput by remember { mutableStateOf(song.customLyrics ?: "") }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Sunting / Tempel Lirik") },
                text = {
                    Column {
                        Text(
                            text = "Tempel teks lirik biasa atau format lirik tersinkron (.lrc dengan format [mm:ss.xx]).",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = lyricsInput,
                            onValueChange = { lyricsInput = it },
                            placeholder = { Text("Tempel teks lirik di sini...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("lyrics_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSaveLyrics(lyricsInput)
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                        modifier = Modifier.testTag("save_lyrics_button")
                    ) {
                        Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}

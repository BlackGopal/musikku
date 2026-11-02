package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Album
import com.example.data.model.Song
import com.example.ui.components.SongArtwork
import com.example.ui.components.liquidGlass
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.HeartRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class HomeFilter(val label: String) {
    ALL("Semua"),
    RECENT("Terbaru"),
    FAVORITES("Disukai"),
    MOST_PLAYED("Sering Diputar")
}

@Composable
fun HomeScreen(
    userName: String,
    songs: List<Song>,
    albums: List<Album>,
    currentSong: Song?,
    isPlaying: Boolean,
    isPowerSaver: Boolean,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSongOptionClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }

    val filteredSongs = remember(selectedFilter, songs) {
        when (selectedFilter) {
            HomeFilter.ALL -> songs
            HomeFilter.RECENT -> songs.sortedByDescending { it.dateAdded }
            HomeFilter.FAVORITES -> songs.filter { it.isFavorite }
            HomeFilter.MOST_PLAYED -> songs.sortedByDescending { it.playCount }
        }
    }

    val featuredAlbum = albums.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle with glow
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(ElectricViolet, Color(0xFF6B3BA7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "S",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Halo, $userName",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "${songs.size} lagu di perangkat",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row {
                    // Search button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF))
                            .clickable { onSearchClick() }
                            .testTag("home_search_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Heart shortcut button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF))
                            .clickable { onFavoritesClick() }
                            .testTag("home_favorites_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Lagu Disukai",
                            tint = HeartRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Horizontal filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeFilter.values().forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonLime else Color(0x26FFFFFF))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("filter_chip_${filter.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) Color(0xFF0D1402) else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Featured Album Section (Curated card from reference design)
        if (featuredAlbum != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
                    Text(
                        text = "Album di Perangkat",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Big featured card styled exactly like the Discover Weekly reference card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFC7B1FF), Color(0xFFA58BFA))
                                )
                            )
                            .clickable { onAlbumSelected(featuredAlbum) }
                            .padding(20.dp)
                            .testTag("featured_album_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = featuredAlbum.name,
                                    color = Color(0xFF1E1136),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${featuredAlbum.artist} • ${featuredAlbum.songCount} lagu",
                                    color = Color(0xFF3B2563),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Circular play button
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF281347))
                                            .clickable {
                                                if (featuredAlbum.songs.isNotEmpty()) {
                                                    onSongSelected(featuredAlbum.songs[0], featuredAlbum.songs)
                                                }
                                            }
                                            .testTag("play_featured_album"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Putar Album",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = Color(0xFF281347),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            SongArtwork(
                                coverUri = featuredAlbum.coverUri,
                                title = featuredAlbum.name,
                                size = 96.dp,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Song Collection Section ("Top daily playlists" / "Koleksi Lagu")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Koleksi Lagu",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredSongs.size} lagu",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        if (filteredSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada lagu dalam kategori ini.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            itemsIndexed(filteredSongs) { index, song ->
                val isCurrentPlaying = currentSong?.id == song.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongSelected(song, filteredSongs) }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("song_item_${song.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongArtwork(
                        coverUri = song.customCoverUri,
                        title = song.title,
                        size = 52.dp,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = if (isCurrentPlaying) NeonLime else TextPrimary,
                            fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artist} • ${song.durationFormatted}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Favorite toggle
                    IconButton(
                        onClick = { onToggleFavorite(song) },
                        modifier = Modifier.size(36.dp).testTag("fav_icon_${song.id}")
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Sukai",
                            tint = if (song.isFavorite) HeartRed else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // More options button
                    IconButton(
                        onClick = { onSongOptionClick(song) },
                        modifier = Modifier.size(36.dp).testTag("more_icon_${song.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Lagu",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

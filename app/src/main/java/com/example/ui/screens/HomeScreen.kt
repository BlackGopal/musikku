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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.SongSortOption
import com.example.ui.components.SongArtwork
import com.example.ui.components.bouncyClickable
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
    var currentSortOption by remember { mutableStateOf(SongSortOption.RECENTLY_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredSongs = remember(selectedFilter, currentSortOption, songs) {
        val base = when (selectedFilter) {
            HomeFilter.ALL -> songs
            HomeFilter.RECENT -> songs.sortedByDescending { it.dateAdded }
            HomeFilter.FAVORITES -> songs.filter { it.isFavorite }
            HomeFilter.MOST_PLAYED -> songs.sortedByDescending { it.playCount }
        }

        when (currentSortOption) {
            SongSortOption.RECENTLY_ADDED -> base.sortedByDescending { it.dateAdded }
            SongSortOption.TITLE_ASC -> base.sortedBy { it.title.lowercase() }
            SongSortOption.TITLE_DESC -> base.sortedByDescending { it.title.lowercase() }
            SongSortOption.ARTIST -> base.sortedBy { it.artist.lowercase() }
            SongSortOption.DURATION_DESC -> base.sortedByDescending { it.durationMs }
            SongSortOption.DURATION_ASC -> base.sortedBy { it.durationMs }
        }
    }

    val featuredAlbum = albums.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 150.dp)
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
                            .size(48.dp)
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .bouncyClickable { onSearchClick() }
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

                    // Heart shortcut button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .bouncyClickable { onFavoritesClick() }
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
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeFilter.values().forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonLime else Color(0x22FFFFFF))
                            .bouncyClickable { selectedFilter = filter }
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

        // Featured Album Section (Curated card)
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

                    // Big featured card with smooth bouncy interaction
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFC7B1FF), Color(0xFFA58BFA))
                                )
                            )
                            .bouncyClickable(pressedScale = 0.96f) { onAlbumSelected(featuredAlbum) }
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
                                    // Circular play button with bounce
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF281347))
                                            .bouncyClickable {
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
                                            .size(38.dp)
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
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Song Collection Section with Sort Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Koleksi Lagu",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Sort Dropdown Button
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x22FFFFFF))
                            .bouncyClickable { showSortMenu = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("sort_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Urutkan",
                                tint = NeonLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentSortOption.label,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        SongSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (option == currentSortOption) NeonLime else TextPrimary,
                                        fontWeight = if (option == currentSortOption) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentSortOption = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
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
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isCurrentPlaying) Color(0x26D4FF28) else Color.Transparent)
                        .bouncyClickable { onSongSelected(song, filteredSongs) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("song_item_${song.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongArtwork(
                        coverUri = song.customCoverUri,
                        title = song.title,
                        artist = song.artist,
                        size = 52.dp,
                        shape = RoundedCornerShape(16.dp)
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artist} • ${song.durationFormatted}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bouncy Favorite toggle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .bouncyClickable { onToggleFavorite(song) }
                            .testTag("fav_icon_${song.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Sukai",
                            tint = if (song.isFavorite) HeartRed else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bouncy More options button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .bouncyClickable { onSongOptionClick(song) }
                            .testTag("more_icon_${song.id}"),
                        contentAlignment = Alignment.Center
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

package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Album
import com.example.data.model.Song
import com.example.ui.MainViewModel
import com.example.ui.SheetOverlay
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.LyricsView
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NavTab
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.QueueView
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MusikkuTheme
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusikkuTheme {
                MusikkuApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MusikkuApp(viewModel: MainViewModel) {
    val context = LocalContext.current

    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val activeOverlay by viewModel.activeOverlay.collectAsState()
    val targetSongForAction by viewModel.targetSongForAction.collectAsState()
    val isPowerSaver by viewModel.isPowerSaver.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showSongOptionsDialog by remember { mutableStateOf<Song?>(null) }
    var showCoverChangeDialog by remember { mutableStateOf<Song?>(null) }

    // Modern Zero-Permission Photo Picker for Custom Cover Art
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val songToUpdate = showCoverChangeDialog ?: targetSongForAction ?: playerState.currentSong
            if (songToUpdate != null) {
                // Take persistable URI permission if needed or convert to string
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if cannot take persistable
                }
                viewModel.updateSongCover(songToUpdate.id, it.toString())
                Toast.makeText(context, "Cover lagu berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
        showCoverChangeDialog = null
    }

    // Audio storage permissions request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Screens
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                when (currentTab) {
                    NavTab.HOME -> {
                        HomeScreen(
                            userName = userName,
                            songs = songs,
                            albums = albums,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            isPowerSaver = isPowerSaver,
                            onSongSelected = { song, list -> viewModel.playSong(song, list) },
                            onAlbumSelected = { album ->
                                viewModel.playSong(album.songs.firstOrNull() ?: return@HomeScreen, album.songs)
                            },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onSearchClick = { viewModel.selectTab(NavTab.LIBRARY) },
                            onFavoritesClick = { viewModel.selectTab(NavTab.PLAYLISTS) },
                            onSongOptionClick = { song -> showSongOptionsDialog = song }
                        )
                    }
                    NavTab.LIBRARY -> {
                        LibraryScreen(
                            songs = songs,
                            albums = albums,
                            currentSong = playerState.currentSong,
                            onSongSelected = { song, list -> viewModel.playSong(song, list) },
                            onAlbumSelected = { album ->
                                viewModel.playSong(album.songs.firstOrNull() ?: return@LibraryScreen, album.songs)
                            },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onSongOptionClick = { song -> showSongOptionsDialog = song }
                        )
                    }
                    NavTab.PLAYLISTS -> {
                        PlaylistsScreen(
                            playlists = playlists,
                            favoriteSongs = favoriteSongs,
                            currentSong = playerState.currentSong,
                            onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                            onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                            onPlayPlaylist = { pl, shuffle ->
                                if (pl.songs.isNotEmpty()) {
                                    val list = if (shuffle) pl.songs.shuffled() else pl.songs
                                    viewModel.playSong(list[0], list)
                                }
                            },
                            onPlayFavorites = { shuffle ->
                                if (favoriteSongs.isNotEmpty()) {
                                    val list = if (shuffle) favoriteSongs.shuffled() else favoriteSongs
                                    viewModel.playSong(list[0], list)
                                }
                            },
                            onRemoveSongFromPlaylist = { plId, songId ->
                                viewModel.removeSongFromPlaylist(plId, songId)
                            },
                            onSongSelected = { song, list -> viewModel.playSong(song, list) }
                        )
                    }
                    NavTab.EQUALIZER -> {
                        EqualizerScreen(
                            isEqEnabled = playerState.isEqEnabled,
                            eqBands = playerState.eqBandLevels,
                            activePresetName = playerState.activePresetName,
                            customPresets = customPresets,
                            hasHardwareEq = playerState.hasHardwareEq,
                            isPowerSaver = isPowerSaver,
                            onToggleEq = { enabled -> viewModel.setEqualizerEnabled(enabled) },
                            onBandChanged = { band, db -> viewModel.setBandLevel(band, db) },
                            onApplyPreset = { preset -> viewModel.applyPreset(preset) },
                            onSaveCustomPreset = { name, bands -> viewModel.saveCustomPreset(name, bands) },
                            onDeleteCustomPreset = { name -> viewModel.deleteCustomPreset(name) }
                        )
                    }
                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            userName = userName,
                            isPowerSaver = isPowerSaver,
                            onUpdateUserName = { name -> viewModel.updateUserName(name) },
                            onTogglePowerSaver = { enabled -> viewModel.togglePowerSaver(enabled) },
                            onRescanLibrary = {
                                viewModel.rescanLibrary()
                                Toast.makeText(context, "Memindai ulang file musik...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Floating MiniPlayer (appears directly above the floating bottom nav bar)
            if (!isNowPlayingExpanded && playerState.currentSong != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 82.dp)
                ) {
                    MiniPlayer(
                        currentSong = playerState.currentSong,
                        isPlaying = playerState.isPlaying,
                        progressFraction = playerState.progressFraction,
                        isPowerSaver = isPowerSaver,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onClick = { viewModel.openNowPlaying() }
                    )
                }
            }

            // Floating Bottom Nav Bar
            if (!isNowPlayingExpanded) {
                FloatingBottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    isPowerSaver = isPowerSaver,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Fullscreen Now Playing Modal Sheet
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingSheet(
                    playerState = playerState,
                    isFavorite = playerState.currentSong?.isFavorite == true,
                    onToggleFavorite = {
                        playerState.currentSong?.let { viewModel.toggleFavorite(it) }
                    },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onSeek = { ms -> viewModel.seekTo(ms) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeat() },
                    onOpenEqualizer = {
                        viewModel.closeNowPlaying()
                        viewModel.selectTab(NavTab.EQUALIZER)
                    },
                    onOpenLyrics = {
                        viewModel.setOverlay(SheetOverlay.LYRICS)
                    },
                    onOpenQueue = {
                        viewModel.setOverlay(SheetOverlay.QUEUE)
                    },
                    onOpenCoverPicker = {
                        showCoverChangeDialog = playerState.currentSong
                    },
                    onOpenAddToPlaylist = {
                        viewModel.setOverlay(SheetOverlay.ADD_TO_PLAYLIST)
                    },
                    onClose = { viewModel.closeNowPlaying() },
                    isPowerSaver = isPowerSaver
                )
            }

            // Lyrics Overlay
            if (activeOverlay == SheetOverlay.LYRICS && playerState.currentSong != null) {
                LyricsView(
                    song = playerState.currentSong!!,
                    currentPositionMs = playerState.currentPositionMs,
                    onSeek = { ms -> viewModel.seekTo(ms) },
                    onSaveLyrics = { text ->
                        playerState.currentSong?.let { viewModel.saveLyrics(it.id, text) }
                    },
                    onClose = { viewModel.closeOverlay() },
                    isPowerSaver = isPowerSaver
                )
            }

            // Queue Overlay
            if (activeOverlay == SheetOverlay.QUEUE) {
                QueueView(
                    queue = playerState.queue,
                    currentIndex = playerState.queueIndex,
                    onSongSelected = { index ->
                        viewModel.playerManager.playSongAtIndex(index)
                    },
                    onClose = { viewModel.closeOverlay() }
                )
            }

            // Add To Playlist Dialog
            if (activeOverlay == SheetOverlay.ADD_TO_PLAYLIST && targetSongForAction != null) {
                AddToPlaylistDialog(
                    song = targetSongForAction!!,
                    playlists = playlists,
                    onAddToPlaylist = { pl ->
                        viewModel.addSongToPlaylist(pl.id, targetSongForAction!!.id)
                        Toast.makeText(context, "Ditambahkan ke ${pl.name}", Toast.LENGTH_SHORT).show()
                        viewModel.closeOverlay()
                    },
                    onCreateNewPlaylist = { name ->
                        viewModel.createPlaylist(name)
                        Toast.makeText(context, "Playlist $name dibuat", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { viewModel.closeOverlay() }
                )
            }

            // Song Options Dialog (from 3-dots icon)
            showSongOptionsDialog?.let { song ->
                AlertDialog(
                    onDismissRequest = { showSongOptionsDialog = null },
                    title = { Text(song.title, color = TextPrimary) },
                    text = {
                        Column {
                            Text(
                                text = "${song.artist} • ${song.album}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                            Button(
                                onClick = {
                                    showSongOptionsDialog = null
                                    viewModel.setOverlay(SheetOverlay.ADD_TO_PLAYLIST, song)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("Tambah ke Playlist", color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    showSongOptionsDialog = null
                                    showCoverChangeDialog = song
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("Ganti Cover Lagu", color = TextPrimary)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showSongOptionsDialog = null }) {
                            Text("Batal", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurfaceElevated
                )
            }

            // Cover Change Dialog
            showCoverChangeDialog?.let { song ->
                AlertDialog(
                    onDismissRequest = { showCoverChangeDialog = null },
                    title = { Text("Ganti Cover: ${song.title}") },
                    text = {
                        Column {
                            Text(
                                text = "Pilih gambar dari galeri perangkat Anda untuk cover lagu ini.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("Pilih Gambar dari Galeri", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            if (song.customCoverUri != null) {
                                Button(
                                    onClick = {
                                        viewModel.updateSongCover(song.id, null)
                                        Toast.makeText(context, "Cover dikembalikan ke semula", Toast.LENGTH_SHORT).show()
                                        showCoverChangeDialog = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Text("Kembalikan ke Cover Asli", color = TextPrimary)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showCoverChangeDialog = null }) {
                            Text("Batal", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurfaceElevated
                )
            }
        }
    }
}

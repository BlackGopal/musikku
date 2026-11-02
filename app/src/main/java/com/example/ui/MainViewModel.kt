package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomPresetEntity
import com.example.data.model.Album
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.EqPreset
import com.example.player.PlayerManager
import com.example.player.PlayerState
import com.example.ui.components.NavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SheetOverlay {
    NONE,
    LYRICS,
    QUEUE,
    ADD_TO_PLAYLIST,
    CHANGE_COVER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MusicRepository(application)
    val playerManager = PlayerManager.getInstance(application)

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    val songs: StateFlow<List<Song>> = repository.allSongsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.albumsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = songs.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customPresets: StateFlow<List<CustomPresetEntity>> = repository.customPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _activeOverlay = MutableStateFlow(SheetOverlay.NONE)
    val activeOverlay: StateFlow<SheetOverlay> = _activeOverlay.asStateFlow()

    private val _targetSongForAction = MutableStateFlow<Song?>(null)
    val targetSongForAction: StateFlow<Song?> = _targetSongForAction.asStateFlow()

    private val _isPowerSaver = MutableStateFlow(repository.isPowerSaverEnabled)
    val isPowerSaver: StateFlow<Boolean> = _isPowerSaver.asStateFlow()

    private val _userName = MutableStateFlow(repository.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        // Record song plays in database
        playerManager.onSongCompletedListener = { song ->
            viewModelScope.launch {
                repository.recordSongPlayed(song.id)
            }
        }
        viewModelScope.launch {
            repository.reloadSongs()
        }
    }

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun openNowPlaying() {
        _isNowPlayingExpanded.value = true
    }

    fun closeNowPlaying() {
        _isNowPlayingExpanded.value = false
        _activeOverlay.value = SheetOverlay.NONE
    }

    fun setOverlay(overlay: SheetOverlay, targetSong: Song? = null) {
        _targetSongForAction.value = targetSong ?: playerState.value.currentSong
        _activeOverlay.value = overlay
    }

    fun closeOverlay() {
        _activeOverlay.value = SheetOverlay.NONE
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerManager.playQueue(queue, index)
    }

    fun togglePlayPause() = playerManager.togglePlayPause()

    fun playNext() = playerManager.playNext()

    fun playPrevious() = playerManager.playPrevious()

    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)

    fun toggleShuffle() = playerManager.toggleShuffle()

    fun cycleRepeat() = playerManager.cycleRepeatMode()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) = playerManager.setEqualizerEnabled(enabled)

    fun setBandLevel(band: Int, db: Int) = playerManager.setBandLevel(band, db)

    fun applyPreset(preset: EqPreset) = playerManager.applyPreset(preset)

    fun saveCustomPreset(name: String, bands: List<Int>) {
        viewModelScope.launch {
            repository.saveCustomPreset(name, bands)
        }
    }

    fun deleteCustomPreset(name: String) {
        viewModelScope.launch {
            repository.deleteCustomPreset(name)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun renamePlaylist(id: Long, name: String) {
        viewModelScope.launch {
            repository.updatePlaylistName(id, name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun updateSongCover(songId: Long, coverUri: String?) {
        viewModelScope.launch {
            repository.updateSongCover(songId, coverUri)
        }
    }

    fun saveLyrics(songId: Long, lyrics: String?) {
        viewModelScope.launch {
            repository.updateSongLyrics(songId, lyrics)
        }
    }

    fun updateUserName(name: String) {
        repository.userName = name
        _userName.value = name
    }

    fun togglePowerSaver(enabled: Boolean) {
        repository.isPowerSaverEnabled = enabled
        _isPowerSaver.value = enabled
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            repository.reloadSongs()
        }
    }
}

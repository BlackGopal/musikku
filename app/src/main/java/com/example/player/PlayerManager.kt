package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class PlayerManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var originalQueue: List<Song> = emptyList()

    var onSongCompletedListener: ((Song) -> Unit)? = null

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("PlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                    _playerState.update { it.copy(isPlaying = false) }
                    true
                }
            }
            initEqualizer()
        } catch (e: Exception) {
            Log.e("PlayerManager", "Failed to initialize MediaPlayer", e)
        }
    }

    private fun initEqualizer() {
        val player = mediaPlayer ?: return
        try {
            equalizer?.release()
            equalizer = Equalizer(0, player.audioSessionId).apply {
                enabled = _playerState.value.isEqEnabled
            }
            applyBandsToHardware()
            _playerState.update { it.copy(hasHardwareEq = true) }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Hardware Equalizer not supported on this session or device: ${e.message}")
            _playerState.update { it.copy(hasHardwareEq = false) }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        originalQueue = songs
        val activeQueue = if (_playerState.value.isShuffle) {
            val selected = songs.getOrNull(startIndex) ?: songs[0]
            listOf(selected) + (songs - selected).shuffled()
        } else {
            songs
        }
        val targetIndex = if (_playerState.value.isShuffle) 0 else startIndex.coerceIn(0, songs.size - 1)

        _playerState.update {
            it.copy(
                queue = activeQueue,
                queueIndex = targetIndex
            )
        }
        playSongAtIndex(targetIndex)
    }

    fun playSongAtIndex(index: Int) {
        val state = _playerState.value
        val song = state.queue.getOrNull(index) ?: return

        _playerState.update {
            it.copy(
                currentSong = song,
                queueIndex = index,
                currentPositionMs = 0L,
                durationMs = song.durationMs
            )
        }

        try {
            val player = mediaPlayer ?: MediaPlayer().also { mediaPlayer = it }
            player.reset()

            if (song.contentUri.startsWith("/")) {
                player.setDataSource(song.contentUri)
            } else {
                val uri = Uri.parse(song.contentUri)
                player.setDataSource(appContext, uri)
            }

            player.prepare()
            player.start()

            val actualDuration = player.duration.toLong().takeIf { it > 0 } ?: song.durationMs

            _playerState.update {
                it.copy(
                    isPlaying = true,
                    durationMs = actualDuration
                )
            }

            initEqualizer()
            startProgressUpdates()
            onSongCompletedListener?.invoke(song)

            // Trigger notification
            MusicPlaybackService.startServiceWithNotification(appContext, song, true)
        } catch (e: Exception) {
            Log.e("PlayerManager", "Error playing song: ${song.title}", e)
            _playerState.update { it.copy(isPlaying = false) }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val currentSong = _playerState.value.currentSong

        if (currentSong == null) {
            // If there's an existing queue, play first
            if (_playerState.value.queue.isNotEmpty()) {
                playSongAtIndex(0)
            }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _playerState.update { it.copy(isPlaying = false) }
            stopProgressUpdates()
            MusicPlaybackService.startServiceWithNotification(appContext, currentSong, false)
        } else {
            try {
                player.start()
                _playerState.update { it.copy(isPlaying = true) }
                startProgressUpdates()
                MusicPlaybackService.startServiceWithNotification(appContext, currentSong, true)
            } catch (e: Exception) {
                // In case playback was reset
                playSongAtIndex(_playerState.value.queueIndex.coerceAtLeast(0))
            }
        }
    }

    fun playNext() {
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        val nextIndex = when {
            state.queueIndex + 1 < state.queue.size -> state.queueIndex + 1
            state.repeatMode == RepeatMode.ALL -> 0
            else -> return // at end of queue
        }

        playSongAtIndex(nextIndex)
    }

    fun playPrevious() {
        val player = mediaPlayer
        // If played more than 3 seconds, restart current track
        if (player != null && player.currentPosition > 3000) {
            player.seekTo(0)
            _playerState.update { it.copy(currentPositionMs = 0L) }
            return
        }

        val state = _playerState.value
        if (state.queue.isEmpty()) return

        val prevIndex = when {
            state.queueIndex - 1 >= 0 -> state.queueIndex - 1
            state.repeatMode == RepeatMode.ALL -> state.queue.size - 1
            else -> 0
        }

        playSongAtIndex(prevIndex)
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        try {
            val clamped = positionMs.coerceIn(0L, _playerState.value.durationMs)
            player.seekTo(clamped.toInt())
            _playerState.update { it.copy(currentPositionMs = clamped) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleShuffle() {
        _playerState.update { state ->
            val newShuffle = !state.isShuffle
            val currentSong = state.currentSong
            val newQueue = if (newShuffle) {
                if (currentSong != null) {
                    listOf(currentSong) + (originalQueue - currentSong).shuffled()
                } else {
                    originalQueue.shuffled()
                }
            } else {
                originalQueue
            }
            val newIndex = if (currentSong != null) {
                newQueue.indexOfFirst { it.id == currentSong.id }.coerceAtLeast(0)
            } else 0

            state.copy(
                isShuffle = newShuffle,
                queue = newQueue,
                queueIndex = newIndex
            )
        }
    }

    fun cycleRepeatMode() {
        _playerState.update { state ->
            val nextMode = when (state.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            state.copy(repeatMode = nextMode)
        }
    }

    private fun handleTrackCompletion() {
        val state = _playerState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _playerState.update { it.copy(isPlaying = true) }
                startProgressUpdates()
            }
            RepeatMode.ALL -> {
                val nextIdx = (state.queueIndex + 1) % state.queue.size
                playSongAtIndex(nextIdx)
            }
            RepeatMode.OFF -> {
                if (state.queueIndex + 1 < state.queue.size) {
                    playSongAtIndex(state.queueIndex + 1)
                } else {
                    _playerState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                    stopProgressUpdates()
                    state.currentSong?.let {
                        MusicPlaybackService.startServiceWithNotification(appContext, it, false)
                    }
                }
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    _playerState.update { it.copy(currentPositionMs = pos) }
                }
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    // --- Equalizer Controls ---

    fun setEqualizerEnabled(enabled: Boolean) {
        _playerState.update { it.copy(isEqEnabled = enabled) }
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("PlayerManager", "Error toggling hardware EQ: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val clamped = levelDb.coerceIn(-15, 15)
        val currentLevels = _playerState.value.eqBandLevels.toMutableList()
        if (bandIndex in currentLevels.indices) {
            currentLevels[bandIndex] = clamped
            _playerState.update {
                it.copy(
                    eqBandLevels = currentLevels,
                    activePresetName = "Kustom"
                )
            }
            applyBandsToHardware()
        }
    }

    fun applyPreset(preset: EqPreset) {
        _playerState.update {
            it.copy(
                eqBandLevels = preset.bands,
                activePresetName = preset.name
            )
        }
        applyBandsToHardware()
    }

    private fun applyBandsToHardware() {
        val eq = equalizer ?: return
        val levels = _playerState.value.eqBandLevels
        try {
            val minRange = eq.bandLevelRange.getOrNull(0) ?: -1500
            val maxRange = eq.bandLevelRange.getOrNull(1) ?: 1500
            val numBands = eq.numberOfBands.toInt()

            for (i in 0 until numBands) {
                val levelDb = levels.getOrElse(i) { 0 }
                // 1 dB = 100 millibels
                val mB = (levelDb * 100).toShort().coerceIn(minRange, maxRange)
                eq.setBandLevel(i.toShort(), mB)
            }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Failed to apply EQ bands to hardware: ${e.message}")
        }
    }

    fun release() {
        stopProgressUpdates()
        try {
            equalizer?.release()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        equalizer = null
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlayerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

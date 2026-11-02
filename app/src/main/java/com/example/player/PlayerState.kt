package com.example.player

import com.example.data.model.Song

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val isEqEnabled: Boolean = true,
    val eqBandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // -15 to +15 dB
    val activePresetName: String = "Flat",
    val hasHardwareEq: Boolean = true
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

data class EqPreset(
    val name: String,
    val isCustom: Boolean,
    val bands: List<Int> // 5 bands in dB (-15 to +15)
)

object DefaultEqPresets {
    val BUILT_IN = listOf(
        EqPreset("Flat", false, listOf(0, 0, 0, 0, 0)),
        EqPreset("Bass Boost", false, listOf(9, 6, 2, 0, -2)),
        EqPreset("Vokal", false, listOf(-2, 3, 7, 5, 1)),
        EqPreset("Treble", false, listOf(-3, 0, 2, 7, 10)),
        EqPreset("Rock", false, listOf(6, 3, -1, 4, 7)),
        EqPreset("Pop", false, listOf(-1, 2, 5, 3, -2))
    )
}

package com.example.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val mimeType: String = "audio/mpeg",
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val customCoverUri: String? = null,
    val customLyrics: String? = null,
    val isMissing: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTime: Long = 0L,
    val isSample: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val coverUri: String? = null,
    val songs: List<Song> = emptyList()
)

data class Playlist(
    val id: Long,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val songs: List<Song> = emptyList()
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

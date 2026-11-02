package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "song_meta")
data class SongMetaEntity(
    @PrimaryKey val songId: Long,
    val isFavorite: Boolean = false,
    val customCoverUri: String? = null,
    val customLyrics: String? = null,
    val playCount: Int = 0,
    val lastPlayedTime: Long = 0L
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    indices = [Index(value = ["playlistId"]), Index(value = ["songId"])]
)
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val songId: Long,
    val sortOrder: Int = 0
)

@Entity(tableName = "album_covers")
data class AlbumCoverEntity(
    @PrimaryKey val albumName: String,
    val customCoverUri: String
)

@Entity(tableName = "eq_presets")
data class CustomPresetEntity(
    @PrimaryKey val name: String,
    val band0: Int,
    val band1: Int,
    val band2: Int,
    val band3: Int,
    val band4: Int
)

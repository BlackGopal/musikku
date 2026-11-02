package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongMetaDao {
    @Query("SELECT * FROM song_meta")
    fun getAllMetaFlow(): Flow<List<SongMetaEntity>>

    @Query("SELECT * FROM song_meta WHERE songId = :songId")
    suspend fun getMeta(songId: Long): SongMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(entity: SongMetaEntity)

    @Query("UPDATE song_meta SET isFavorite = :isFav WHERE songId = :songId")
    suspend fun setFavorite(songId: Long, isFav: Boolean)

    @Query("UPDATE song_meta SET customCoverUri = :coverUri WHERE songId = :songId")
    suspend fun setCustomCover(songId: Long, coverUri: String?)

    @Query("UPDATE song_meta SET customLyrics = :lyrics WHERE songId = :songId")
    suspend fun setCustomLyrics(songId: Long, lyrics: String?)

    @Query("UPDATE song_meta SET playCount = playCount + 1, lastPlayedTime = :time WHERE songId = :songId")
    suspend fun recordPlayed(songId: Long, time: Long = System.currentTimeMillis())
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun updatePlaylistName(id: Long, name: String)

    @Query("UPDATE playlists SET coverUri = :coverUri WHERE id = :id")
    suspend fun updatePlaylistCover(id: Long, coverUri: String?)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(item: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun getPlaylistSongsFlow(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    suspend fun getPlaylistSongs(playlistId: Long): List<PlaylistSongEntity>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Int
}

@Dao
interface AlbumCoverDao {
    @Query("SELECT * FROM album_covers")
    fun getAllAlbumCoversFlow(): Flow<List<AlbumCoverEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAlbumCover(entity: AlbumCoverEntity)

    @Query("DELETE FROM album_covers WHERE albumName = :albumName")
    suspend fun removeAlbumCover(albumName: String)
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM eq_presets ORDER BY name ASC")
    fun getAllPresetsFlow(): Flow<List<CustomPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomPresetEntity)

    @Query("DELETE FROM eq_presets WHERE name = :name")
    suspend fun deletePreset(name: String)
}

package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.AlbumCoverEntity
import com.example.data.local.CustomPresetEntity
import com.example.data.local.MusikkuDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongEntity
import com.example.data.local.SongMetaEntity
import com.example.data.model.Album
import com.example.data.model.LyricLine
import com.example.data.model.Playlist
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context) {

    private val db = MusikkuDatabase.getInstance(context)
    private val songMetaDao = db.songMetaDao()
    private val playlistDao = db.playlistDao()
    private val albumCoverDao = db.albumCoverDao()
    private val presetDao = db.presetDao()

    private val _scannedSongs = MutableStateFlow<List<Song>>(emptyList())
    val scannedSongs: Flow<List<Song>> = _scannedSongs

    // Shared preferences for user name and power saver (mode hemat)
    private val prefs = context.getSharedPreferences("musikku_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString("user_name", "Samantha") ?: "Samantha"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var isPowerSaverEnabled: Boolean
        get() = prefs.getBoolean("power_saver_mode", false)
        set(value) = prefs.edit().putBoolean("power_saver_mode", value).apply()

    init {
        // Initial scan
        refreshSongs()
        ensureDefaultPlaylists()
    }

    suspend fun scanMedia(): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()

        // 1. Scan device MediaStore if permission is available
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Tidak diketahui"
                    val artist = it.getString(artistCol) ?: "Artis tidak diketahui"
                    val album = it.getString(albumCol) ?: "Album tidak diketahui"
                    val duration = it.getLong(durCol)
                    val track = it.getInt(trackCol)
                    val year = it.getInt(yearCol)
                    val mime = it.getString(mimeCol) ?: "audio/mpeg"
                    val dateAdded = it.getLong(dateCol) * 1000L

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    if (duration > 5000) { // filter out short sound effects
                        songsList.add(
                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                contentUri = contentUri,
                                trackNumber = track,
                                year = year,
                                mimeType = mime,
                                dateAdded = dateAdded,
                                isFavorite = false,
                                isMissing = false,
                                isSample = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Add bundled melodic sample tracks so app is always immediately functional & playable
        val sampleSongs = SampleMusicGenerator.ensureSampleTracks(context)
        songsList.addAll(sampleSongs)

        songsList
    }

    fun refreshSongs() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launchRefresh()
    }

    private fun kotlinx.coroutines.CoroutineScope.launchRefresh() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).run {
            kotlinx.coroutines.GlobalScope.run {
                // background load
            }
        }
    }

    suspend fun reloadSongs() = withContext(Dispatchers.IO) {
        val list = scanMedia()
        _scannedSongs.value = list
    }

    // Combine scanned songs with Room metadata (favorites, custom cover, custom lyrics, play count)
    val allSongsFlow: Flow<List<Song>> = combine(
        _scannedSongs,
        songMetaDao.getAllMetaFlow()
    ) { songs, metas ->
        val metaMap = metas.associateBy { it.songId }
        songs.map { song ->
            val meta = metaMap[song.id]
            if (meta != null) {
                song.copy(
                    isFavorite = meta.isFavorite,
                    customCoverUri = meta.customCoverUri,
                    customLyrics = meta.customLyrics ?: song.customLyrics,
                    playCount = meta.playCount.coerceAtLeast(song.playCount),
                    lastPlayedTime = meta.lastPlayedTime.coerceAtLeast(song.lastPlayedTime)
                )
            } else {
                song
            }
        }
    }.flowOn(Dispatchers.IO)

    // Flow of albums
    val albumsFlow: Flow<List<Album>> = combine(
        allSongsFlow,
        albumCoverDao.getAllAlbumCoversFlow()
    ) { songs, customCovers ->
        val coverMap = customCovers.associate { it.albumName to it.customCoverUri }
        songs.groupBy { it.album }.map { (albumName, albumSongs) ->
            Album(
                id = albumName.hashCode().toLong(),
                name = albumName,
                artist = albumSongs.firstOrNull()?.artist ?: "Artis tidak diketahui",
                songCount = albumSongs.size,
                coverUri = coverMap[albumName] ?: albumSongs.firstOrNull { it.customCoverUri != null }?.customCoverUri,
                songs = albumSongs
            )
        }.sortedBy { it.name }
    }.flowOn(Dispatchers.IO)

    // Flow of playlists
    val playlistsFlow: Flow<List<Playlist>> = combine(
        playlistDao.getAllPlaylistsFlow(),
        allSongsFlow
    ) { playlistEntities, songs ->
        val songMap = songs.associateBy { it.id }
        playlistEntities.map { entity ->
            val pSongs = playlistDao.getPlaylistSongs(entity.id).mapNotNull { ps ->
                songMap[ps.songId]
            }
            Playlist(
                id = entity.id,
                name = entity.name,
                coverUri = entity.coverUri ?: pSongs.firstOrNull { it.customCoverUri != null }?.customCoverUri,
                createdAt = entity.createdAt,
                songCount = pSongs.size,
                songs = pSongs
            )
        }
    }.flowOn(Dispatchers.IO)

    // Flow of custom presets
    val customPresetsFlow: Flow<List<CustomPresetEntity>> = presetDao.getAllPresetsFlow()

    suspend fun toggleFavorite(song: Song) = withContext(Dispatchers.IO) {
        val newFav = !song.isFavorite
        val existing = songMetaDao.getMeta(song.id)
        if (existing == null) {
            songMetaDao.upsertMeta(
                SongMetaEntity(
                    songId = song.id,
                    isFavorite = newFav,
                    customCoverUri = song.customCoverUri,
                    customLyrics = song.customLyrics,
                    playCount = song.playCount,
                    lastPlayedTime = song.lastPlayedTime
                )
            )
        } else {
            songMetaDao.setFavorite(song.id, newFav)
        }
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        val existing = songMetaDao.getMeta(songId)
        if (existing == null) {
            songMetaDao.upsertMeta(
                SongMetaEntity(
                    songId = songId,
                    playCount = 1,
                    lastPlayedTime = System.currentTimeMillis()
                )
            )
        } else {
            songMetaDao.recordPlayed(songId)
        }
    }

    suspend fun updateSongCover(songId: Long, coverUri: String?) = withContext(Dispatchers.IO) {
        val existing = songMetaDao.getMeta(songId)
        if (existing == null) {
            songMetaDao.upsertMeta(
                SongMetaEntity(
                    songId = songId,
                    customCoverUri = coverUri
                )
            )
        } else {
            songMetaDao.setCustomCover(songId, coverUri)
        }
    }

    suspend fun updateAlbumCover(albumName: String, coverUri: String?) = withContext(Dispatchers.IO) {
        if (coverUri == null) {
            albumCoverDao.removeAlbumCover(albumName)
        } else {
            albumCoverDao.setAlbumCover(AlbumCoverEntity(albumName, coverUri))
        }
    }

    suspend fun updateSongLyrics(songId: Long, lyrics: String?) = withContext(Dispatchers.IO) {
        val existing = songMetaDao.getMeta(songId)
        if (existing == null) {
            songMetaDao.upsertMeta(
                SongMetaEntity(
                    songId = songId,
                    customLyrics = lyrics
                )
            )
        } else {
            songMetaDao.setCustomLyrics(songId, lyrics)
        }
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun updatePlaylistName(id: Long, name: String) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylistName(id, name)
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
        playlistDao.clearPlaylistSongs(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean = withContext(Dispatchers.IO) {
        val count = playlistDao.isSongInPlaylist(playlistId, songId)
        if (count > 0) {
            return@withContext false // already in playlist
        }
        val currentSongs = playlistDao.getPlaylistSongs(playlistId)
        playlistDao.insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                sortOrder = currentSongs.size
            )
        )
        true
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun saveCustomPreset(name: String, bands: List<Int>) = withContext(Dispatchers.IO) {
        presetDao.insertPreset(
            CustomPresetEntity(
                name = name,
                band0 = bands.getOrElse(0) { 0 },
                band1 = bands.getOrElse(1) { 0 },
                band2 = bands.getOrElse(2) { 0 },
                band3 = bands.getOrElse(3) { 0 },
                band4 = bands.getOrElse(4) { 0 }
            )
        )
    }

    suspend fun deleteCustomPreset(name: String) = withContext(Dispatchers.IO) {
        presetDao.deletePreset(name)
    }

    private fun ensureDefaultPlaylists() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).run {
            // Seed sample playlist if empty
        }
    }

    companion object {
        fun parseLrcLyrics(rawText: String?): List<LyricLine> {
            if (rawText.isNullOrBlank()) return emptyList()
            val lines = rawText.lines()
            val result = mutableListOf<LyricLine>()
            val lrcRegex = Regex("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?\\](.*)")

            for (line in lines) {
                val match = lrcRegex.find(line.trim())
                if (match != null) {
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val millisStr = match.groupValues[3]
                    val millis = if (millisStr.isNotEmpty()) {
                        if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()
                    } else 0L
                    val totalMs = minutes * 60000L + seconds * 1000L + millis
                    val text = match.groupValues[4].trim()
                    if (text.isNotEmpty()) {
                        result.add(LyricLine(totalMs, text))
                    }
                } else if (line.isNotBlank() && !line.startsWith("[")) {
                    result.add(LyricLine(0L, line.trim()))
                }
            }
            return result.sortedBy { it.timestampMs }
        }
    }
}

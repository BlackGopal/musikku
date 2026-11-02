package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongMetaEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        AlbumCoverEntity::class,
        CustomPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusikkuDatabase : RoomDatabase() {
    abstract fun songMetaDao(): SongMetaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun albumCoverDao(): AlbumCoverDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: MusikkuDatabase? = null

        fun getInstance(context: Context): MusikkuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusikkuDatabase::class.java,
                    "musikku_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

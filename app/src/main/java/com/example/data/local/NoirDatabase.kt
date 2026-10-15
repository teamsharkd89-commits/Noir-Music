package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.Playlist
import com.example.data.local.entity.PlaylistSong
import com.example.data.local.entity.Song

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSong::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NoirDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: NoirDatabase? = null

        fun getInstance(context: Context): NoirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoirDatabase::class.java,
                    "noir_music.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

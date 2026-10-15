package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NoirDatabase
import com.example.data.local.entity.Playlist
import com.example.data.local.entity.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: NoirDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoirDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context matches NOIR Music`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NOIR Music", appName)
    }

    @Test
    fun `insert song and retrieve successfully`() = runBlocking {
        val song = Song(
            uriString = "content://media/external/audio/media/1",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 244000L,
            mimeType = "audio/flac",
            bitrate = 1411
        )
        val id = db.songDao().insertSong(song)
        assertNotNull(id)

        val allSongs = db.songDao().getAllSongs().first()
        assertEquals(1, allSongs.size)
        assertEquals("Midnight City", allSongs[0].title)
        assertEquals("M83", allSongs[0].artist)
    }

    @Test
    fun `insert playlist and retrieve successfully`() = runBlocking {
        val playlist = Playlist(
            name = "Late Night Noir",
            description = "Ambient and electronic tracks"
        )
        val playlistId = db.playlistDao().insertPlaylist(playlist)
        assertNotNull(playlistId)

        val allPlaylists = db.playlistDao().getAllPlaylists().first()
        assertEquals(1, allPlaylists.size)
        assertEquals("Late Night Noir", allPlaylists[0].name)
    }

    @Test
    fun `toggle song favorite and retrieve in favorite songs`() = runBlocking {
        val song = Song(
            uriString = "content://media/external/audio/media/2",
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            durationMs = 230000L,
            isFavorite = false
        )
        val id = db.songDao().insertSong(song)
        db.songDao().updateFavorite(id, true)

        val favorites = db.songDao().getFavoriteSongs().first()
        assertEquals(1, favorites.size)
        assertEquals("Starboy", favorites[0].title)
    }
}

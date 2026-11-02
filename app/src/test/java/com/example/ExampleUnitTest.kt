package com.example

import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.DefaultEqPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSongDurationFormatted() {
        val song = Song(
            id = 1L,
            title = "Starlit Reverie",
            artist = "Budiarti",
            album = "Midnight Confessions",
            durationMs = 215000L, // 3 mins 35 secs
            contentUri = "file:///sample.wav"
        )
        assertEquals("3:35", song.durationFormatted)
    }

    @Test
    fun testLrcParser() {
        val lrc = """
            [00:04.50]Baris pertama
            [00:10.00]Baris kedua
        """.trimIndent()

        val parsed = MusicRepository.parseLrcLyrics(lrc)
        assertEquals(2, parsed.size)
        assertEquals(4500L, parsed[0].timestampMs)
        assertEquals("Baris pertama", parsed[0].text)
        assertEquals(10000L, parsed[1].timestampMs)
        assertEquals("Baris kedua", parsed[1].text)
    }

    @Test
    fun testDefaultEqPresets() {
        val presets = DefaultEqPresets.BUILT_IN
        assertTrue(presets.isNotEmpty())
        val flat = presets.first { it.name == "Flat" }
        assertEquals(5, flat.bands.size)
        assertTrue(flat.bands.all { it == 0 })
    }
}

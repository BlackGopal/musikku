package com.example.data.repository

import android.content.Context
import com.example.data.model.Song
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates local demo audio tracks in internal storage so the user can immediately
 * listen, adjust the 5-band equalizer, inspect lyrics, and build playlists
 * even on fresh emulators or devices without pre-loaded audio files.
 */
object SampleMusicGenerator {

    data class SampleTrackSpec(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val durationSeconds: Int,
        val baseFreq: Double,
        val scale: List<Double>,
        val lrcLyrics: String
    )

    private val SAMPLE_SPECS = listOf(
        SampleTrackSpec(
            id = -101L,
            title = "Starlit Reverie",
            artist = "Budiarti",
            album = "Midnight Confessions",
            durationSeconds = 48,
            baseFreq = 220.0, // A3
            scale = listOf(220.0, 261.63, 329.63, 392.0, 440.0, 523.25),
            lrcLyrics = """
                [00:00.00]Starlit Reverie - Budiarti
                [00:04.00]Gemintang malam menghiasi langit sunyi
                [00:09.50]Alunan nada membelai lembut sanubari
                [00:15.00]Di balik hembusan angin yang tenang
                [00:20.50]Kisah rindu terukir terang benderang
                [00:26.00]Setiap melodi mengalir bagai cahaya
                [00:32.00]Membawa damai ke relung jiwa kita
                [00:38.00]Starlit reverie... dalam keheningan malam
                [00:44.00]Nada abadi takkan pernah padam
            """.trimIndent()
        ),
        SampleTrackSpec(
            id = -102L,
            title = "Midnight Confessions",
            artist = "Samantha",
            album = "Midnight Confessions",
            durationSeconds = 42,
            baseFreq = 261.63, // C4
            scale = listOf(261.63, 293.66, 329.63, 392.0, 440.0, 523.25),
            lrcLyrics = """
                [00:00.00]Midnight Confessions - Samantha
                [00:05.00]Whispers in the quiet midnight breeze
                [00:10.00]Finding serenity beneath the trees
                [00:16.00]Neon pulses glowing through the dark
                [00:21.00]Every heartbeat leaving an electric spark
                [00:27.00]In this sanctuary where rhythms play
                [00:33.00]All the world's worries drift away
                [00:39.00]Soft liquid echoes until the break of dawn
            """.trimIndent()
        ),
        SampleTrackSpec(
            id = -103L,
            title = "Lost in the Echo",
            artist = "Nova Sound",
            album = "Electric Horizons",
            durationSeconds = 36,
            baseFreq = 196.0, // G3
            scale = listOf(196.0, 220.0, 246.94, 293.66, 329.63, 392.0),
            lrcLyrics = """
                [00:00.00]Lost in the Echo - Nova Sound
                [00:04.00]Gelombang suara merambat di angkasa
                [00:09.00]Getaran bass menggetarkan sukma
                [00:14.50]Terhanyut dalam pantulan resonansi
                [00:20.00]Menari di antara dimensi harmoni
                [00:26.00]Gema yang indah tak bertepi
                [00:31.00]Musikku mengalun abadi di hati
            """.trimIndent()
        ),
        SampleTrackSpec(
            id = -104L,
            title = "Symphony of Rain",
            artist = "Aruna",
            album = "Acoustic Seasons",
            durationSeconds = 40,
            baseFreq = 174.61, // F3
            scale = listOf(174.61, 196.0, 220.0, 261.63, 329.63, 349.23),
            lrcLyrics = """
                [00:00.00]Symphony of Rain - Aruna
                [00:04.50]Rintik hujan mengetuk jendela kaca
                [00:10.00]Menciptakan ritme alam nan bersahaja
                [00:15.50]Aroma tanah basah memberi kesejukan
                [00:21.00]Lagu rindu mengalir tanpa keraguan
                [00:27.00]Dengarkan setiap tetes yang bermakna
                [00:33.00]Simfoni kedamaian semesta
            """.trimIndent()
        ),
        SampleTrackSpec(
            id = -105L,
            title = "Cosmic Horizons",
            artist = "Orion Beats",
            album = "Electric Horizons",
            durationSeconds = 45,
            baseFreq = 146.83, // D3
            scale = listOf(146.83, 174.61, 220.0, 261.63, 293.66, 349.23),
            lrcLyrics = """
                [00:00.00]Cosmic Horizons - Orion Beats
                [00:05.00]Voyaging past the nebula rings
                [00:11.00]Synthesizers hum like cosmic strings
                [00:17.00]Infinite space and boundless beats
                [00:23.00]Where harmonic waves and starlight meets
                [00:30.00]Floating free in zero gravity
                [00:36.00]Echoes of pure acoustic tranquility
            """.trimIndent()
        )
    )

    fun ensureSampleTracks(context: Context): List<Song> {
        val sampleDir = File(context.filesDir, "sample_audio")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val sampleSongs = mutableListOf<Song>()
        val sampleRate = 22050
        val numChannels = 1

        for (spec in SAMPLE_SPECS) {
            val audioFile = File(sampleDir, "track_${spec.id.toString().replace("-", "")}.wav")
            if (!audioFile.exists() || audioFile.length() < 1000) {
                try {
                    generateMelodicWav(audioFile, spec, sampleRate, numChannels)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            sampleSongs.add(
                Song(
                    id = spec.id,
                    title = spec.title,
                    artist = spec.artist,
                    album = spec.album,
                    durationMs = spec.durationSeconds * 1000L,
                    contentUri = audioFile.absolutePath,
                    trackNumber = 1,
                    year = 2024,
                    mimeType = "audio/wav",
                    dateAdded = System.currentTimeMillis() - (spec.id * 10000),
                    isFavorite = (spec.id == -101L || spec.id == -102L), // default sample favorites
                    customCoverUri = null,
                    customLyrics = spec.lrcLyrics,
                    isMissing = false,
                    playCount = if (spec.id == -101L) 12 else if (spec.id == -102L) 8 else 3,
                    lastPlayedTime = System.currentTimeMillis() - 3600000,
                    isSample = true
                )
            )
        }

        return sampleSongs
    }

    private fun generateMelodicWav(
        file: File,
        spec: SampleTrackSpec,
        sampleRate: Int,
        channels: Int
    ) {
        val totalSamples = sampleRate * spec.durationSeconds
        val bytesPerSample = 2
        val dataSize = totalSamples * channels * bytesPerSample

        FileOutputStream(file).use { fos ->
            // Write standard 44-byte WAV header
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + dataSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // PCM chunk size
            header.putShort(1.toShort()) // PCM format
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(sampleRate * channels * bytesPerSample)
            header.putShort((channels * bytesPerSample).toShort())
            header.putShort((bytesPerSample * 8).toShort())
            header.put("data".toByteArray())
            header.putInt(dataSize)
            fos.write(header.array())

            // Synthesize pleasant acoustic chord progression with envelope
            val buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN)
            val noteDurationSamples = (sampleRate * 1.5).toInt() // note changes every 1.5s
            val scale = spec.scale

            for (i in 0 until totalSamples) {
                val noteIdx = (i / noteDurationSamples) % scale.size
                val nextNoteIdx = ((i / noteDurationSamples) + 2) % scale.size
                val freq1 = scale[noteIdx]
                val freq2 = scale[nextNoteIdx] * 1.5 // fifth/harmonic
                val bassFreq = spec.baseFreq * 0.5

                // Envelope per note (soft attack and slow decay)
                val notePhase = (i % noteDurationSamples).toDouble() / noteDurationSamples
                val envelope = (sin(notePhase * PI)).coerceIn(0.0, 1.0)

                val t = i.toDouble() / sampleRate
                val wave1 = sin(2.0 * PI * freq1 * t)
                val wave2 = sin(2.0 * PI * freq2 * t) * 0.4
                val bass = sin(2.0 * PI * bassFreq * t) * 0.35
                val combined = (wave1 * 0.5 + wave2 * 0.25 + bass * 0.25) * envelope

                val sampleValue = (combined * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                if (!buffer.hasRemaining()) {
                    fos.write(buffer.array())
                    buffer.clear()
                }
                buffer.putShort(sampleValue)
            }

            if (buffer.position() > 0) {
                fos.write(buffer.array(), 0, buffer.position())
            }
        }
    }
}

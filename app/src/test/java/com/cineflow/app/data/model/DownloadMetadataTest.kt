package com.cineflow.app.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit test murni JVM — tidak butuh Android SDK (guard CI).
 *
 * Memverifikasi serialisasi/deserialisasi DownloadMetadata lewat Gson,
 * karena format ini dipakai untuk persistensi antrean unduhan.
 */
class DownloadMetadataTest {

    private val gson = Gson()

    @Test
    fun `serialize and deserialize preserves fields`() {
        val meta = DownloadMetadata(
            title = "Test Movie",
            episode = "E01",
            quality = "720p",
            posterUrl = "https://example.com/poster.jpg",
            bookId = "book-123",
            source = "movie_tv",
            chapterNum = 1,
            description = "Deskripsi film uji",
            episodeId = "ep-456",
            isAudioPart = false,
            timestamp = 12345L
        )

        val json = gson.toJson(meta)
        assertNotNull(json)
        assertEquals("Test Movie", gson.fromJson(json, DownloadMetadata::class.java).title)
        assertEquals("book-123", gson.fromJson(json, DownloadMetadata::class.java).bookId)
        assertEquals("720p", gson.fromJson(json, DownloadMetadata::class.java).quality)
        assertEquals(12345L, gson.fromJson(json, DownloadMetadata::class.java).timestamp)
    }

    @Test
    fun `defaults are applied when fields missing`() {
        val json = """{"title":"A","episode":"E","quality":"SD","poster_url":"","book_id":"b","source":"x"}"""
        val meta = gson.fromJson(json, DownloadMetadata::class.java)
        assertEquals(0, meta.chapterNum)
        assertEquals(0, meta.totalChapters)
    }
}

package com.cineflow.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Persistensi daftar download — model Tasirin Download Manager.
 *
 * Menyimpan daftar unduhan lengkap (snapshot) dan progres ringan secara
 * terpisah. Progres dipanggil berkala saat download aktif (hemat I/O);
 * snapshot penuh hanya saat ada perubahan struktur (tambah/hapus/status).
 */
class DownloadRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("downloads", Context.MODE_PRIVATE)

    /** Muat daftar unduhan dari SharedPreferences */
    fun load(): List<DownloadItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return DownloadItemCodec.decode(raw, coerceActiveToPaused = true)
    }

    /** Simpan progres kompak (id -> bytes/total) tanpa detail */
    fun saveProgress(items: List<DownloadItem>) {
        prefs.edit {
            putString(KEY_PROGRESS, DownloadItemCodec.encodeProgress(items))
        }
    }

    /** Simpan snapshot penuh (struktur berubah: tambah/hapus/status) */
    fun save(items: List<DownloadItem>) {
        prefs.edit {
            putString(KEY_ITEMS, DownloadItemCodec.encode(items))
            // Hapus progres ringan supaya tidak menimpa data lebih lama
            remove(KEY_PROGRESS)
        }
    }

    /** Tambah item baru ke daftar */
    fun add(item: DownloadItem): List<DownloadItem> {
        val items = load().toMutableList()
        items.add(item)
        save(items)
        return items
    }

    /** Update item berdasarkan ID */
    fun update(updatedItem: DownloadItem): List<DownloadItem> {
        val items = load().toMutableList()
        val index = items.indexOfFirst { it.id == updatedItem.id }
        if (index >= 0) {
            items[index] = updatedItem
            save(items)
        }
        return items
    }

    /** Hapus item berdasarkan ID */
    fun remove(id: String): List<DownloadItem> {
        val items = load().toMutableList()
        items.removeAll { it.id == id }
        save(items)
        return items
    }

    /** Hapus semua item selesai/gagal */
    fun clearFinished(): List<DownloadItem> {
        val items = load().toMutableList()
        items.removeAll { it.isFinished }
        save(items)
        return items
    }

    companion object {
        private const val KEY_ITEMS = "items"
        private const val KEY_PROGRESS = "progress"
    }
}

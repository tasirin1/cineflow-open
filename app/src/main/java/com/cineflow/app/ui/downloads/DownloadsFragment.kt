package com.cineflow.app.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cineflow.app.data.model.DownloadItem
import com.cineflow.app.data.repository.DownloadManager
import com.cineflow.app.util.AppLogger
import kotlinx.coroutines.launch

class DownloadsFragment : Fragment() {

    companion object {
        private const val TAG = "DownloadsFragment"
    }

    private lateinit var rvDownloads: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: DownloadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDownloads = view.findViewById(R.id.rv_downloads)
        tvEmpty = view.findViewById(R.id.tv_empty)

        adapter = DownloadAdapter { item -> deleteDownload(item) }
        rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        rvDownloads.adapter = adapter

        collectDownloads()
    }

    private fun collectDownloads() {
        val manager = DownloadManager.getInstance(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            manager.downloadItems.collect { items ->
                AppLogger.d(TAG, "downloadItems berubah: ${items.size} item")
                adapter.submitList(items)
                tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                rvDownloads.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun deleteDownload(item: DownloadItem) {
        AppLogger.i(TAG, "hapus unduhan: ${item.id} (${item.title})")
        DownloadManager.getInstance(requireContext()).deleteDownload(item.id)
    }
}

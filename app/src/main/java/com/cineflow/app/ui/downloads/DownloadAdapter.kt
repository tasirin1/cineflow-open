package com.cineflow.app.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cineflow.app.R
import com.cineflow.app.data.model.DownloadItem
import com.cineflow.app.databinding.ItemDownloadBinding
import java.io.File

class DownloadAdapter(
    private val onDelete: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    private val items = mutableListOf<DownloadItem>()

    fun submitList(newItems: List<DownloadItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DownloadViewHolder(
        private val binding: ItemDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DownloadItem) {
            binding.tvTitle.text = item.title
            binding.tvEpisode.text = buildString {
                append(item.episode)
                if (item.quality.isNotBlank()) append(" • ${item.quality}")
            }
            binding.tvStatus.text = if (item.progress > 0 && item.status != "Selesai") {
                "${item.status} (${item.progress}%)"
            } else {
                item.status
            }
            binding.progressBar.progress = item.progress

            val poster = item.localPosterPath?.let { File(it) } ?: item.posterUrl
            binding.ivPoster.load(poster) {
                placeholder(R.color.bg_surface_variant)
                error(R.color.bg_surface_variant)
            }

            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}

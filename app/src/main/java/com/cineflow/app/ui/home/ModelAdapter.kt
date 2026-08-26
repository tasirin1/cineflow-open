package com.cineflow.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.cineflow.app.R
import com.cineflow.app.data.model.StreamingModel

class ModelAdapter : ListAdapter<StreamingModel, ModelAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_model, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        fun bind(model: StreamingModel) {
            tvName.text = model.name
            tvDescription.text = model.description ?: "Tidak ada deskripsi"
            tvStatus.text = model.status ?: if (model.premium) "Premium" else "Gratis"

            ivIcon.load(model.iconUrl) {
                crossfade(true)
                transformations(RoundedCornersTransformation(8f))
                placeholder(R.color.bg_surface_variant)
                error(R.color.bg_surface_variant)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StreamingModel>() {
        override fun areItemsTheSame(oldItem: StreamingModel, newItem: StreamingModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StreamingModel, newItem: StreamingModel) = oldItem == newItem
    }
}

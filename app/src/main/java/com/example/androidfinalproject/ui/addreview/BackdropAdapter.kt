package com.example.androidfinalproject.ui.addreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.TmdbBackdrop
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class BackdropAdapter(
    private val onBackdropSelected: (TmdbBackdrop) -> Unit
) : ListAdapter<TmdbBackdrop, BackdropAdapter.BackdropViewHolder>(BackdropDiffCallback()) {

    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackdropViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_backdrop, parent, false)
        return BackdropViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackdropViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun setSelectedUrl(url: String) {
        val index = currentList.indexOfFirst { it.fullUrl == url }
        if (index >= 0 && index != selectedPosition) {
            val old = selectedPosition
            selectedPosition = index
            notifyItemChanged(old)
            notifyItemChanged(index)
        }
    }

    inner class BackdropViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.backdropCard)
        private val image: ImageView = itemView.findViewById(R.id.backdropImage)
        private val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)

        fun bind(backdrop: TmdbBackdrop, isSelected: Boolean) {
            Picasso.get()
                .load(backdrop.thumbnailUrl)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .fit()
                .centerCrop()
                .into(image)

            val strokeWidthPx = (2 * itemView.resources.displayMetrics.density).toInt()
            card.strokeWidth = if (isSelected) strokeWidthPx else 0
            checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onBackdropSelected(backdrop)
            }
        }
    }

    class BackdropDiffCallback : DiffUtil.ItemCallback<TmdbBackdrop>() {
        override fun areItemsTheSame(oldItem: TmdbBackdrop, newItem: TmdbBackdrop) =
            oldItem.filePath == newItem.filePath

        override fun areContentsTheSame(oldItem: TmdbBackdrop, newItem: TmdbBackdrop) =
            oldItem == newItem
    }
}

package com.example.androidfinalproject.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.Review

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieBanner: ImageView = itemView.findViewById(R.id.movieBanner)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieGenre: TextView = itemView.findViewById(R.id.movieGenre)
        private val userProfileImage: ShapeableImageView = itemView.findViewById(R.id.userProfileImage)

        fun bind(review: Review) {
            movieTitle.text = review.movieTitle
            userName.text = review.userFullName
            ratingBar.rating = review.rating

            if (review.movieGenre.isNotEmpty()) {
                movieGenre.text = review.movieGenre
                movieGenre.visibility = View.VISIBLE
            } else {
                movieGenre.visibility = View.GONE
            }

            Picasso.get()
                .load(review.movieBannerUrl)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .fit()
                .centerCrop()
                .into(movieBanner)

            if (review.userProfilePictureUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.userProfilePictureUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(userProfileImage)
            } else {
                userProfileImage.setImageResource(R.drawable.ic_person)
            }
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}

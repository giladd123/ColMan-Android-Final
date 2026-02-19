package com.example.androidfinalproject.ui.addreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.TmdbMovie
import com.example.androidfinalproject.data.remote.TmdbGenres
import com.squareup.picasso.Picasso

class MovieSearchAdapter(
    private val onMovieClick: (TmdbMovie) -> Unit
) : ListAdapter<TmdbMovie, MovieSearchAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_search, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moviePoster: ImageView = itemView.findViewById(R.id.moviePoster)
        private val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieDetails: TextView = itemView.findViewById(R.id.movieDetails)

        fun bind(movie: TmdbMovie) {
            movieTitle.text = movie.title

            val genres = TmdbGenres.getGenreNames(movie.genreIds)
            val details = listOfNotNull(
                movie.year.takeIf { it.isNotEmpty() },
                genres.takeIf { it.isNotEmpty() }
            ).joinToString(" \u00B7 ")
            movieDetails.text = details

            val imageUrl = movie.posterUrl ?: movie.backdropUrl
            if (imageUrl != null) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .fit()
                    .centerCrop()
                    .into(moviePoster)
            } else {
                moviePoster.setImageResource(R.drawable.placeholder_movie)
            }

            itemView.setOnClickListener { onMovieClick(movie) }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<TmdbMovie>() {
        override fun areItemsTheSame(oldItem: TmdbMovie, newItem: TmdbMovie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TmdbMovie, newItem: TmdbMovie): Boolean {
            return oldItem == newItem
        }
    }
}

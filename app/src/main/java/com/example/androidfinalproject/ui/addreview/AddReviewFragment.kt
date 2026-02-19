package com.example.androidfinalproject.ui.addreview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.TmdbMovie
import com.example.androidfinalproject.data.remote.TmdbGenres
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso

class AddReviewFragment : Fragment() {

    private val viewModel: AddReviewViewModel by viewModels()
    private lateinit var movieSearchAdapter: MovieSearchAdapter
    private lateinit var backdropAdapter: BackdropAdapter

    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchProgressBar: ProgressBar
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var reviewFormScrollView: NestedScrollView
    private lateinit var selectedMovieBanner: ImageView
    private lateinit var selectedMovieTitle: TextView
    private lateinit var selectedMovieGenre: TextView
    private lateinit var clearMovieButton: ImageButton
    private lateinit var chooseBackdropLabel: TextView
    private lateinit var backdropProgressBar: ProgressBar
    private lateinit var backdropRecyclerView: RecyclerView
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var submitProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupSearch()
        setupRecyclerView()
        setupBackdropRecyclerView()
        setupActions()
        observeViewModel()
    }

    private fun bindViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        searchProgressBar = view.findViewById(R.id.searchProgressBar)
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        reviewFormScrollView = view.findViewById(R.id.reviewFormScrollView)
        selectedMovieBanner = view.findViewById(R.id.selectedMovieBanner)
        selectedMovieTitle = view.findViewById(R.id.selectedMovieTitle)
        selectedMovieGenre = view.findViewById(R.id.selectedMovieGenre)
        clearMovieButton = view.findViewById(R.id.clearMovieButton)
        chooseBackdropLabel = view.findViewById(R.id.chooseBackdropLabel)
        backdropProgressBar = view.findViewById(R.id.backdropProgressBar)
        backdropRecyclerView = view.findViewById(R.id.backdropRecyclerView)
        ratingBar = view.findViewById(R.id.ratingBar)
        reviewEditText = view.findViewById(R.id.reviewEditText)
        submitButton = view.findViewById(R.id.submitButton)
        submitProgressBar = view.findViewById(R.id.submitProgressBar)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchMovies(s?.toString() ?: "")
            }
        })
    }

    private fun setupRecyclerView() {
        movieSearchAdapter = MovieSearchAdapter { movie ->
            viewModel.selectMovie(movie)
            searchEditText.setText("")
            searchEditText.clearFocus()
        }
        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieSearchAdapter
        }
    }

    private fun setupBackdropRecyclerView() {
        backdropAdapter = BackdropAdapter { backdrop ->
            viewModel.selectBackdrop(backdrop)
        }
        backdropRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = backdropAdapter
        }
    }

    private fun setupActions() {
        clearMovieButton.setOnClickListener {
            viewModel.clearSelectedMovie()
        }

        ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            updateSubmitButtonState()
        }

        reviewEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSubmitButtonState()
            }
        })

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewText = reviewEditText.text?.toString()?.trim() ?: ""
            viewModel.submitReview(rating, reviewText)
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            movieSearchAdapter.submitList(results)
            if (results.isNotEmpty()) {
                searchResultsRecyclerView.visibility = View.VISIBLE
                reviewFormScrollView.visibility = View.GONE
            } else {
                searchResultsRecyclerView.visibility = View.GONE
                if (viewModel.selectedMovie.value != null) {
                    reviewFormScrollView.visibility = View.VISIBLE
                }
            }
        }

        viewModel.selectedMovie.observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                showSelectedMovie(movie)
                reviewFormScrollView.visibility = View.VISIBLE
                searchResultsRecyclerView.visibility = View.GONE
            } else {
                reviewFormScrollView.visibility = View.GONE
                ratingBar.rating = 0f
                reviewEditText.setText("")
            }
            updateSubmitButtonState()
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            searchProgressBar.visibility = if (isSearching) View.VISIBLE else View.GONE
        }

        viewModel.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
            submitProgressBar.visibility = if (isSubmitting) View.VISIBLE else View.GONE
            submitButton.isEnabled = !isSubmitting
            searchEditText.isEnabled = !isSubmitting
        }

        viewModel.submitResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(requireContext(), R.string.review_submitted, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), R.string.review_submit_failed, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearSubmitResult()
            }
        }

        viewModel.backdrops.observe(viewLifecycleOwner) { backdrops ->
            if (backdrops.isNotEmpty()) {
                chooseBackdropLabel.visibility = View.VISIBLE
                backdropRecyclerView.visibility = View.VISIBLE
                backdropAdapter.submitList(backdrops)
            } else {
                chooseBackdropLabel.visibility = View.GONE
                backdropRecyclerView.visibility = View.GONE
            }
        }

        viewModel.isLoadingBackdrops.observe(viewLifecycleOwner) { isLoading ->
            backdropProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.selectedBackdropUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .fit()
                    .centerCrop()
                    .into(selectedMovieBanner)
                backdropAdapter.setSelectedUrl(url)
            }
        }
    }

    private fun showSelectedMovie(movie: TmdbMovie) {
        selectedMovieTitle.text = movie.title

        val genres = TmdbGenres.getGenreNames(movie.genreIds)
        val details = listOfNotNull(
            movie.year.takeIf { it.isNotEmpty() },
            genres.takeIf { it.isNotEmpty() }
        ).joinToString(" \u00B7 ")
        selectedMovieGenre.text = details

        // Banner image is handled by selectedBackdropUrl observer
        if (viewModel.selectedBackdropUrl.value.isNullOrEmpty()) {
            val imageUrl = movie.backdropUrl ?: movie.posterUrl
            if (imageUrl != null) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .fit()
                    .centerCrop()
                    .into(selectedMovieBanner)
            } else {
                selectedMovieBanner.setImageResource(R.drawable.placeholder_movie)
            }
        }
    }

    private fun updateSubmitButtonState() {
        val hasMovie = viewModel.selectedMovie.value != null
        val hasRating = ratingBar.rating > 0f
        val hasReview = !reviewEditText.text.isNullOrBlank()
        submitButton.isEnabled = hasMovie && hasRating && hasReview
    }
}

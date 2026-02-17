package com.example.androidfinalproject.ui.addreview

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AddReviewFragment : Fragment() {

    private val viewModel: AddReviewViewModel by viewModels()

    private lateinit var editTextMovieName: TextInputEditText
    private lateinit var editTextMovieDescription: TextInputEditText
    private lateinit var reviewTextInput: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var movieBannerImageView: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSaveReview: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: android.net.Uri? = null
    private var uploadedImageUrl: String? = null
    private var isLoading = false
    private var isUploading = false
    private var isFetchingMovie = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadReviewImage(it)
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .fit()
                .centerCrop()
                .into(movieBannerImageView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        editTextMovieName = view.findViewById(R.id.editTextMovieName)
        editTextMovieDescription = view.findViewById(R.id.editTextMovieDescription)
        reviewTextInput = view.findViewById(R.id.reviewTextInput)

        ratingBar = view.findViewById(R.id.ratingBar)
        movieBannerImageView = view.findViewById(R.id.movieBannerImageView)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnSaveReview = view.findViewById(R.id.btnSaveReview)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        editTextMovieName.setOnEditorActionListener { _, actionId, event ->
            val isEnterAction = actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isEnterAction) {
                val movieName = editTextMovieName.text?.toString().orEmpty().trim()
                if (movieName.isNotEmpty()) {
                    fetchMovieFromOMDB(movieName)
                }
                true
            } else {
                false
            }
        }

        btnSaveReview.setOnClickListener {
            saveReview()
        }
    }

    private fun saveReview() {
        val movieTitle = editTextMovieName.text.toString().trim()
        val reviewText = reviewTextInput.text.toString().trim()
        val rating = ratingBar.rating
        val movieBannerUrl = uploadedImageUrl ?: viewModel.movieDetails.value?.posterUrl.orEmpty()

        viewModel.createReview(
            movieTitle = movieTitle,
            movieBannerUrl = movieBannerUrl,
            rating = rating,
            reviewText = reviewText
        )
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            this.isLoading = isLoading
            updateProgress()
            btnSaveReview.isEnabled = !isLoading
        }

        viewModel.isUploadingImage.observe(viewLifecycleOwner) { isUploading ->
            this.isUploading = isUploading
            updateProgress()
            btnSelectImage.isEnabled = !isUploading
            btnSelectImage.text = if (isUploading) {
                getString(R.string.uploading_image)
            } else {
                getString(R.string.select_image)
            }
        }

        viewModel.uploadedImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            imageUrl?.let {
                uploadedImageUrl = it
                Toast.makeText(
                    requireContext(),
                    R.string.image_uploaded_successfully,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.addReviewState.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        R.string.review_created_successfully,
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.clearState()
                    clearInputs()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.review_creation_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateProgress() {
        progressBar.visibility = if (isLoading || isUploading || isFetchingMovie) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun clearInputs() {
        editTextMovieName.setText("")
        editTextMovieDescription.setText("")
        reviewTextInput.setText("")
        ratingBar.rating = 0f
        selectedImageUri = null
        uploadedImageUrl = null
        movieBannerImageView.setImageResource(R.drawable.placeholder_movie)
        btnSelectImage.visibility = View.VISIBLE
    }

    private fun fetchMovieFromOMDB(movieName: String) {
        isFetchingMovie = true
        updateProgress()

        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val encodedName = URLEncoder.encode(movieName, "UTF-8")
                    val url = URL("https://www.omdbapi.com/?t=$encodedName&apikey=fda29bd6")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        return@withContext Result.failure(
                            IllegalStateException("OMDb request failed with $responseCode")
                        )
                    }

                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val responseStatus = json.optString("Response", "False")
                    if (responseStatus != "True") {
                        return@withContext Result.failure(IllegalStateException("Movie not found"))
                    }

                    val title = json.optString("Title", "")
                    val plot = json.optString("Plot", "")
                    val poster = json.optString("Poster", "")

                    Result.success(MovieResponse(title, plot, poster))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            isFetchingMovie = false
            updateProgress()

            if (!isAdded) return@launch

            if (result.isSuccess) {
                val data = result.getOrNull() ?: return@launch
                editTextMovieName.setText(data.title)
                editTextMovieDescription.setText(data.plot)

                if (data.poster.isNotBlank() && data.poster != "N/A") {
                    uploadedImageUrl = data.poster
                    btnSelectImage.visibility = View.GONE
                    Picasso.get()
                        .load(data.poster)
                        .placeholder(R.drawable.placeholder_movie)
                        .error(R.drawable.placeholder_movie)
                        .fit()
                        .centerCrop()
                        .into(movieBannerImageView)
                } else {
                    uploadedImageUrl = null
                    btnSelectImage.visibility = View.VISIBLE
                    movieBannerImageView.setImageResource(R.drawable.placeholder_movie)
                }
            } else {
                Toast.makeText(requireContext(), "Movie not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private data class MovieResponse(
        val title: String,
        val plot: String,
        val poster: String
    )
}

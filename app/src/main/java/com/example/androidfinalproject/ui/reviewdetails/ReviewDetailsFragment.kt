package com.example.androidfinalproject.ui.reviewdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.Review
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewDetailsFragment : Fragment() {

    private val viewModel: ReviewDetailsViewModel by viewModels()
    private val args: ReviewDetailsFragmentArgs by navArgs()

    private lateinit var editTextMovieTitle: TextInputEditText
    private lateinit var editTextReviewText: TextInputEditText
    private lateinit var textViewUserName: MaterialTextView
    private lateinit var textViewReviewDate: MaterialTextView
    private lateinit var ratingBar: RatingBar
    private lateinit var movieBannerImageView: ImageView
    private lateinit var btnEditSave: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var isEditMode = false
    private var currentReview: Review? = null
    private var uploadedImageUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
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
        return inflater.inflate(R.layout.fragment_review_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupListeners()
        observeViewModel()
        
        // Load the review using the reviewId from SafeArgs
        viewModel.loadReview(args.reviewId)
    }

    private fun initializeViews(view: View) {
        editTextMovieTitle = view.findViewById(R.id.editTextMovieTitle)
        editTextReviewText = view.findViewById(R.id.editTextReviewText)
        textViewUserName = view.findViewById(R.id.textViewUserName)
        textViewReviewDate = view.findViewById(R.id.textViewReviewDate)
        ratingBar = view.findViewById(R.id.ratingBar)
        movieBannerImageView = view.findViewById(R.id.movieBannerImageView)
        btnEditSave = view.findViewById(R.id.btnEditSave)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnEditSave.setOnClickListener {
            if (isEditMode) {
                saveReview()
            } else {
                enterEditMode()
            }
        }

        movieBannerImageView.setOnClickListener {
            if (isEditMode) {
                imagePickerLauncher.launch("image/*")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.review.observe(viewLifecycleOwner) { review ->
            if (review != null) {
                currentReview = review
                displayReviewData(review)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnEditSave.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(requireContext(), R.string.image_uploaded_successfully, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearUploadStatus()
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        R.string.review_updated_successfully,
                        Toast.LENGTH_SHORT
                    ).show()
                    exitEditMode()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), R.string.failed_to_update_review, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearUpdateStatus()
            }
        }
    }

    private fun displayReviewData(review: Review) {
        editTextMovieTitle.setText(review.movieTitle)
        editTextReviewText.setText(review.reviewText)
        textViewUserName.text = getString(R.string.reviewed_by, review.userFullName)
        textViewReviewDate.text = formatDate(review.timestamp)
        ratingBar.rating = review.rating

        // Load image
        if (review.movieBannerUrl.isNotBlank()) {
            uploadedImageUrl = review.movieBannerUrl
            Picasso.get()
                .load(review.movieBannerUrl)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .fit()
                .centerCrop()
                .into(movieBannerImageView)
        }
    }

    private fun enterEditMode() {
        isEditMode = true
        editTextMovieTitle.isEnabled = true
        editTextMovieTitle.isFocusableInTouchMode = true
        editTextReviewText.isEnabled = true
        editTextReviewText.isFocusableInTouchMode = true
        ratingBar.isEnabled = true
        btnEditSave.text = getString(R.string.save_changes)
    }

    private fun exitEditMode() {
        isEditMode = false
        editTextMovieTitle.isEnabled = false
        editTextMovieTitle.isFocusable = false
        editTextReviewText.isEnabled = false
        editTextReviewText.isFocusable = false
        ratingBar.isEnabled = false
        btnEditSave.text = getString(R.string.edit)
    }

    private fun saveReview() {
        val movieTitle = editTextMovieTitle.text.toString().trim()
        val reviewText = editTextReviewText.text.toString().trim()
        val rating = ratingBar.rating

        if (movieTitle.isBlank()) {
            Toast.makeText(requireContext(), R.string.movie_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        if (reviewText.isBlank()) {
            Toast.makeText(requireContext(), R.string.review_text_required, Toast.LENGTH_SHORT).show()
            return
        }

        if (rating <= 0f) {
            Toast.makeText(requireContext(), R.string.rating_required, Toast.LENGTH_SHORT).show()
            return
        }

        // Update the review
        currentReview?.let { review ->
            val updatedReview = review.copy(
                movieTitle = movieTitle,
                reviewText = reviewText,
                rating = rating,
                movieBannerUrl = uploadedImageUrl ?: review.movieBannerUrl
            )
            viewModel.updateReview(updatedReview)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

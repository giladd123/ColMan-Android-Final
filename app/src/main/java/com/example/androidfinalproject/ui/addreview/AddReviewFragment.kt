package com.example.androidfinalproject.ui.addreview

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
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddReviewFragment : Fragment() {

    private val viewModel: AddReviewViewModel by viewModels()

    private lateinit var movieTitleInput: TextInputEditText
    private lateinit var movieTitleLayout: TextInputLayout
    private lateinit var reviewTextInput: TextInputEditText
    private lateinit var reviewTextLayout: TextInputLayout
    private lateinit var ratingBar: RatingBar
    private lateinit var movieBannerImageView: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSaveReview: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: android.net.Uri? = null
    private var uploadedImageUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadReviewImage(it)
            movieBannerImageView.setImageURI(it)
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
        movieTitleLayout = view.findViewById(R.id.movieTitleLayout)
        movieTitleInput = movieTitleLayout.editText as TextInputEditText

        reviewTextLayout = view.findViewById(R.id.reviewTextLayout)
        reviewTextInput = reviewTextLayout.editText as TextInputEditText

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

        btnSaveReview.setOnClickListener {
            saveReview()
        }
    }

    private fun saveReview() {
        val movieTitle = movieTitleInput.text.toString().trim()
        val reviewText = reviewTextInput.text.toString().trim()
        val rating = ratingBar.rating
        val movieBannerUrl = uploadedImageUrl ?: ""

        viewModel.createReview(
            movieTitle = movieTitle,
            movieBannerUrl = movieBannerUrl,
            rating = rating,
            reviewText = reviewText
        )
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSaveReview.isEnabled = !isLoading
            btnSelectImage.isEnabled = !isLoading
        }

        viewModel.isUploadingImage.observe(viewLifecycleOwner) { isUploading ->
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
}

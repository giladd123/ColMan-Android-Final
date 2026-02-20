package com.example.androidfinalproject.ui.reviewdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.ui.addreview.BackdropAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewDetailFragment : Fragment() {

    private val viewModel: ReviewDetailViewModel by viewModels()
    private val args: ReviewDetailFragmentArgs by navArgs()

    private var currentReview: Review? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        NavigationUI.setupWithNavController(toolbar, findNavController())
        toolbar.inflateMenu(R.menu.menu_review_detail)

        // Hide menu items until we know if user is owner
        toolbar.menu.findItem(R.id.action_edit)?.isVisible = false
        toolbar.menu.findItem(R.id.action_delete)?.isVisible = false

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    currentReview?.let { showEditDialog(it) }
                    true
                }
                R.id.action_delete -> {
                    currentReview?.let { showDeleteConfirmation(it) }
                    true
                }
                else -> false
            }
        }

        val movieBanner = view.findViewById<ImageView>(R.id.movieBanner)
        val movieTitle = view.findViewById<MaterialTextView>(R.id.movieTitle)
        val movieGenre = view.findViewById<MaterialTextView>(R.id.movieGenre)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val userProfileImage = view.findViewById<ShapeableImageView>(R.id.userProfileImage)
        val userName = view.findViewById<MaterialTextView>(R.id.userName)
        val reviewDate = view.findViewById<MaterialTextView>(R.id.reviewDate)
        val reviewText = view.findViewById<MaterialTextView>(R.id.reviewText)

        viewModel.getReview(args.reviewId).observe(viewLifecycleOwner) { review ->
            review?.let {
                currentReview = it

                movieTitle.text = it.movieTitle
                ratingBar.rating = it.rating
                reviewText.text = it.reviewText
                userName.text = it.userFullName

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                reviewDate.text = dateFormat.format(Date(it.timestamp))

                if (it.movieGenre.isNotEmpty()) {
                    movieGenre.text = it.movieGenre
                    movieGenre.visibility = View.VISIBLE
                } else {
                    movieGenre.visibility = View.GONE
                }

                if (it.movieBannerUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.movieBannerUrl)
                        .placeholder(R.drawable.placeholder_movie)
                        .error(R.drawable.placeholder_movie)
                        .into(movieBanner)
                } else {
                    movieBanner.setImageResource(R.drawable.placeholder_movie)
                }

                if (it.userProfilePictureUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.userProfilePictureUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(userProfileImage)
                } else {
                    userProfileImage.setImageResource(R.drawable.ic_person)
                }

                // Show edit/delete only for the owner
                val isOwner = viewModel.isOwner(it)
                toolbar.menu.findItem(R.id.action_edit)?.isVisible = isOwner
                toolbar.menu.findItem(R.id.action_delete)?.isVisible = isOwner
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            if (result.isSuccess) {
                Toast.makeText(requireContext(), R.string.review_deleted, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), R.string.review_delete_failed, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            if (result.isSuccess) {
                Toast.makeText(requireContext(), R.string.review_updated, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.review_update_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(review: Review) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_review, null)

        val editRatingBar = dialogView.findViewById<RatingBar>(R.id.editRatingBar)
        val editReviewText = dialogView.findViewById<TextInputEditText>(R.id.editReviewText)
        val editBackdropLabel = dialogView.findViewById<View>(R.id.editBackdropLabel)
        val editBackdropProgressBar = dialogView.findViewById<ProgressBar>(R.id.editBackdropProgressBar)
        val editBackdropRecyclerView = dialogView.findViewById<RecyclerView>(R.id.editBackdropRecyclerView)

        editRatingBar.rating = review.rating
        editReviewText.setText(review.reviewText)

        var selectedBackdropUrl = review.movieBannerUrl

        val backdropAdapter = BackdropAdapter { backdrop ->
            selectedBackdropUrl = backdrop.fullUrl
        }
        editBackdropRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = backdropAdapter
        }

        viewModel.fetchBackdropsForMovie(review.movieTmdbId)

        viewModel.backdrops.observe(viewLifecycleOwner) { backdrops ->
            if (backdrops.isNotEmpty()) {
                editBackdropLabel.visibility = View.VISIBLE
                editBackdropRecyclerView.visibility = View.VISIBLE
                backdropAdapter.submitList(backdrops)
                backdropAdapter.setSelectedUrl(selectedBackdropUrl)
            }
        }

        viewModel.isLoadingBackdrops.observe(viewLifecycleOwner) { isLoading ->
            editBackdropProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_review)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newRating = editRatingBar.rating
                val newText = editReviewText.text?.toString()?.trim() ?: ""
                if (newRating > 0 && newText.isNotEmpty()) {
                    if (selectedBackdropUrl != review.movieBannerUrl) {
                        viewModel.updateReview(review.id, newRating, newText, selectedBackdropUrl)
                    } else {
                        viewModel.updateReview(review.id, newRating, newText)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmation(review: Review) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_review_confirm_title)
            .setMessage(R.string.delete_review_confirm_message)
            .setPositiveButton(R.string.delete_review) { _, _ ->
                viewModel.deleteReview(review.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

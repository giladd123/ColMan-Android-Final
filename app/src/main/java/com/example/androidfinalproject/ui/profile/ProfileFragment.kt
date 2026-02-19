package com.example.androidfinalproject.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.androidfinalproject.R
import com.example.androidfinalproject.ui.home.ReviewAdapter
import com.firebase.ui.auth.AuthUI
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var profileImage: ShapeableImageView
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInput: TextInputEditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var profileProgressBar: ProgressBar
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    private var currentPhotoUrl: String? = null
    private var pendingPhotoUrl: String? = null
    private var originalName: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadProfilePicture(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupListeners()
        setupLogout(view)
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        nameInputLayout = view.findViewById(R.id.nameInputLayout)
        nameInput = nameInputLayout.editText as TextInputEditText
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        profileProgressBar = view.findViewById(R.id.profileProgressBar)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        reviewsRecyclerView = view.findViewById(R.id.reviewsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter { review ->
            val action = ProfileFragmentDirections.actionProfileFragmentToReviewDetailFragment(review.id)
            findNavController().navigate(action)
        }
        reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun setupListeners() {
        view?.findViewById<FloatingActionButton>(R.id.fabEditPhoto)?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        nameInput.doAfterTextChanged {
            updateSaveButtonState()
        }

        btnSaveProfile.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val photoUrl = pendingPhotoUrl ?: currentPhotoUrl
            viewModel.saveProfile(name, photoUrl)
        }
    }

    private fun updateSaveButtonState() {
        val currentName = nameInput.text.toString().trim()
        val hasNameChanged = originalName != null && currentName != originalName
        val hasPhotoChanged = pendingPhotoUrl != null

        btnSaveProfile.isEnabled = (hasNameChanged || hasPhotoChanged) && currentName.isNotBlank()
    }

    private fun setupLogout(view: View) {
        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                if (originalName == null) {
                    originalName = it.fullName
                    nameInput.setText(it.fullName)
                } else if (pendingPhotoUrl == null && !nameInput.hasFocus()) {
                    // Only update text if not editing and no pending photo (to avoid resetting during upload)
                    nameInput.setText(it.fullName)
                    originalName = it.fullName // Sync original name with DB update
                }
                
                currentPhotoUrl = it.photoUrl
                updateSaveButtonState()
                
                if (it.photoUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.ic_person)
                }
            }
        }

        viewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            reviewAdapter.submitList(reviews)
            emptyStateText.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { isSaving ->
            profileProgressBar.visibility = if (isSaving) View.VISIBLE else View.GONE
            if (isSaving) {
                btnSaveProfile.isEnabled = false
            } else {
                updateSaveButtonState()
            }
        }

        viewModel.isUploadingPhoto.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                Toast.makeText(requireContext(), R.string.uploading_image, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.uploadedPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            photoUrl?.let {
                pendingPhotoUrl = it
                updateSaveButtonState()
                Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(profileImage)
                viewModel.clearUploadedPhotoUrl()
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show()
                    pendingPhotoUrl = null
                    originalName = nameInput.text.toString().trim() // Reset original name to current
                    updateSaveButtonState()
                } else {
                    Toast.makeText(requireContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearSaveStatus()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}


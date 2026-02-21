package com.example.androidfinalproject.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidfinalproject.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso

class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setPhotoUri(it)
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.ic_person)
                .fit()
                .centerCrop()
                .into(view?.findViewById<ShapeableImageView>(R.id.signUpProfileImage))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileImage = view.findViewById<ShapeableImageView>(R.id.signUpProfileImage)
        val fabAddPhoto = view.findViewById<FloatingActionButton>(R.id.fabAddPhoto)
        val displayNameLayout = view.findViewById<TextInputLayout>(R.id.displayNameInputLayout)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailInputLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)
        val btnCreateAccount = view.findViewById<MaterialButton>(R.id.btnCreateAccount)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { findNavController().navigateUp() }

        profileImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        fabAddPhoto.setOnClickListener { imagePickerLauncher.launch("image/*") }

        btnCreateAccount.setOnClickListener {
            val displayName = displayNameLayout.editText?.text.toString().trim()
            val email = emailLayout.editText?.text.toString().trim()
            val password = passwordLayout.editText?.text.toString()
            val confirmPassword = confirmPasswordLayout.editText?.text.toString()

            displayNameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null

            if (displayName.isEmpty()) {
                displayNameLayout.error = getString(R.string.error_name_required)
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailLayout.error = getString(R.string.error_email_required)
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordLayout.error = getString(R.string.error_password_too_short)
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordLayout.error = getString(R.string.error_passwords_dont_match)
                return@setOnClickListener
            }

            viewModel.signUp(email, password, displayName)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnCreateAccount.isEnabled = !isLoading
        }

        viewModel.signUpResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    findNavController().navigate(
                        SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                    )
                }
                it.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage ?: getString(R.string.sign_up_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                viewModel.clearResult()
            }
        }
    }
}

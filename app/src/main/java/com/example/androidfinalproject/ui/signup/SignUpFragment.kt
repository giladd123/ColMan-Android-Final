package com.example.androidfinalproject.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidfinalproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailInputLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)
        val btnCreateAccount = view.findViewById<MaterialButton>(R.id.btnCreateAccount)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnCreateAccount.setOnClickListener {
            val email = emailLayout.editText?.text.toString().trim()
            val password = passwordLayout.editText?.text.toString()
            val confirmPassword = confirmPasswordLayout.editText?.text.toString()

            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null

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

            viewModel.signUp(email, password)
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

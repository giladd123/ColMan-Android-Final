package com.example.androidfinalproject.ui.signin

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

class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailInputLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val btnSignIn = view.findViewById<MaterialButton>(R.id.btnSignIn)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnSignIn.setOnClickListener {
            val email = emailLayout.editText?.text.toString().trim()
            val password = passwordLayout.editText?.text.toString()

            emailLayout.error = null
            passwordLayout.error = null

            if (email.isEmpty()) {
                emailLayout.error = getString(R.string.error_email_required)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordLayout.error = getString(R.string.error_password_required)
                return@setOnClickListener
            }

            viewModel.signIn(email, password)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSignIn.isEnabled = !isLoading
        }

        viewModel.signInResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    findNavController().navigate(
                        SignInFragmentDirections.actionSignInFragmentToHomeFragment()
                    )
                }
                it.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage ?: getString(R.string.sign_in_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                viewModel.clearResult()
            }
        }
    }
}

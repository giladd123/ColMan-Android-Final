package com.example.androidfinalproject.ui.login

import android.app.Activity
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
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onSignInSuccess()
        } else {
            viewModel.onSignInFailed(getString(R.string.sign_in_failed))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSignInWithGoogle = view.findViewById<MaterialButton>(R.id.btnSignInWithGoogle)
        val btnSignIn = view.findViewById<MaterialButton>(R.id.btnSignIn)
        val btnSignUp = view.findViewById<MaterialButton>(R.id.btnSignUp)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // If already signed in, navigate directly
        if (viewModel.currentUser != null) {
            navigateToHome()
            return
        }

        btnSignInWithGoogle.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnSignInWithGoogle.isEnabled = false
            val signInIntent = viewModel.buildGoogleSignInIntent()
            signInLauncher.launch(signInIntent)
        }

        btnSignIn.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignInFragment()
            )
        }

        btnSignUp.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            )
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            btnSignInWithGoogle.isEnabled = true

            result?.let {
                it.onSuccess {
                    navigateToHome()
                }
                it.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage ?: getString(R.string.sign_in_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                viewModel.clearLoginResult()
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        )
    }
}

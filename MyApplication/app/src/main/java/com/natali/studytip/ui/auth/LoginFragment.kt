package com.natali.studytip.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.natali.studytip.R
import com.natali.studytip.StudyTipsApplication
import com.natali.studytip.databinding.FragmentLoginBinding
import com.natali.studytip.ui.ViewModelFactory

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(
            requireActivity().application,
            (requireActivity().application as StudyTipsApplication).tipRepository,
            (requireActivity().application as StudyTipsApplication).userRepository,
            (requireActivity().application as StudyTipsApplication).quoteRepository,
            (requireActivity().application as StudyTipsApplication).authRepository,
            (requireActivity().application as StudyTipsApplication).firebaseStorageManager,
            (requireActivity().application as StudyTipsApplication).firestoreManager
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        checkAutoLogin()
    }

    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loginButton.isEnabled = !isLoading
            binding.progressBar.isVisible = isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe authentication success
        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToHome()
            }
        }
    }

    private fun setupClickListeners() {
        binding.signupLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()
            viewModel.signIn(email, password)
        }
    }

    private fun checkAutoLogin() {
        viewModel.checkAutoLogin(
            onSuccess = { navigateToHome() },
            onFailure = { /* User not logged in, stay on login screen */ }
        )
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_login_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

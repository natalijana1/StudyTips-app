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
import com.natali.studytip.databinding.FragmentSignupBinding
import com.natali.studytip.ui.ViewModelFactory

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
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
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.signupButton.isEnabled = !isLoading
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
        binding.signinLink.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
        }

        binding.signupButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()

            // Pass password twice to maintain ViewModel signature (no confirm password in UI)
            viewModel.signUp(name, email, password, password)
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_signup_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

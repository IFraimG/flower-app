package com.example.ecoapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecoapp.MainActivity;
import com.example.ecoapp.R;
import com.example.ecoapp.databinding.FragmentRegistrationBinding;
import com.example.ecoapp.domain.helpers.StorageHandler;
import com.example.ecoapp.presentation.viewmodels.AuthViewModel;

import org.jetbrains.annotations.NotNull;

public class AuthSignupFragment extends Fragment {
    private FragmentRegistrationBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       binding = FragmentRegistrationBinding.inflate(getLayoutInflater());

        if (new StorageHandler(requireContext()).getAuth()) pushData();

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.loginTextView.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_authSignupFragment_to_authLoginFragment));
       binding.registrationCardView.setOnClickListener(v -> {

           String name = binding.firstNameEditText.getText().toString();
           String lastname = binding.lastNameEditText.getText().toString();
           String email = binding.emailEditText.getText().toString();
           String password = binding.passwordEditText.getText().toString();
           String confirmPassword = binding.confirmPasswordEditText.getText().toString();

           if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()) {
               binding.registrationError.setText("Вы заполнили не все поля");
           } else if (!password.equals(confirmPassword)) {
               binding.registrationError.setText("Вы неправильно подтвердили пароль");
           } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
               binding.registrationError.setText("Вы ввели email некорректно");
           } else {
               binding.registrationError.setText("");

               viewModel.signup(name + " " + lastname, password, email).observe(requireActivity(), statusCode -> {
                   if (statusCode != 0) {
                       if (statusCode >= 400) {
                           binding.registrationError.setText("Пользователь с данным email уже существует");
                       } else {
                           binding.firstNameEditText.setText("");
                           binding.lastNameEditText.setText("");
                           binding.emailEditText.setText("");
                           binding.passwordEditText.setText("");
                           binding.confirmPasswordEditText.setText("");

                           pushData();
                       }
                   }
               });
           }
       });


        return binding.getRoot();
    }

    private void pushData() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).changeMenu(true);
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_main_fragment);
            navHostFragment.getNavController().navigate(R.id.homeFragment);
        }
    }
}
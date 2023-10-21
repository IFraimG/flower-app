package com.example.ecoapp.presentation.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoapp.data.models.User;
import com.example.ecoapp.presentation.adapters.UserScoresAdapter;
import com.example.ecoapp.presentation.viewmodels.EventViewModel;
import com.example.ecoapp.presentation.viewmodels.ProfileViewModel;

import java.util.ArrayList;

public class UserListDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        EventViewModel viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        Bundle args = getArguments();

        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (args != null) {
            String eventID = args.getString("eventID");
            viewModel.getUsersScores(eventID).observe(requireActivity(), users -> {
                if (users != null) {
                    UserScoresAdapter adapter = new UserScoresAdapter(users);
                    adapter.setOnItemClickListener((position, scores) -> {
                        if (!scores.isEmpty()) {
                            profileViewModel.updateUserScores(users.get(position).getId(), Integer.parseInt(scores)).observe(requireActivity(), statusCode -> {
                                if (statusCode >= 200 && statusCode < 400) {
                                    Toast.makeText(requireContext(), "Успешно!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    recyclerView.setAdapter(adapter);
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Список пользователей").setView(recyclerView);

        AlertDialog dialog = builder.create();

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
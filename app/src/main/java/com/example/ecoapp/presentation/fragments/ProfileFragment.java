package com.example.ecoapp.presentation.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.example.ecoapp.presentation.MainActivity;
import com.example.ecoapp.R;
import com.example.ecoapp.databinding.FragmentProfileBinding;
import com.example.ecoapp.domain.helpers.StorageHandler;
import com.example.ecoapp.presentation.viewmodels.ProfileViewModel;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentProfileBinding binding;
    private int SELECT_PHOTO_PROFILE = 1;
    private Uri uri;
    private StorageHandler storageHandler;
    private ProfileViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageHandler = new StorageHandler(requireContext());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());

        storageHandler = new StorageHandler(requireContext());
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.profileLoader.setOnRefreshListener(this);
        loadData();

        binding.profileImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(intent, "Choose Photo");

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

            startActivityForResult(chooserIntent, SELECT_PHOTO_PROFILE);
        });


        binding.logout.setOnClickListener(v -> {
            storageHandler.logout();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).changeMenu(false);
            }

            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_authSignupFragment);
            Navigation.findNavController(v).popBackStack(R.id.profileFragment, true);
        });

        binding.personName.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                if (!textView.getText().toString().isEmpty()) {
                    viewModel.updateName(textView.getText().toString());
                }

                return true;
            }

            return false;
        });

        binding.whiteTheme.setOnClickListener(View -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            requireActivity().recreate();

            storageHandler.setTheme(0);
        });

        binding.blackTheme.setOnClickListener(View -> {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            requireActivity().recreate();
            storageHandler.setTheme(1);
        });

        binding.greenTheme.setOnClickListener(View -> {
            storageHandler.setTheme(2);
        });

        binding.fragmentProfileButtonAddTask.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.createTaskFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (((requestCode == SELECT_PHOTO_PROFILE && resultCode == RESULT_OK)) && data != null) {
            if (data.getData() != null) {
                uri = data.getData();

                try {
                    final InputStream imageStream = requireActivity().getContentResolver().openInputStream(uri);
                    final Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);

                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = requireActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imagePath = cursor.getString(columnIndex);
                    cursor.close();

                    File file = new File(imagePath);
                    saveImage(file, originalBitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                File f = new File(requireContext().getCacheDir(), "test");
                try {
                    f.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

                    byte[] bitmapdata = bos.toByteArray();

                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    saveImage(f, bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void saveImage(File file, Bitmap originalBitmap) {
        viewModel.savePhoto(storageHandler.getToken(), file, storageHandler.getUserID()).observe(requireActivity(), statusCode -> {
            if (statusCode == 0) {
                binding.profileImageButton.setVisibility(View.GONE);
                binding.profileLoadImage.setVisibility(View.VISIBLE);
            } else if (statusCode < 400) {
                binding.profileImageButton.setImageBitmap(originalBitmap);
                binding.profileImageButton.setVisibility(View.VISIBLE);
                binding.profileLoadImage.setVisibility(View.GONE);
            } else {
                Toast.makeText(requireContext(),"Произошла ошибка" + Integer.toString(statusCode), Toast.LENGTH_SHORT).show();
                binding.profileImageButton.setVisibility(View.VISIBLE);
                binding.profileLoadImage.setVisibility(View.GONE);
            }
        });
    }

    private void loadData() {
        binding.profileLoader.setRefreshing(true);
        viewModel.getUserData(storageHandler.getToken(), storageHandler.getUserID()).observe(requireActivity(), user -> {
            if (user != null) {
                binding.personName.setText(user.getName());
                binding.personPoints.setText("Баллы: " + user.getScores());
                if (user.getImage() != null) {
                    binding.profileImageButton.setVisibility(View.VISIBLE);
                    binding.profileLoadImage.setVisibility(View.GONE);
                    String url = "https://test123-production-e08e.up.railway.app/image/" + user.getImage();
                    Picasso.get().load(url).into(binding.profileImageButton);
                }
                binding.profileLoader.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}
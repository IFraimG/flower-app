package com.example.ecoapp.data.api.auth;

import com.example.ecoapp.data.api.auth.dto.AuthLoginDTO;
import com.example.ecoapp.data.api.auth.dto.AuthResponseDTO;
import com.example.ecoapp.data.api.auth.dto.AuthSignupDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthAPI {
        @POST("/auth/users/login")
        Call<AuthResponseDTO> login(@Header("Authorization") String token, @Body AuthLoginDTO authLoginDTO);

        @POST("/auth/users/signup")
        Call<AuthResponseDTO> signup(@Header("Authorization") String token, @Body AuthSignupDTO authSignupDTO);
}
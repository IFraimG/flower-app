package com.example.ecoapp.data.api.users;

import com.example.ecoapp.Model.User;
import com.example.ecoapp.data.api.users.dto.ChangeScoresDTO;
import com.example.ecoapp.data.api.users.dto.EditProfileDTO;
import com.example.ecoapp.data.api.users.dto.HabitDTO;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserAPI {
    @PUT("/users/edit_profile")
    @Multipart
    Call<User> editProfile(@Header("Authorization") String token, @Body EditProfileDTO editProfileDTO,
                           @Part MultipartBody.Part img);

    @PUT("/users/change_scores")
    Call<User> changeScores(@Header("Authorization") String token, @Body ChangeScoresDTO changeScoresDTO);

    @GET("/users/get_user")
    Call<User> getUser(@Header("Authorization") String token, @Query("id") String id);

    @PUT("/users/add_photo")
    @Multipart
    Call<User> addPhoto(@Header("Authorization") String token, @Body String id, @Part MultipartBody.Part img);

    @PUT("/users/add_habit")
    Call<User> addHabit(@Header("Authorization") String token, @Body HabitDTO habitDTO);

    @DELETE("/users/remove_habit")
    Call<User> removeHabit(@Header("Authorization") String token, @Body HabitDTO habitDTO);

    @GET("/users/get_habit_by_title")
    Call<User> getHabitByTitle(@Header("Authorization") String token, @Body String title);
}

package com.example.ecoapp.data.api.guides;

import com.example.ecoapp.data.models.Guide;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class GuideRepository {
    private final GuideAPI guideAPI;

    public GuideRepository(GuideAPIService guideAPIService) {
        guideAPI = guideAPIService.getGuideAPI();
    }

    public Call<Guide> createGuide(String token, String title, String description, String authorID, String source, File photo) {
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), photo);
        MultipartBody.Part file = MultipartBody.Part.createFormData("img", photo.getName(), fileReqBody);

        return guideAPI.createGuide(token, new Guide(title, description, authorID, source), file);
    }

    public Call<ResponseBody> deleteGuide(String token, String guideID) {
        return guideAPI.deleteHabit(token, guideID);
    }

    public Call<Guide> getGuideByID(String token, String guideID) {
        return guideAPI.getGuideByID(token, guideID);
    }

    public Call<ArrayList<Guide>> getGuidesList(String token) {
        return guideAPI.getGuidesList(token);
    }

    public Call<ResponseBody> changeGuide(String token, String title, String description, String source, File photo) {
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), photo);
        MultipartBody.Part file = MultipartBody.Part.createFormData("img", photo.getName(), fileReqBody);

        return guideAPI.changeGuide(token, new Guide(title, description, source), file);
    }
}
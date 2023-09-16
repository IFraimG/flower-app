package com.example.ecoapp.data.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private Retrofit retrofit;
    public String BASE_URL = "https://test123-production-e08e.up.railway.app";
//    public String BASE_URL = "http://127.0.0.1:8080";

    public HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return loggingInterceptor;
    }


    public Retrofit create() {
        OkHttpClient.Builder client = new OkHttpClient.Builder().addInterceptor(getHttpLoggingInterceptor());

        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client.build())
                .build();
    }

    public Retrofit getInstance() {
        if (retrofit == null) retrofit = create();
        return retrofit;
    }
}
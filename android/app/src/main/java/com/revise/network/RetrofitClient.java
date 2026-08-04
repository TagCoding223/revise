package com.revise.network;

import com.revise.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

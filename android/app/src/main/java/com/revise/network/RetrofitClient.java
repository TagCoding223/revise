package com.revise.network;

import android.content.Context;

import com.revise.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null){
            TokenManager tokenManager = new TokenManager(context);
            TokenAuthenticator authenticator = new TokenAuthenticator(tokenManager);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(tokenManager))
                    .authenticator(authenticator) // Attach the background renewal engine
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Provide the API service back to the authenticator so it can make refresh calls
            authenticator.setAuthApiService(retrofit.create(AuthApiService.class));
        }
        return retrofit;
    }
}

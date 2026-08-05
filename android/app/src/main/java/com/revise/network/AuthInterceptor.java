package com.revise.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager){
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Skip adding the token for public routes like login or signup
        if(originalRequest.url().encodedPath().contains("/auth/")){
            return chain.proceed(originalRequest);
        }

        String accessToken = tokenManager.getAccessToken();

        // --- ADD THIS LOG --- TODO: Remove this log
        android.util.Log.d("NetworkLog", "Attaching Token to Request: " + accessToken);

        if(accessToken != null){
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization","Bearer "+accessToken)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}

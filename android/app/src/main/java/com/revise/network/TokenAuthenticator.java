package com.revise.network;

import android.content.Context;
import android.content.Intent;

import com.revise.MainActivity;
import com.revise.dto.request.RefreshRequest;
import com.revise.dto.response.TokenRefreshResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {
    private TokenManager tokenManager;
    private AuthApiService authApiService; // We will pass this in
    private Context context;

    public TokenAuthenticator(Context context, TokenManager tokenManager) {
        this.context = context.getApplicationContext(); // Use app context to prevent memory leaks
        this.tokenManager = tokenManager;
    }

    public void setAuthApiService(AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // 1. THE FIX: If the request that triggered the 401 was an auth/refresh route,
        // OR if we have already tried refreshing once (priorResponse), STOP the loop!
        if (response.request().url().encodedPath().contains("/auth/") || response.priorResponse() != null) {
            return null; // Give up and force logout
        }

        String currentRefreshToken = tokenManager.getRefreshToken();
        if (currentRefreshToken == null) {
            return null;
        }

        // Make a SYNCHRONOUS request to your backend to get a new access token
        Call<TokenRefreshResponse> refreshCall = authApiService.refreshToken(new RefreshRequest(currentRefreshToken));
        retrofit2.Response<TokenRefreshResponse> refreshResponse = refreshCall.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            String newAccessToken = refreshResponse.body().getAccessToken();
            tokenManager.saveTokens(newAccessToken, currentRefreshToken, tokenManager.getUserId());

            // Retry the original request with the brand new access token
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        } else {
            // Refresh token is expired. Wipe data and force login.
            tokenManager.clearTokens();

            // Clear profile cache
            context.getSharedPreferences("ProfileCache", Context.MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            return null;
        }
    }
}

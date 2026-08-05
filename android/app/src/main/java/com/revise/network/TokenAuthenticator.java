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
        // To prevent infinite loops if the refresh token is ALSO expired
        if (response.request().header("Authorization") != null &&
                response.request().url().encodedPath().contains("/auth/refresh")) {
            return null; // Stop trying, force the user to log in again
        }

        String currentRefreshToken = tokenManager.getRefreshToken();
        if (currentRefreshToken == null) {
            return null;
        }

        // Make a SYNCHRONOUS request to your backend to get a new access token (Request new access token synchronously)
        Call<TokenRefreshResponse> refreshCall = authApiService.refreshToken(new RefreshRequest(currentRefreshToken));
        retrofit2.Response<TokenRefreshResponse> refreshResponse = refreshCall.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            // Success: Save new token and retry the failed request
            String newAccessToken = refreshResponse.body().getAccessToken();
            tokenManager.saveTokens(newAccessToken, currentRefreshToken, tokenManager.getUserId()); // Keep old refresh token or save new one if backend rotates it

            // Retry the original request with the brand new access token
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        } else {
            // FAILURE: Refresh token is expired or invalid.

            // 1. Wipe data
            tokenManager.clearTokens();

            // 2. Force navigation back to MainActivity (which will see no tokens and load LoginFragment)
            Intent intent = new Intent(context, MainActivity.class);

            // These flags clear the entire app backstack so the user can't press "Back" to return to the dashboard
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            return null;
        }
    }
}

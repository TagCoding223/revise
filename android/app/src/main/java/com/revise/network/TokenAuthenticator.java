package com.revise.network;

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

    public TokenAuthenticator(TokenManager tokenManager) {
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

        // Make a SYNCHRONOUS request to your backend to get a new access token
        Call<TokenRefreshResponse> refreshCall = authApiService.refreshToken(new RefreshRequest(currentRefreshToken));
        retrofit2.Response<TokenRefreshResponse> refreshResponse = refreshCall.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            // Save the new tokens
            String newAccessToken = refreshResponse.body().getAccessToken();
            tokenManager.saveToken(newAccessToken, currentRefreshToken, tokenManager.getUserId()); // Keep old refresh token or save new one if backend rotates it

            // Retry the original request with the brand new access token
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        } else {
            // Refresh token expired. Wipe data and route to login screen.
            tokenManager.clearTokens();
            // TODO: Broadcast an intent or event here to force your MainActivity to redirect to LoginFragment
            return null;
        }
    }
}

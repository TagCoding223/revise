package com.revise.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String PREFS_NAME = "secure_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences sharedPreferences;

    public TokenManager(Context context){
        try{
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        }catch (GeneralSecurityException | IOException e){
            e.printStackTrace();
        }
    }

    public void saveToken(String accessToken, String refreshToken, String userId){
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .apply();

        // TODO: Remove the logs
        Log.d("AuthTokens", "Access Token: " + sharedPreferences.getString(KEY_ACCESS_TOKEN, "null"));
        Log.d("AuthTokens", "Refresh Token: " + sharedPreferences.getString(KEY_REFRESH_TOKEN, "null"));
        Log.d("AuthTokens", "User Id: " + sharedPreferences.getString(KEY_USER_ID, "null"));
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getAccessToken(){
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken(){
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearTokens(){
        sharedPreferences.edit().clear().apply();
    }


}

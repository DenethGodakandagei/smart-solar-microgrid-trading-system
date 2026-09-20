/*
 * Smart Solar Microgrid Trading System
 * ApiClient.java
 *
 * Member 2 - Native Android Prosumer Application
 * Singleton Retrofit + OkHttp client for all REST API communication.
 * Includes authentication interceptor to attach JWT token to every request.
 */
package com.smartsolar.app.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartsolar.app.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class that provides a configured Retrofit instance.
 * Attaches JWT bearer token from SharedPreferences to every outgoing request.
 * Includes logging interceptor for development debugging.
 */
public class ApiClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    // Prevent instantiation
    private ApiClient() {
    }

    /**
     * Returns the singleton ApiService instance.
     * Initializes Retrofit with OkHttp client on first call.
     *
     * @param context Application context for reading auth token from SharedPreferences.
     * @return Configured ApiService ready for making API calls.
     */
    public static synchronized ApiService getApiService(Context context) {
        if (apiService == null) {
            retrofit = buildRetrofit(context);
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Forces a rebuild of the Retrofit instance.
     * Call this after login/logout to refresh the auth interceptor.
     *
     * @param context Application context.
     * @return Fresh ApiService instance.
     */
    public static synchronized ApiService refreshApiService(Context context) {
        retrofit = buildRetrofit(context);
        apiService = retrofit.create(ApiService.class);
        return apiService;
    }

    /**
     * Builds the Retrofit instance with OkHttp client, auth interceptor,
     * logging interceptor, and timeout configuration.
     *
     * @param context Application context.
     * @return Configured Retrofit instance.
     */
    private static Retrofit buildRetrofit(Context context) {
        // Logging interceptor for development
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor to attach JWT token
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();

            // Read token from SharedPreferences
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            String token = prefs.getString(Constants.PREF_AUTH_TOKEN, "");

            // Build new request with Authorization header
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            // Only add auth header if token exists
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.method(original.method(), original.body()).build();
            return chain.proceed(request);
        };

        // Build OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

        // Build Retrofit
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Resets the singleton instances.
     * Call on logout to clear cached service references.
     */
    public static synchronized void reset() {
        retrofit = null;
        apiService = null;
    }
}

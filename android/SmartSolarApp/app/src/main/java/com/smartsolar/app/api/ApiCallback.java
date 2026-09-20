/*
 * Smart Solar Microgrid Trading System
 * ApiCallback.java
 *
 * Member 2 - Native Android Prosumer Application
 * Generic callback interface for handling API response success and failure.
 */
package com.smartsolar.app.api;

/**
 * Generic callback interface for asynchronous API responses.
 * Activities implement this to handle success data or error messages.
 *
 * @param <T> The expected response type from the API.
 */
public interface ApiCallback<T> {

    /**
     * Called when the API request succeeds and returns valid data.
     *
     * @param result The deserialized response object.
     */
    void onSuccess(T result);

    /**
     * Called when the API request fails due to network error,
     * server error, or invalid response.
     *
     * @param errorMessage A human-readable error description.
     */
    void onError(String errorMessage);
}

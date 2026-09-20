/*
 * Smart Solar Microgrid Trading System
 * ApiService.java
 *
 * Member 2 - Native Android Prosumer Application
 * Retrofit interface defining all REST API endpoints for the C# Web API.
 * Covers authentication, prosumer management, bookings, nodes, and operator operations.
 */
package com.smartsolar.app.api;

import com.smartsolar.app.api.models.BookingRequest;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.api.models.BookingSummary;
import com.smartsolar.app.api.models.DashboardStats;
import com.smartsolar.app.api.models.LoginRequest;
import com.smartsolar.app.api.models.LoginResponse;
import com.smartsolar.app.api.models.MicrogridNode;
import com.smartsolar.app.api.models.ProsumerProfile;
import com.smartsolar.app.api.models.RegisterRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Retrofit API service interface.
 * All endpoints map to the C# Web API hosted on IIS.
 * Business logic resides in the API (FAT service pattern).
 */
public interface ApiService {

    // ---------------------------------------------------------------
    // Authentication Endpoints
    // ---------------------------------------------------------------

    /**
     * Authenticates a user and returns a JWT token with role information.
     *
     * @param loginRequest Contains NIC/email and password.
     * @return LoginResponse with token, role, and basic user info.
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * Registers a new prosumer account using NIC as the primary key.
     *
     * @param registerRequest Contains NIC, name, email, phone, address, password.
     * @return ProsumerProfile of the newly created account.
     */
    @POST("auth/register")
    Call<ProsumerProfile> register(@Body RegisterRequest registerRequest);

    // ---------------------------------------------------------------
    // Prosumer Profile Endpoints
    // ---------------------------------------------------------------

    /**
     * Retrieves a prosumer's profile by their NIC.
     *
     * @param nic National Identity Card number (primary key).
     * @return ProsumerProfile with all profile details.
     */
    @GET("prosumer/{nic}")
    Call<ProsumerProfile> getProfile(@Path("nic") String nic);

    /**
     * Updates a prosumer's profile information.
     *
     * @param nic     NIC of the prosumer to update.
     * @param profile Updated profile data.
     * @return Updated ProsumerProfile.
     */
    @PUT("prosumer/{nic}")
    Call<ProsumerProfile> updateProfile(
            @Path("nic") String nic,
            @Body ProsumerProfile profile
    );

    /**
     * Requests deactivation of a prosumer account.
     * Account can only be reactivated by a Backoffice officer.
     *
     * @param nic NIC of the prosumer requesting deactivation.
     * @return Updated ProsumerProfile with deactivated status.
     */
    @POST("prosumer/{nic}/deactivate")
    Call<ProsumerProfile> deactivateAccount(@Path("nic") String nic);

    // ---------------------------------------------------------------
    // Microgrid Node Endpoints
    // ---------------------------------------------------------------

    /**
     * Retrieves all active microgrid nodes.
     *
     * @return List of MicrogridNode objects with GPS and capacity data.
     */
    @GET("nodes")
    Call<List<MicrogridNode>> getAllNodes();

    /**
     * Retrieves nearby microgrid nodes based on GPS coordinates.
     *
     * @param latitude  User's current latitude.
     * @param longitude User's current longitude.
     * @return List of nearby MicrogridNode objects.
     */
    @GET("nodes/nearby")
    Call<List<MicrogridNode>> getNearbyNodes(
            @Query("lat") double latitude,
            @Query("lng") double longitude
    );

    /**
     * Retrieves details of a specific microgrid node.
     *
     * @param nodeId The unique identifier of the node.
     * @return MicrogridNode with full details.
     */
    @GET("nodes/{id}")
    Call<MicrogridNode> getNodeById(@Path("id") String nodeId);

    // ---------------------------------------------------------------
    // Booking / Reservation Endpoints
    // ---------------------------------------------------------------

    /**
     * Creates a new energy slot booking.
     * Must be scheduled within 7 days (enforced by API).
     *
     * @param bookingRequest Contains node ID, date, time slot, energy amount.
     * @return BookingResponse with booking ID and status.
     */
    @POST("bookings")
    Call<BookingResponse> createBooking(@Body BookingRequest bookingRequest);

    /**
     * Updates an existing booking.
     * Requires at least 12 hours' notice before the slot time (enforced by API).
     *
     * @param bookingId      The ID of the booking to update.
     * @param bookingRequest Updated booking data.
     * @return BookingResponse with updated details.
     */
    @PUT("bookings/{id}")
    Call<BookingResponse> updateBooking(
            @Path("id") String bookingId,
            @Body BookingRequest bookingRequest
    );

    /**
     * Cancels an existing booking.
     * Requires at least 12 hours' notice before the slot time (enforced by API).
     *
     * @param bookingId The ID of the booking to cancel.
     * @return BookingResponse with cancelled status.
     */
    @DELETE("bookings/{id}")
    Call<BookingResponse> cancelBooking(@Path("id") String bookingId);

    /**
     * Retrieves a specific booking by its ID.
     *
     * @param bookingId The booking ID.
     * @return BookingResponse with full booking details.
     */
    @GET("bookings/{id}")
    Call<BookingResponse> getBookingById(@Path("id") String bookingId);

    /**
     * Retrieves all bookings for a specific prosumer.
     *
     * @param nic The prosumer's NIC.
     * @return List of BookingResponse objects.
     */
    @GET("bookings")
    Call<List<BookingResponse>> getBookingsByNic(@Query("nic") String nic);

    /**
     * Retrieves booking history for a prosumer (completed/cancelled bookings).
     *
     * @param nic The prosumer's NIC.
     * @return List of historical BookingResponse objects.
     */
    @GET("bookings/history")
    Call<List<BookingResponse>> getBookingHistory(@Query("nic") String nic);

    /**
     * Retrieves pending bookings for a prosumer.
     *
     * @param nic The prosumer's NIC.
     * @return List of pending BookingResponse objects.
     */
    @GET("bookings/pending")
    Call<List<BookingResponse>> getPendingBookings(@Query("nic") String nic);

    /**
     * Searches and filters bookings based on criteria.
     *
     * @param filters Map of filter parameters (nic, status, date, nodeId).
     * @return Filtered list of BookingResponse objects.
     */
    @GET("bookings/search")
    Call<List<BookingResponse>> searchBookings(@QueryMap Map<String, String> filters);

    /**
     * Retrieves dashboard statistics for a prosumer.
     *
     * @param nic The prosumer's NIC.
     * @return DashboardStats with pending and approved counts.
     */
    @GET("bookings/dashboard")
    Call<DashboardStats> getDashboardStats(@Query("nic") String nic);

    // ---------------------------------------------------------------
    // Operator Endpoints
    // ---------------------------------------------------------------

    /**
     * Verifies a scanned QR code against the server and finalizes
     * the energy transfer transaction.
     *
     * @param verificationData Map containing bookingId, prosumerNic, and qrToken.
     * @return BookingSummary with verification result.
     */
    @POST("operator/verify")
    Call<BookingSummary> verifyAndFinalizeTransaction(@Body Map<String, String> verificationData);
}

/*
 * Smart Solar Microgrid Trading System
 * BookingAdapter.java
 *
 * Member 2 - Native Android Prosumer Application
 * RecyclerView Adapter for displaying energy slot bookings with
 * status badges, timing, and action buttons (Edit, Cancel, QR).
 */
package com.smartsolar.app.booking.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.app.R;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter that binds a list of BookingResponse items to item_booking views.
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(BookingResponse booking);
        void onEditClick(BookingResponse booking);
        void onCancelClick(BookingResponse booking);
        void onQrClick(BookingResponse booking);
    }

    private final Context context;
    private final List<BookingResponse> bookings = new ArrayList<>();
    private final OnBookingClickListener listener;

    public BookingAdapter(Context context, OnBookingClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBookings(List<BookingResponse> newBookings) {
        this.bookings.clear();
        if (newBookings != null) {
            this.bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    public List<BookingResponse> getBookings() {
        return bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingResponse booking = bookings.get(position);
        holder.bind(booking, context, listener);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemNodeName;
        private final TextView tvItemStatusBadge;
        private final TextView tvItemBookingId;
        private final TextView tvItemEnergy;
        private final TextView tvItemDateTime;
        private final View itemDivider;
        private final View layoutItemActions;
        private final MaterialButton btnItemEdit;
        private final MaterialButton btnItemCancel;
        private final MaterialButton btnItemQr;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemNodeName = itemView.findViewById(R.id.tvItemNodeName);
            tvItemStatusBadge = itemView.findViewById(R.id.tvItemStatusBadge);
            tvItemBookingId = itemView.findViewById(R.id.tvItemBookingId);
            tvItemEnergy = itemView.findViewById(R.id.tvItemEnergy);
            tvItemDateTime = itemView.findViewById(R.id.tvItemDateTime);
            itemDivider = itemView.findViewById(R.id.itemDivider);
            layoutItemActions = itemView.findViewById(R.id.layoutItemActions);
            btnItemEdit = itemView.findViewById(R.id.btnItemEdit);
            btnItemCancel = itemView.findViewById(R.id.btnItemCancel);
            btnItemQr = itemView.findViewById(R.id.btnItemQr);
        }

        public void bind(BookingResponse booking, Context context, OnBookingClickListener listener) {
            // Node Name
            String nodeName = booking.getNodeName();
            if (nodeName == null || nodeName.isEmpty()) {
                nodeName = "Grid Node (" + (booking.getNodeId() != null ? booking.getNodeId() : "N/A") + ")";
            }
            tvItemNodeName.setText(nodeName);

            // Booking ID
            tvItemBookingId.setText("ID: " + (booking.getBookingId() != null ? booking.getBookingId() : "N/A"));

            // Energy kWh
            tvItemEnergy.setText("⚡ " + booking.getEnergyKwh() + " kWh");

            // Date & Time
            String dateDisplay = booking.getSlotDate() != null ? booking.getSlotDate() : "";
            Date parsedDate = DateTimeUtils.parseApiDate(booking.getSlotDate());
            if (parsedDate != null) {
                dateDisplay = DateTimeUtils.formatDisplayDate(parsedDate);
            }
            String timeDisplay = booking.getSlotTime() != null ? booking.getSlotTime() : "";
            tvItemDateTime.setText("📅 " + dateDisplay + " • " + timeDisplay);

            // Status Badge
            String status = booking.getStatus() != null ? booking.getStatus() : Constants.STATUS_PENDING;
            tvItemStatusBadge.setText(status);
            applyStatusBadgeStyle(status, context);

            // Action button visibility
            boolean isPendingOrApproved = Constants.STATUS_PENDING.equalsIgnoreCase(status)
                    || Constants.STATUS_APPROVED.equalsIgnoreCase(status);
            boolean isCancelled = Constants.STATUS_CANCELLED.equalsIgnoreCase(status);

            if (isCancelled) {
                layoutItemActions.setVisibility(View.GONE);
                itemDivider.setVisibility(View.GONE);
            } else if (isPendingOrApproved) {
                layoutItemActions.setVisibility(View.VISIBLE);
                itemDivider.setVisibility(View.VISIBLE);
                btnItemEdit.setVisibility(View.VISIBLE);
                btnItemCancel.setVisibility(View.VISIBLE);
                btnItemQr.setVisibility(View.VISIBLE);
            } else {
                // Completed
                layoutItemActions.setVisibility(View.VISIBLE);
                itemDivider.setVisibility(View.VISIBLE);
                btnItemEdit.setVisibility(View.GONE);
                btnItemCancel.setVisibility(View.GONE);
                btnItemQr.setVisibility(View.VISIBLE);
            }

            // Click Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });

            btnItemEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(booking);
                }
            });

            btnItemCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(booking);
                }
            });

            btnItemQr.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQrClick(booking);
                }
            });
        }

        private void applyStatusBadgeStyle(String status, Context context) {
            int textColor;
            int bgColor;

            if (Constants.STATUS_APPROVED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.badge_approved);
                bgColor = ContextCompat.getColor(context, R.color.status_success_light);
            } else if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.badge_cancelled);
                bgColor = ContextCompat.getColor(context, R.color.status_error_light);
            } else if (Constants.STATUS_COMPLETED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.badge_completed);
                bgColor = ContextCompat.getColor(context, R.color.status_info_light);
            } else {
                textColor = ContextCompat.getColor(context, R.color.badge_pending);
                bgColor = ContextCompat.getColor(context, R.color.status_pending_light);
            }

            tvItemStatusBadge.setTextColor(textColor);
            tvItemStatusBadge.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        }
    }
}

/*
 * Smart Solar Microgrid Trading System
 * NearbyNodesMapActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Google Maps integration displaying nearby active microgrid nodes
 * with interactive markers, search & filter bar, capacity details, and direct booking actions.
 */
package com.smartsolar.app.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.MicrogridNode;
import com.smartsolar.app.booking.CreateBookingActivity;
import com.smartsolar.app.db.NodeCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Interactive map screen plotting microgrid hub locations with search and filtering.
 */
public class NearbyNodesMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private ImageButton btnMapBack;
    private ImageButton btnRefreshNodes;
    private TextInputEditText etSearchMapNodes;
    private ChipGroup chipGroupMapFilter;
    private FloatingActionButton fabMyLocation;

    private MaterialCardView cardNodeInfo;
    private TextView tvSelectedNodeName;
    private TextView tvSelectedNodeStatus;
    private TextView tvSelectedNodeLocation;
    private TextView tvSelectedNodeCapacity;
    private TextView tvSelectedNodeSlots;
    private MaterialButton btnBookThisNode;

    private NodeCacheDao nodeCacheDao;
    private List<MicrogridNode> allNodesList = new ArrayList<>();
    private final Map<Marker, MicrogridNode> markerNodeMap = new HashMap<>();
    private MicrogridNode selectedNode;

    // Default center location (Colombo, Sri Lanka)
    private static final LatLng DEFAULT_LOCATION = new LatLng(6.9271, 79.8612);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_nodes_map);

        nodeCacheDao = SmartSolarApplication.getInstance().getNodeCacheDao();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupListeners();
        setupMap();
    }

    private void initViews() {
        btnMapBack = findViewById(R.id.btnMapBack);
        btnRefreshNodes = findViewById(R.id.btnRefreshNodes);
        etSearchMapNodes = findViewById(R.id.etSearchMapNodes);
        chipGroupMapFilter = findViewById(R.id.chipGroupMapFilter);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        cardNodeInfo = findViewById(R.id.cardNodeInfo);
        tvSelectedNodeName = findViewById(R.id.tvSelectedNodeName);
        tvSelectedNodeStatus = findViewById(R.id.tvSelectedNodeStatus);
        tvSelectedNodeLocation = findViewById(R.id.tvSelectedNodeLocation);
        tvSelectedNodeCapacity = findViewById(R.id.tvSelectedNodeCapacity);
        tvSelectedNodeSlots = findViewById(R.id.tvSelectedNodeSlots);
        btnBookThisNode = findViewById(R.id.btnBookThisNode);
    }

    private void setupListeners() {
        btnMapBack.setOnClickListener(v -> finish());
        btnRefreshNodes.setOnClickListener(v -> loadGridNodes());
        fabMyLocation.setOnClickListener(v -> moveToUserLocation());

        btnBookThisNode.setOnClickListener(v -> {
            if (selectedNode != null) {
                Intent bookIntent = new Intent(this, CreateBookingActivity.class);
                bookIntent.putExtra(Constants.EXTRA_NODE_ID, selectedNode.getNodeId());
                startActivity(bookIntent);
            }
        });

        // Search text watcher
        etSearchMapNodes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips listener
        chipGroupMapFilter.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(latLng -> cardNodeInfo.setVisibility(View.GONE));

        checkLocationPermissionAndEnableMyLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12f));

        loadGridNodes();
    }

    private void checkLocationPermissionAndEnableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
            moveToUserLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.REQUEST_LOCATION_PERMISSION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndEnableMyLocation();
            }
        }
    }

    private void moveToUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && googleMap != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));
            }
        });
    }

    private void loadGridNodes() {
        // Load cached nodes first for instant offline rendering
        if (nodeCacheDao != null) {
            List<MicrogridNode> cached = nodeCacheDao.getAllNodes();
            if (!cached.isEmpty()) {
                allNodesList = new ArrayList<>(cached);
                applyFilter();
            }
        }

        // Fetch fresh nodes from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getAllNodes().enqueue(new Callback<List<MicrogridNode>>() {
                @Override
                public void onResponse(@NonNull Call<List<MicrogridNode>> call,
                                       @NonNull Response<List<MicrogridNode>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        allNodesList = response.body();
                        if (nodeCacheDao != null) {
                            nodeCacheDao.insertNodes(allNodesList);
                        }
                        applyFilter();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<MicrogridNode>> call, @NonNull Throwable t) {
                    Toast.makeText(NearbyNodesMapActivity.this, "Loaded nodes from offline cache", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyFilter() {
        if (allNodesList == null || allNodesList.isEmpty()) return;

        String query = etSearchMapNodes.getText() != null ? etSearchMapNodes.getText().toString().trim().toLowerCase() : "";
        int checkedChipId = chipGroupMapFilter.getCheckedChipId();

        List<MicrogridNode> filtered = new ArrayList<>();
        for (MicrogridNode node : allNodesList) {
            // Text search match
            boolean matchesQuery = query.isEmpty()
                    || (node.getNodeName() != null && node.getNodeName().toLowerCase().contains(query))
                    || (node.getLocation() != null && node.getLocation().toLowerCase().contains(query));

            if (!matchesQuery) continue;

            // Chip category filter
            if (checkedChipId == R.id.chipFilterAvailableSlots) {
                if (node.getAvailableSlots() <= 0) continue;
            } else if (checkedChipId == R.id.chipFilterHighCapacity) {
                if (node.getCapacityKwh() < 200.0) continue;
            }

            filtered.add(node);
        }

        plotNodesOnMap(filtered);

        // If exact single match found during typing, move camera there
        if (filtered.size() == 1 && !query.isEmpty()) {
            MicrogridNode single = filtered.get(0);
            if (single.getLatitude() != 0.0 && single.getLongitude() != 0.0 && googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(single.getLatitude(), single.getLongitude()), 15f));
                showNodeCard(single);
            }
        }
    }

    private void plotNodesOnMap(List<MicrogridNode> nodes) {
        if (googleMap == null || nodes == null) return;

        googleMap.clear();
        markerNodeMap.clear();

        for (MicrogridNode node : nodes) {
            if (node.getLatitude() != 0.0 && node.getLongitude() != 0.0) {
                LatLng position = new LatLng(node.getLatitude(), node.getLongitude());
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(node.getNodeName())
                        .snippet("Capacity: " + node.getCapacityKwh() + " kWh • Available Slots: " + node.getAvailableSlots())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                node.getAvailableSlots() > 0 ? BitmapDescriptorFactory.HUE_ORANGE : BitmapDescriptorFactory.HUE_RED
                        ))
                );

                if (marker != null) {
                    markerNodeMap.put(marker, node);
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        MicrogridNode node = markerNodeMap.get(marker);
        if (node != null) {
            selectedNode = node;
            showNodeCard(node);
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(node.getLatitude(), node.getLongitude()), 14f));
            }
        }
        return false;
    }

    private void showNodeCard(MicrogridNode node) {
        tvSelectedNodeName.setText(node.getNodeName());
        tvSelectedNodeStatus.setText(node.getStatus() != null ? node.getStatus() : "Active");
        tvSelectedNodeLocation.setText(node.getLocation());
        tvSelectedNodeCapacity.setText("⚡ Capacity: " + node.getCapacityKwh() + " kWh");
        tvSelectedNodeSlots.setText("🔋 " + node.getAvailableSlots() + " Available Slots");

        cardNodeInfo.setVisibility(View.VISIBLE);
    }
}

package com.fitapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log; // <-- EKLENDİ
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsFragment"; // <-- EKLENDİ
    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SupportMapFragment mapFragment;
    private Button findGymButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        findGymButton = rootView.findViewById(R.id.findGymButton);
        findGymButton.setOnClickListener(this::findGym);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Log.d(TAG, "onCreateView: Harita fragmenti yüklendi.");
        return rootView;
    }

    private boolean checkPermission() {
        boolean granted = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkPermission: " + granted);
        return granted;
    }

    private void requestPermission() {
        Log.d(TAG, "requestPermission: Konum izni isteniyor.");
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                FINE_PERMISSION_CODE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Harita hazır.");
        myMap = googleMap;
        if (checkPermission()) {
            getCurrentLocation();
        } else {
            requestPermission();
        }
    }

    public void findGym(View view) {
        if (currentLocation != null) {
            Log.d(TAG, "findGym: currentLocation not null.");
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                    "&radius=1000" +
                    "&type=gym" +
                    "&key=" + getString(R.string.MAPS_API_KEY);

            Log.d(TAG, "findGym: URL -> " + url);

            Object[] dataFetch = new Object[2];

            Log.d(TAG, "data fetch: URL -> " + dataFetch);
            dataFetch[0] = myMap;
            dataFetch[1] = url;

            FetchData fetchData = new FetchData();
            fetchData.execute(dataFetch);
        } else {
            Log.d(TAG, "findGym: currentLocation null.");
            Toast.makeText(requireContext(), "Konum bilgisi alınamadı, lütfen konum servisini açın.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getCurrentLocation: Konum izni verilmemiş.");
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                Log.d(TAG, "getCurrentLocation: Konum alındı: " + location.getLatitude() + ", " + location.getLongitude());

                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                myMap.addMarker(new MarkerOptions().position(currentLatLng).title("Mevcut Konum"));
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
            } else {
                Log.d(TAG, "getCurrentLocation: Konum null, GPS açık mı?");
                Toast.makeText(requireContext(), "Konum alınamadı, lütfen GPS'i etkinleştirin.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: İzin verildi.");
                getCurrentLocation();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: İzin reddedildi.");
                Toast.makeText(requireContext(), "İzin reddedildi. Lütfen konum erişimine izin verin.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

public class NavigateLocation extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private Toolbar toolbar;
    private MapView mapView;

    private MapboxMap mapboxMap;

    private PermissionsManager permissionsManager;

    private LatLng location;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        setContentView(R.layout.activity_navigate_location);

        mapView = findViewById(R.id.navigation_mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        toolbar = findViewById(R.id.navigation_toolbar);

        username = getIntent().getExtras().getString("USERNAME");

        double sentLat = getIntent().getExtras().getDouble("LAT");
        double sentLon = getIntent().getExtras().getDouble("LON");
        location = new LatLng(sentLat,sentLon);

        //setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Directions to " + username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Please we need it.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "Please we need it.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private LatLng myLocation;

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                try{
                    enableLocationComponent(style);

                    myLocation = new LatLng(NavigateLocation.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                            NavigateLocation.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

                    NavigateLocation.this.mapboxMap.setMinZoomPreference(100);
                    NavigateLocation.this.mapboxMap.setMaxZoomPreference(15);

                    Point selfLocation = Point.fromLngLat(myLocation.getLongitude(),myLocation.getLatitude());
                    Point destination = Point.fromLngLat(location.getLongitude(),location.getLatitude());

                    GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                    if(source != null){
                        source.setGeoJson(Feature.fromGeometry(destination));
                    }

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .zoom(20)
                            .target(myLocation)
                            .build();

                    mapboxMap.setCameraPosition(cameraPosition);

                    getRoute(selfLocation,destination);

                    mapboxMap.addMarker(new MarkerOptions()
                            .setPosition(NavigateLocation.this.location)
                    .setTitle(username));


                }catch(Exception ex){
                    new Alert(NavigateLocation.this).showErrorMessage("Notification",ex.getMessage());
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private DirectionsRoute currentRoute;

    private void getRoute(Point origin, Point destination){
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            //No found routes
                            return;
                        }else if(response.body().routes().size() < 1){
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        NavigationMapRoute navigationRoute = new NavigationMapRoute(null,mapView,mapboxMap,R.style.NavigationMapRoute);
                        navigationRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.d("Error",t.getMessage());
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private CircleImageView civProfileImage;
    private TextView txtvProfileMainText;
    private TextView txtvProfileSubText;

    private AutoCompleteTextView etxtMainServiceSearch;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    private Alert alert;

    private User currentUser;

    private ArrayList<User> workerLists;
    private ArrayList<String> workerSuggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation_view_main);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.nav_profile:
                        Intent i = new Intent(getApplicationContext(),SelfProfile.class);
                        startActivity(i);
                        break;

                    case R.id.nav_messages:
                        Intent intent2 = new Intent(getApplicationContext(),ConversationListActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_worker_setup:
                        startActivity(new Intent(getApplicationContext(),WorkerSetup.class));
                        break;

                    case R.id.nav_logout:
                        auth.signOut();
                        Intent intent = new Intent(getApplicationContext(),Login.class);
                        startActivity(intent);
                        finish();
                        break;


                }

                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_sub_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_sub_menu_search:
                //Create Dialog
                searchPeople();
                break;
        }

        return true;
    }

    private void searchPeople(){
        //Find all the users that has etxts data
        String data = etxtMainServiceSearch.getText().toString().trim();
        final ArrayList<User> workersWithSearchedService = new ArrayList<>();

        for(int i = 0; i<workerLists.size(); i++){
            User user = workerLists.get(i);
            if(user.getWorkerProfile().getMainService().equals(data)){
                workersWithSearchedService.add(user);
            }
        }

        String[] array = new String[workersWithSearchedService.size()];
        for(int i = 0; i<workersWithSearchedService.size(); i++){
            array[i] = workersWithSearchedService.get(i).getFirstName() + " " + workersWithSearchedService.get(i).getLastName();
        }

        //Create Dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Search Results")
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User chosenWorker = workersWithSearchedService.get(which);
                        viewPeople(chosenWorker);

                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(chosenWorker.getWorkerProfile().getUserLocation().getLat(),chosenWorker.getWorkerProfile().getUserLocation().getLng())) // Sets the new camera position
                                .zoom(15) // Sets the zoom
                                .bearing(100) // Rotate the camera
                                .build(); // Creates a CameraPosition from the builder

                        MainActivity.this.mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),7000);

                        etxtMainServiceSearch.setText("");
                        dialog.dismiss();
                    }
                })
                .create();
        //Show Dialog
        dialog.show();
    }

    private void viewPeople(User user){
        //Fetch the view
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_profile_viewer,null,false);

        //Get the components
        final CircleImageView civ = view.findViewById(R.id.dialog_profile_viewer_civ);
        TextView mainText = view.findViewById(R.id.dialog_profile_viewer_main_text);
        TextView subText = view.findViewById(R.id.dialog_profile_viewer_sub_text);
        TextView mainServiceText = view.findViewById(R.id.dialog_profile_viewer_main_service);
        Button btnViewProfile = view.findViewById(R.id.dialog_profile_viewer_view_profile);

        final User worker = user;

        //Find the User Photo
        final File file;
        try {
            file = File.createTempFile("image","png");
            StorageReference reference = storage.getReference().child(user.getProfileImagePath());
            reference.getFile(file)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Uri uri = Uri.fromFile(file);
                                civ.setImageURI(uri);
                            }else{
                                task.getException().printStackTrace();
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Set data to the view
        mainText.setText(user.getFirstName() + " " + user.getLastName());
        subText.setText(user.getEmail());
        mainServiceText.setText(user.getWorkerProfile().getMainService());
        btnViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Direct to Users Profile
                Intent intent = new Intent(MainActivity.this,ProfielActivity.class);
                intent.putExtra("USER_ID",worker.getUid());
                startActivity(intent);
            }
        });

        //Create dialog to show User Data
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(view)
                .create();

        //Show dialog box
        dialog.show();
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

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                //Find the users profile in the lists
                User user = null;

                for(int i = 0; i<workerLists.size(); i++){
                    if(marker.getTitle().equals(workerLists.get(i).getUid())){
                        user = workerLists.get(i);
                        break;
                    }
                }

                viewPeople(user);

                return true;
            }
        });

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                try{
                    enableLocationComponent(style);

                    if(MainActivity.this.mapboxMap.getLocationComponent() != null){
                        LatLng location = new LatLng(MainActivity.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                                MainActivity.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

                        MainActivity.this.mapboxMap.setMinZoomPreference(100);
                        MainActivity.this.mapboxMap.setMaxZoomPreference(15);

                        CameraPosition position = new CameraPosition.Builder()
                                .target(location) // Sets the new camera position
                                .zoom(15) // Sets the zoom
                                .bearing(100) // Rotate the camera
                                .build(); // Creates a CameraPosition from the builder

                        MainActivity.this.mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),7000);

                        refreshMap();
                    }else{
                        throw new Exception("Cannot find location, please turn on your Location Services or GPS");
                    }


                }catch(Exception ex){
                    alert.showErrorMessage("Notification",ex.getMessage());
                }

            }
        });
    }

    private void init(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        if(user == null){
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }

        View headerView = navigationView.getHeaderView(0);
        civProfileImage = headerView.findViewById(R.id.navHeader_civ_profile);
        txtvProfileMainText = headerView.findViewById(R.id.navHeader_txtv_profile_main_text);
        txtvProfileSubText = headerView.findViewById(R.id.navHeader_txtv_profile_sub_text);

        MainProfileSetup setup = new MainProfileSetup(MainActivity.this);
        setup.execute();

        alert = new Alert(this);

        etxtMainServiceSearch = findViewById(R.id.etxt_main_search_services);
    }

    public void refreshButton(View view){
        refreshMap();
    }

    private LatLng myLocation;

    private void refreshMap(){
        try{
            //Refreshes all the workers
            workerLists = new ArrayList<>();
            workerSuggestion = new ArrayList<>();


            //Remove all markers if exists
            for(Marker m : mapboxMap.getMarkers()){
                mapboxMap.removeMarker(m);
            }

            LatLng location = new LatLng(MainActivity.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                    MainActivity.this.mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

            myLocation = location;

            MainActivity.this.mapboxMap.setMinZoomPreference(15);
            MainActivity.this.mapboxMap.setMaxZoomPreference(20);

            CameraPosition position;

            if(location != null){
                position = new CameraPosition.Builder()
                        .target(location) // Sets the new camera position
                        .zoom(15) // Sets the zoom
                        .bearing(12) // Rotate the camera
                        .build(); // Creates a CameraPosition from the builder
            }else{
                position = new CameraPosition.Builder()
                        .zoom(15) // Sets the zoom
                        .bearing(12) // Rotate the camera
                        .build(); // Creates a CameraPosition from the builder
            }

            MainActivity.this.mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),7000);

            database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        //Identify Workers
                        if(user.isWorkerMode() && !user.getUid().equals(currentUser.getUid())){

                            if(myLocation != null){
                                if(myLocation.getLatitude() + 0.0100 > user.getWorkerProfile().getUserLocation().getLat()
                                && myLocation.getLatitude() - 0.0100 < user.getWorkerProfile().getUserLocation().getLat()
                                && myLocation.getLongitude() + 0.0100 > user.getWorkerProfile().getUserLocation().getLng()
                                && myLocation.getLongitude() - 0.0100 < user.getWorkerProfile().getUserLocation().getLng()){
                                    //Add to the list of worker
                                    workerLists.add(user);

                                    //Add Worker's Service on the search list
                                    workerSuggestion.add(user.getWorkerProfile().getMainService());

                                    //Get the Users postion
                                    User.Location workerLocation = user.getWorkerProfile().getUserLocation();

                                    //Create an icon for the map.
                                    mapboxMap.addMarker(new MarkerOptions()
                                            .setTitle(user.getUid())
                                            .setPosition(new LatLng(workerLocation.getLat(),workerLocation.getLng())));
                                }
                            }else{
                                //Add to the list of worker
                                workerLists.add(user);

                                //Add Worker's Service on the search list
                                workerSuggestion.add(user.getWorkerProfile().getMainService());

                                //Get the Users postion
                                User.Location workerLocation = user.getWorkerProfile().getUserLocation();

                                //Create an icon for the map.
                                mapboxMap.addMarker(new MarkerOptions()
                                        .setTitle(user.getUid())
                                        .setPosition(new LatLng(workerLocation.getLat(),workerLocation.getLng())));
                            }
                        }

                    }
                    etxtMainServiceSearch.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,workerSuggestion));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        }catch(Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    private class MainProfileSetup extends AsyncTask<Void,Void,String>{
        private ProgressDialog pg;
        private Context context;

        public MainProfileSetup(Context context) {
            super();

            this.context = context;

            pg = new ProgressDialog(context);
            pg.setMessage("Loading...");
            pg.setIndeterminate(true);
            pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pg.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                database.getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        currentUser = user;

                        txtvProfileMainText.setText(user.getFirstName() + " " + user.getLastName());
                        txtvProfileSubText.setText(user.getEmail());

                        final File file;
                        try {
                            file = File.createTempFile("image","png");
                            StorageReference reference = storage.getReference().child(user.getProfileImagePath());
                            reference.getFile(file)
                                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                Uri uri = Uri.fromFile(file);
                                                civProfileImage.setImageURI(uri);
                                            }else{
                                                task.getException().printStackTrace();
                                            }
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.toException().printStackTrace();
                    }
                });
            }catch (Exception ex){
                pg.dismiss();
                new Alert(context).showErrorMessage("Notification",ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pg.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
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

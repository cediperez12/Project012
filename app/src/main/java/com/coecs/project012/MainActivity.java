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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private CircleImageView civProfileImage;
    private TextView txtvProfileMainText;
    private TextView txtvProfileSubText;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation_view_main);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

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

        MainProfileSetup setup = new MainProfileSetup(this);
        setup.execute();
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

                                            pg.dismiss();
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.toException().printStackTrace();
                        pg.dismiss();
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
            if(pg.isShowing())
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

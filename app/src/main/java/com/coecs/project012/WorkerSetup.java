package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WorkerSetup extends AppCompatActivity implements LocationListener {

    private DatabaseReference userDr;
    private FirebaseAuth mAuth;
    private DatabaseReference referenceForServices;

    private User currentUser;

    private Toolbar toolbar;

    private TextInputLayout tilServices;
    private ChipGroup cgSkills, cgOtherServices;
    private LinearLayout lvExperience, lvEducationAttainment;
    private CircleImageView civOtherServices, civEduc, civExperience, civSkills;
    private Switch swWorkMode;
    private CheckBox cbTermsAgreement;
    private TextView worker_setup_save_location_notfier_textv;
    private AutoCompleteTextView etxt_main_service;

    private ArrayList<String> listSkills, listOtherServices; //Holders.
    private ArrayAdapter<String> listEducation, listExperice;

    private Alert alert;

    private ProgressDialog progressDialog;

    private LocationManager locationManager;
    private Location location;

    private boolean checkGps = false;
    private boolean checkInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_setup);
        init();
    }

    private void init() {
        toolbar = findViewById(R.id.worker_setup_toolbar);

        //Toolbar Setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Worker Setup");

        tilServices = findViewById(R.id.til_services);
        etxt_main_service = findViewById(R.id.etxt_main_service);
        cgSkills = findViewById(R.id.chip_group_skills);
        cgOtherServices = findViewById(R.id.chip_group_other_services);

        lvExperience = findViewById(R.id.listv_experiences);
        lvEducationAttainment = findViewById(R.id.listv_educational_attainment);

        swWorkMode = findViewById(R.id.sw_worker_mode);
        cbTermsAgreement = findViewById(R.id.checkBox_terms_agreement);
        worker_setup_save_location_notfier_textv = findViewById(R.id.worker_setup_save_location_notfier_textv);

        //Setup Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        //Setup Firebase
        mAuth = FirebaseAuth.getInstance();
        userDr = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        referenceForServices = FirebaseDatabase.getInstance().getReference("services");

        //Setup Services
        referenceForServices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> data = new ArrayList<>();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    data.add(ds.getValue(String.class));
                }

                etxt_main_service.setAdapter(new ArrayAdapter<String>(WorkerSetup.this,android.R.layout.simple_list_item_1,data));
                etxt_main_service.clearFocus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Show Progress Dialog
        progressDialog.show();

        //Setup User Data
        userDr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);

                if (currentUser.getWorkerProfile() == null) {
                    currentUser.setWorkerProfile(new User.WorkerProfile());
                    currentUser.getWorkerProfile().setMainService("");
                }

                //Load Skill List
                LoadSkills();

                //Load Other Service List
                LoadServices();

                //Load Education List
                LoadEducList();

                //Load Experiences
                LoadExperienceList();

                tilServices.getEditText().setText(currentUser.getWorkerProfile().getMainService());
                swWorkMode.setChecked(currentUser.isWorkerMode());
                cbTermsAgreement.setChecked(currentUser.getWorkerProfile().isTermsAndAgreement());

                //Dismiss Progress Dialog
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();

                //Dismiss Progress Dialog
                progressDialog.dismiss();
            }
        });

        //setup alert
        alert = new Alert(this);

        swWorkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Fetch Location
                    try {//Find Restrictions
                        //Create Dialog for confirmation
                        AlertDialog confirmationDialog = new AlertDialog.Builder(WorkerSetup.this)
                                .setTitle("Notification")
                                .setMessage("This act will give save your location and will be seen by other user for them to contact you. Are you sure you want to save your location?")
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        swWorkMode.setChecked(false);
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try{
                                            if(ActivityCompat.checkSelfPermission(WorkerSetup.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                                ActivityCompat.requestPermissions(WorkerSetup.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
                                            }else{
                                                LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

                                                boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                                                boolean gpsConnection = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                                                if(network){
                                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,WorkerSetup.this);

                                                    if(locationManager != null){
                                                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                                        if(location != null){
                                                            double lat = location.getLatitude(), lon = location.getLongitude();
                                                            currentUser.getWorkerProfile().setUserLocation(new User.Location(lat,lon));
                                                        }else{
                                                            throw new Exception("Failed to fetch your location. Please try again later.");
                                                        }
                                                    }
                                                }else if(gpsConnection){
                                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,WorkerSetup.this);

                                                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    if(location != null){
                                                        double lat = location.getLatitude(), lon = location.getLongitude();
                                                        currentUser.getWorkerProfile().setUserLocation(new User.Location(lat,lon));
                                                    }else{
                                                        throw new Exception("Failed to fetch your location. Please try again later.");
                                                    }
                                                }else{
                                                    throw new Exception("Please turn on your GPS/Location and Internet Connection");
                                                }
                                            }
                                        }catch (Exception ex){
                                            swWorkMode.setChecked(false);
                                            alert.showErrorMessage("Notification",ex.getMessage());
                                        }
                                    }
                                })
                                .create();

                        //Show dialog
                        confirmationDialog.show();
                    }catch (Exception ex){
                        swWorkMode.setChecked(false);
                        alert.showErrorMessage("Notification",ex.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                    swWorkMode.setChecked(false);
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //Chips
    public void addNewService(View view){
        View newServiceView = LayoutInflater.from(this).inflate(R.layout.add_new_entry_other_service,null,false);

        //Dialog Components
        final EditText etxt_entry_other_service = newServiceView.findViewById(R.id.etxt_entry_other_service);

        //Create Alert Dialog
        AlertDialog newServiceDialog = new AlertDialog.Builder(this)
                .setTitle("New Service")
                .setView(newServiceView)
                .setNegativeButton(getString(android.R.string.cancel),null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strOtherService = etxt_entry_other_service.getText().toString().trim();

                        try{
                            //Restrictions
                            if(strOtherService.isEmpty())
                                throw new Exception("Please put something as your service");

                            if(currentUser.getWorkerProfile().getOtherService().contains(strOtherService))
                                throw new Exception("You have that service in your list.");
                            //End of Restrictions

                            //Add new Service in the list
                            currentUser.getWorkerProfile().getOtherService().add(strOtherService);

                            //Load Services List
                            LoadServices();

                        }catch (Exception ex){
                            alert.showErrorMessage("Notification",ex.getMessage());
                        }
                    }
                })
                .create();

        //Show the Dialog;
        newServiceDialog.show();
    }

    private void LoadServices(){
        //Remove all Views inside the Chip Group
        cgOtherServices.removeAllViews();

        //Check if there is a list.
        if(currentUser.getWorkerProfile().getOtherService() == null){
            currentUser.getWorkerProfile().setOtherService(new ArrayList<String>());
        }

        //Fetch the list.
        List<String> servicesList = currentUser.getWorkerProfile().getOtherService();

        //Add restrictions
        try{
            //Restriction
            if(servicesList.isEmpty())
                throw new Exception("Your list is empty.");
            //End of Restriction

            //Create a loop to get each service
            for(int i = 0; i<servicesList.size(); i++){

                //Get the string
                final String service = servicesList.get(i);

                //Get the index;
                final int position = i;

                //Get a chip.
                Chip chip = getChip(cgOtherServices, service, i, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog deleteDialog = new AlertDialog.Builder(WorkerSetup.this)
                                .setTitle("Notification")
                                .setMessage("Are you sure you want to delete " + service + "?")
                                .setNegativeButton(getString(android.R.string.no),null)
                                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Remove the selected Service
                                        currentUser.getWorkerProfile().getOtherService().remove(position);
                                        LoadServices();
                                    }
                                })
                                .create();
                        deleteDialog.show();
                    }
                });

                //Add the chip;
                cgOtherServices.addView(chip);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void addNewSkill(View view){
        //Find the view
        final View entryView = getLayoutInflater().inflate(R.layout.add_new_entry_other_service,null,false);

        //Get the component in the view
        final EditText etxtEntry = entryView.findViewById(R.id.etxt_entry_other_service);

        //Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add new Skill")
                .setView(entryView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Restrictions
                        try{
                            //Fetch the data
                            String data = etxtEntry.getText().toString().trim();

                            //Restrictions
                            if(data.isEmpty()){
                                throw new Exception("Please put any data inside");
                            }else if(currentUser.getWorkerProfile().getSkills().contains(data)){
                                throw new Exception("The Skill is already on the lists.");
                            }

                            //Add the skill to the lists
                            currentUser.getWorkerProfile().getSkills().add(data);

                            //Load the Skills
                            LoadSkills();
                        }catch (Exception ex){
                            alert.showErrorMessage("Notification",ex.getMessage());
                        }

                    }
                })
                .create();

        //Show Dialog
        dialog.show();
    }

    private void LoadSkills(){
        //Remove all Views
        cgSkills.removeAllViews();

        //Check the list if it exists
        if(currentUser.getWorkerProfile().getSkills() == null)
            currentUser.getWorkerProfile().setSkills(new ArrayList<String>());

        //Create a temporary list
        final List<String> listSkills = currentUser.getWorkerProfile().getSkills();

        //Create a loop
        try{//Find exceptions
            for(int i = 0; i<listSkills.size(); i++){

                //Fetch single data
                final String data = listSkills.get(i);

                //Fetch current Position
                final int position = i;

                //Get Chip
                Chip chip = getChip(cgSkills, data, position, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Create delete dialog
                        AlertDialog deleteDialog = new AlertDialog.Builder(WorkerSetup.this)
                                .setTitle("Notification")
                                .setMessage("Are you sure you want to delete " + data + "?")
                                .setNegativeButton("No",null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        currentUser.getWorkerProfile().getSkills().remove(position);
                                        LoadSkills();
                                    }
                                })
                                .create();

                        //Show delete Dialog
                        deleteDialog.show();
                    }
                });

                cgSkills.addView(chip);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //ListViews
    public void addNewEducationalAttainment(View v){
        //Find the View
        final View dialogView = getLayoutInflater().inflate(R.layout.add_new_entry_education_attainment,null,false);

        //Setup View Components
        final EditText etxtSchool = dialogView.findViewById(R.id.etxt_new_entry_school);
        final EditText etxtCourse = dialogView.findViewById(R.id.etxt_new_entry_course);
        final Spinner yearFrom = dialogView.findViewById(R.id.spinner_new_entry_educational_attainment_year_from);
        final Spinner yearTo = dialogView.findViewById(R.id.spinner_new_entry_educational_attainment_year_to);

        //Setup DateSpinners
        final int to = Calendar.getInstance().get(Calendar.YEAR);

        //Create a list
        final ArrayAdapter<Integer> yearsListFrom = new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line);

        //Loop to create a list of years;
        for(int i = to-50; i<to; i++){
            yearsListFrom.add(i);
        }

        yearFrom.setAdapter(yearsListFrom);
        yearFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Create a List to handle years to;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(WorkerSetup.this,android.R.layout.simple_dropdown_item_1line);

                //Get the last year from FROMSPINNER
                int from = yearsListFrom.getItem(position);

                //Create loop until the current year;
                for(int i = from; i<to; i++){
                    adapter.add(Integer.toString(i));
                }

                adapter.add("Current");

                //Set adapter
                yearTo.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Leave Blank
            }
        });

        //Create Dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New Educational Attainment")
                .setView(dialogView)
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Fetch Data
                        String strSchool = etxtSchool.getText().toString().trim();
                        String strCourse = etxtCourse.getText().toString().trim();
                        String years = yearFrom.getSelectedItem() + " - " + yearTo.getSelectedItem();

                        //Restrictions
                        try{

                            if(strSchool.isEmpty())
                                throw new Exception("Please define your school.");
                            else if(strCourse.isEmpty())
                                throw new Exception("Please define your course");
                            //End of Restrictions

                            //Add a holder for the data/
                            User.EducationalAttainment educationalAttainment = new User.EducationalAttainment();
                            educationalAttainment.setSchool(strSchool);
                            educationalAttainment.setCourse(strCourse);
                            educationalAttainment.setYear(years);

                            //Add to the lists
                            currentUser.getWorkerProfile().getEducations().add(educationalAttainment);

                            //Load the Educational Attainment
                            LoadEducList();
                        }catch (Exception ex){
                            alert.showErrorMessage("Notification",ex.getMessage());
                        }

                    }
                })
                .create();

        //Show Dialog
        dialog.show();
    }

    private void LoadEducList(){
        //Remove all Views
        lvEducationAttainment.removeAllViews();

        //Check if lists exists
        if(currentUser.getWorkerProfile().getEducations() == null){
            currentUser.getWorkerProfile().setEducations(new ArrayList<User.EducationalAttainment>());
        }

        //Create a temporary List Holder
        List<User.EducationalAttainment> list = currentUser.getWorkerProfile().getEducations();

        //Create an adapter for Educational Attainment
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);

        //Create a loop to get the String Values of the Lists
        for(int i = 0; i<list.size(); i++){
            //Fetch the data
            String data = list.get(i).toString();

            //Add the data to the adapter
            adapter.add(data);

            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null,false);

            final int position = i;

            TextView textView = (TextView)view;
            textView.setText(data);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //Create Delete Dialog
                AlertDialog deleteDialog = new AlertDialog.Builder(WorkerSetup.this)
                        .setTitle("Notification")
                        .setMessage("Are you sure you want to delete " + adapter.getItem(position) + "?")
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Delete function
                                currentUser.getWorkerProfile().getEducations().remove(position);

                                //Load List
                                LoadEducList();
                            }
                        })
                        .create();

                //Show dialog
                deleteDialog.show();
                }
            });

            lvEducationAttainment.addView(textView);
        }
    }

    public void addNewExperience(View v){
        //Get the View
        final View dialogView = getLayoutInflater().inflate(R.layout.add_new_experience,null,false);

        //Get View Components
        final EditText etxtCompany = dialogView.findViewById(R.id.etxt_entry_work_experience_company);
        final EditText etxtPosition = dialogView.findViewById(R.id.etxt_entry_work_experience_position);
        final Spinner spinnerYearFrom = dialogView.findViewById(R.id.spinner_new_entry_work_experience_year_from);
        final Spinner spinnerYearTo = dialogView.findViewById(R.id.spinner_new_entry_work_experience_year_to);

        //Create adapter for spinners Data;
        final ArrayAdapter<Integer> yearFromAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line);

        //Find the current year
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        //Create a loop for the years
        for(int i = currentYear - 50; i<currentYear; i++){
            yearFromAdapter.add(i);
        }

        //Set adapter
        spinnerYearFrom.setAdapter(yearFromAdapter);

        //Setup year from on click item
        spinnerYearFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Get selected Item
                int selecteditem = yearFromAdapter.getItem(position);

                //Create a list
                ArrayAdapter<String> toYear = new ArrayAdapter<String>(WorkerSetup.this,android.R.layout.simple_dropdown_item_1line);

                //Create a loop to get to the current year.
                for(int i = selecteditem; i<currentYear;i++){
                    toYear.add(Integer.toString(i));
                }

                //Add Current
                toYear.add("Current");

                //Setup adapter
                spinnerYearTo.setAdapter(toYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Create Dialog
        AlertDialog experienceDialog = new AlertDialog.Builder(this)
                .setTitle("Add new Experience")
                .setView(dialogView)
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Fetch Data
                        String strCompany = etxtCompany.getText().toString().trim();
                        String strPosition = etxtPosition.getText().toString().trim();
                        String yearFrom = spinnerYearFrom.getSelectedItem() + " - " + spinnerYearTo.getSelectedItem();

                        //Restrictions
                        try{

                            if(strCompany.isEmpty())
                                throw new Exception("Please define your company");
                            else if(strPosition.isEmpty())
                                throw new Exception("Please define your position");
                            //End of restrictions

                            //Add the data to the model
                            User.Experiences exp = new User.Experiences();
                            exp.setCompany(strCompany);
                            exp.setJobTitle(strPosition);
                            exp.setYears(yearFrom);

                            //Add the Experience
                            currentUser.getWorkerProfile().getExperiences().add(exp);

                            //Load the List!
                            LoadExperienceList();

                        }catch (Exception ex){
                            alert.showErrorMessage("Notification",ex.getMessage());
                        }

                    }
                })
                .create();

        //Show dialog
        experienceDialog.show();

    }

    private void LoadExperienceList(){
        //Remove All Views
        lvExperience.removeAllViews();

        //Check if lists exists
        if(currentUser.getWorkerProfile().getExperiences() == null){
            currentUser.getWorkerProfile().setExperiences(new ArrayList<User.Experiences>());
        }

        //Create a temporary list holder
        List<User.Experiences> experiences = currentUser.getWorkerProfile().getExperiences();

        //Create a list adapter
        final ArrayAdapter<String> experiencesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);

        //Fill the list using loop
        for(int i = 0; i<experiences.size(); i++){
            experiencesAdapter.add(experiences.get(i).toString());

            final int position = i;

            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null,false);
            TextView textView = (TextView)view;
            textView.setText(experiences.get(i).toString());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Create dialog for delete
                AlertDialog dialogForDelete = new AlertDialog.Builder(WorkerSetup.this)
                        .setTitle("Notification")
                        .setMessage("Are you sure you want to delete " + experiencesAdapter.getItem(position) + "?")
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentUser.getWorkerProfile().getExperiences().remove(position);
                                LoadExperienceList();
                            }
                        })
                        .create();
                //Show Delete Dialog
                dialogForDelete.show();
                }
            });

            lvExperience.addView(textView);
        }

//        lvExperience.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                //Create dialog for delete
//                AlertDialog dialogForDelete = new AlertDialog.Builder(WorkerSetup.this)
//                        .setTitle("Notification")
//                        .setMessage("Are you sure you want to delete " + experiencesAdapter.getItem(position) + "?")
//                        .setNegativeButton("No",null)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                currentUser.getWorkerProfile().getExperiences().remove(position);
//                                LoadExperienceList();
//                            }
//                        })
//                        .create();
//                //Show Delete Dialog
//                dialogForDelete.show();
//            }
//        });
//
//        //Set adapter
//        lvExperience.setAdapter(experiencesAdapter);
    }

    public void saveWorkerSetup(View view){
        try{
            //Terms and agreement Check
            if(!cbTermsAgreement.isChecked()){
                throw new Exception("You must read and agree to the terms and agreement we provided.");
            }else if(tilServices.getEditText().getText().toString().isEmpty()){
                throw new Exception("Please fill in the Main service you want to provide your customers.");
            }else{
                //Create dialog
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Save")
                        .setMessage("Are you sure you want to save this worker setup?")
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentUser.getWorkerProfile().setMainService(tilServices.getEditText().getText().toString().trim());
                                currentUser.setWorkerMode(swWorkMode.isChecked());
                                currentUser.getWorkerProfile().setTermsAndAgreement(cbTermsAgreement.isChecked());
                                userDr.setValue(currentUser);

                                if(swWorkMode.isChecked()){
                                    worker_setup_save_location_notfier_textv.setText("New Location Saved!");
                                    worker_setup_save_location_notfier_textv.setVisibility(View.VISIBLE);
                                }

                                referenceForServices = FirebaseDatabase.getInstance().getReference("services");
                                referenceForServices.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean isMentioned = false;
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            String data = ds.getValue(String.class);

                                            if(data.equals(currentUser.getWorkerProfile().getMainService())){
                                                isMentioned = true;
                                            }
                                        }

                                        if(!isMentioned){
                                            referenceForServices.push().setValue(currentUser.getWorkerProfile().getMainService());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .create();

                //Show dialog
                dialog.show();
            }
        }catch (Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    private Chip getChip(final ChipGroup entryChipGroup, final String text, final int position, View.OnClickListener listener) {
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setOnClickListener(listener);
        return chip;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

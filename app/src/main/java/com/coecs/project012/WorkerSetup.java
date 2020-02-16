package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Date;
import java.util.ArrayList;

public class WorkerSetup extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout tilServices;
    private ChipGroup cgSkills,cgOtherServices;
    private ListView lvExperience,lvEducationAttainment;
    private CircleImageView civOtherServices,civEduc,civExperience,civSkills;

    private ArrayList<String> listSkills,listOtherServices; //Holders.
    private ArrayAdapter<String> listEducation,listExperice;

    private User.EducationalAttainment[] attainments;
    private User.Experiences[] experiences;
    private User.Location location;

    private int yearFrom = 1970,yearTo;

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

        addNewEducationalAttainment();

        tilServices = findViewById(R.id.til_services);
    }

    //Chips
    private void addNewSkill(){
        final View serviceEntryView = View.inflate(this,R.layout.add_new_entry_other_service,null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(serviceEntryView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etxt = serviceEntryView.findViewById(R.id.etxt_entry_other_service);

                        String textInput = etxt.getText().toString().trim();
                        try{
                            if(listSkills.contains(textInput))
                                throw new Exception("Please do not add redundant Skills");
                            else if(textInput.isEmpty())
                                throw new Exception("The text is empty.");
                            else{
                                listSkills.add(textInput);
                                LoadSkills();
                            }
                        }catch (Exception ex){
                            new Alert(WorkerSetup.this).showErrorMessage("Notification",ex.getMessage());
                        }
                    }
                })
                .setNegativeButton("Cancel",null)
                .setTitle("Skills")
                .setMessage("Add a new Skill")
                .create();
        dialog.show();
        return;
    }

    private void LoadSkills(){
        for(String str : listSkills){
            cgSkills.addView(getChip(cgSkills,str));
        }
    }

    private void addNewService(){
        final View serviceEntryView = View.inflate(this,R.layout.add_new_entry_other_service,null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(serviceEntryView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etxt = serviceEntryView.findViewById(R.id.etxt_entry_other_service);

                        String textInput = etxt.getText().toString().trim();
                        try{
                            if(listSkills.contains(textInput))
                                throw new Exception("Please do not add redundant Service");
                            else if(textInput.isEmpty())
                                throw new Exception("The text is empty.");
                            else{
                                listOtherServices.add(textInput);
                                LoadServices();
                            }
                        }catch (Exception ex){
                            new Alert(WorkerSetup.this).showErrorMessage("Notification",ex.getMessage());
                        }
                    }
                })
                .setNegativeButton("Cancel",null)
                .setTitle("Other Service")
                .setMessage("Add a new Service")
                .create();
        dialog.show();
        return;
    }

    private void LoadServices(){
        for(String str : listOtherServices){
            cgOtherServices.addView(getChip(cgOtherServices,str));
        }
    }

    //ListViews
    private void addNewEducationalAttainment(){
        final View view = getLayoutInflater().inflate(R.layout.add_new_entry_education_attainment,null,false);

        final ArrayAdapter<Integer> listOfYearsFrom = new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line),listOfYearsTo = new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line);

        listOfYearsFrom.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        listOfYearsTo.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        yearTo = 2020;

        for(int i = yearFrom; i<=yearTo; i++){
            listOfYearsFrom.add(i);
        }

        final EditText etxtSchool = view.findViewById(R.id.etxt_new_entry_school);
        final EditText etxtCourse = view.findViewById(R.id.etxt_new_entry_course);

        final Spinner spinnerTo = view.findViewById(R.id.spinner_new_entry_educational_attainment_year_to);
        final Spinner spinnerFrom = view.findViewById(R.id.spinner_new_entry_educational_attainment_year_from);
        spinnerFrom.setAdapter(listOfYearsFrom);
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int from = listOfYearsFrom.getItem(position);
                listOfYearsTo.clear();

                for(int j = from; j<=yearTo; j++){
                    listOfYearsTo.add(j);
                }

                spinnerTo.setAdapter(listOfYearsTo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String textSchool = etxtSchool.getText().toString().trim();
                        String txtCourse = etxtCourse.getText().toString().trim();
                        String schoolYear = listOfYearsFrom.getItem(spinnerFrom.getSelectedItemPosition()) + " to " + listOfYearsTo.getItem(spinnerTo.getSelectedItemPosition());

                        try{

                            if(textSchool.isEmpty())
                                throw new Exception("The school is required as you fill it in.");
                            else if(txtCourse.isEmpty())
                                throw new Exception("Course is empty. Please fill it in");



                        }catch (Exception ex){
                            new Alert(WorkerSetup.this).showErrorMessage("Notification",ex.getMessage());
                        }
                    }
                })
                .setNegativeButton("Cancel",null)
                .setTitle("Skills")
                .setMessage("Add a new Skill")
                .create();
        dialog.show();
    }

    private Chip getChip(final ChipGroup entryChipGroup, String text) {
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((Chip)v).getText().toString().trim();
                listSkills.remove(text);
                entryChipGroup.removeView(v);
                LoadSkills();
            }
        });
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

}

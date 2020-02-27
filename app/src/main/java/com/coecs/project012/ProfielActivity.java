package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfielActivity extends AppCompatActivity {
    //Common Components
    private Toolbar toolbar;
    private CircleImageView civDisplayPhoto;
    private TextView tvDisplayName, tvDisplayEmail;
    private Button btnSendMessage;
    private TabHost tabhost;

    //Profile Components
    private TextView txvMainService,txtvOtherservices,txtvSkills,txtvExperiences,txtvEducation;
    private ChipGroup cgOtherServices,cgSkills;
    private LinearLayout listLayoutWorkExperiences,listLayoutEducationalAttainment;

    //Ratings Components
    private TextView profile_txtv_rating;
    private ListView profile_listv_rating;

    //Alert
    private Alert alert;

    //Database
    private DatabaseReference userDatabaseReference,currentUserDataReference,conversationDatabase;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    //Users
    private String userId;
    private User userProfile;
    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiel);
        init();
    }

    private void init(){
        toolbar = findViewById(R.id.profile_toolbar);
        civDisplayPhoto = findViewById(R.id.profile_display_photo);
        tvDisplayName = findViewById(R.id.profile_display_name);
        tvDisplayEmail = findViewById(R.id.profile_display_email);
        btnSendMessage = findViewById(R.id.profile_btn_send_message);
        tabhost = findViewById(R.id.profile_tab_host);

        //Setup Profile Components
        txvMainService = findViewById(R.id.profile_main_service);
        cgOtherServices = findViewById(R.id.profile_cg_other_services);
        cgSkills = findViewById(R.id.profile_cg_skills);
        listLayoutWorkExperiences = findViewById(R.id.profile_layout_list_work_experiences);
        listLayoutEducationalAttainment = findViewById(R.id.profile_layout_list_educational_attainment);
        txtvOtherservices = findViewById(R.id.profile_other_services_textv);
        txtvSkills = findViewById(R.id.profile_skills_txtv);
        txtvExperiences = findViewById(R.id.profile_work_experience_textv);
        txtvEducation = findViewById(R.id.profile_education_textv);

        //Setup Rating Components
        profile_txtv_rating = findViewById(R.id.profile_txtv_rating);
        profile_listv_rating = findViewById(R.id.profile_listv_ratings);

        //Setup Tabhost
        tabhost.setup();

        tabhost.addTab(tabhost.newTabSpec("one").setContent(R.id.tab1).setIndicator("Profile"));
        tabhost.addTab(tabhost.newTabSpec("two").setContent(R.id.tab2).setIndicator("Reviews"));

        //Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setup Alert
        alert = new Alert(this);

        //Fetch the users UID
        userId = getIntent().getExtras().getString("USER_ID");


        try{
            if(userId == null){
                throw new Exception("User ID cannot be found.");
            }

            //Setup Database & Authentication
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            currentUserDataReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            conversationDatabase = FirebaseDatabase.getInstance().getReference("conversations");
            storage = FirebaseStorage.getInstance();

            //Populate Data
            userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Fetch the user
                    User user = dataSnapshot.getValue(User.class);
                    userProfile = user;

                    //Populate User Data
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
                                            civDisplayPhoto.setImageURI(uri);
                                        }else{
                                            task.getException().printStackTrace();
                                        }
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    tvDisplayName.setText(user.getFirstName() + " " + user.getLastName());
                    tvDisplayEmail.setText(user.getEmail());

                    //Populate Main Service
                    txvMainService.setText(user.getWorkerProfile().getMainService());

                    //Populate Other Service
                    List<String> otherServices = user.getWorkerProfile().getOtherService();
                    if(otherServices.isEmpty()){
                        txtvOtherservices.setText("");
                    }else{
                        for(int i = 0; i<otherServices.size(); i++)
                            cgOtherServices.addView(getChip(otherServices.get(i)));
                    }

                    //Populate Skills
                    List<String> skills = user.getWorkerProfile().getSkills();
                    if(skills.isEmpty()){
                        txtvSkills.setText("");
                    }else{
                        for(int i = 0; i<skills.size(); i++)
                            cgSkills.addView(getChip(skills.get(i)));
                    }

                    //Populate Experiences
                    List<User.Experiences> experiencesList = user.getWorkerProfile().getExperiences();
                    if(experiencesList.isEmpty()){
                        txtvExperiences.setText("");
                    }else{
                        for(int i = 0; i<experiencesList.size(); i++){
                            //Inflate View
                            View view = LayoutInflater.from(ProfielActivity.this).inflate(android.R.layout.simple_list_item_1,null,false);
                            TextView tv = (TextView)view;
                            tv.setText(experiencesList.get(i).toString());
                            listLayoutWorkExperiences.addView(view);
                        }
                    }

                    //Populate Educational Attainment
                    List<User.EducationalAttainment> educationalAttainments = user.getWorkerProfile().getEducations();
                    if(educationalAttainments.isEmpty()){
                        txtvEducation.setText("");
                    }else{
                        for(int i = 0; i<educationalAttainments.size(); i++){
                            View view = LayoutInflater.from(ProfielActivity.this).inflate(android.R.layout.simple_list_item_1,null,false);
                            TextView tv = (TextView)view;
                            tv.setText(educationalAttainments.get(i).toString());
                            listLayoutEducationalAttainment.addView(view);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });

            //Fetch the Current Users Data;
            currentUserDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try{
                        currentUserProfile = dataSnapshot.getValue(User.class);
                    }catch (Exception ex){
                        alert.showErrorMessage("Notification",ex.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (Exception ex){
            alert.showErrorMessage("Notification",ex.getMessage());
        }
    }

    public void SendMessage(View view){
        try{
            List<String> usersListOfConversationIds = userProfile.getConversationIds();
            List<String> currentUserListOfConversationIds = currentUserProfile.getConversationIds();

            String conversationId = null;

            //Tracks if there is null
            if(usersListOfConversationIds != null && currentUserListOfConversationIds != null){
                //Check if there is a same conversation id in both of the users
                Set<String> collections = new HashSet<String>();
                collections.addAll(usersListOfConversationIds);
                collections.retainAll(currentUserListOfConversationIds);

                if(!collections.isEmpty()){
                    conversationId = (String)collections.toArray()[0];
                }
            }

            //If the conversation Id is not found
            if(conversationId == null){
                //Create a new Conversation
                conversationId = conversationDatabase.push().getKey();

                List<String> users = new ArrayList<String>();
                users.add(currentUserProfile.getUid());
                users.add(userProfile.getUid());

                Conversation convo = new Conversation(conversationId,users);
                conversationDatabase.child(conversationId).setValue(convo);

                //Save from the users data
                if(usersListOfConversationIds == null)
                    userProfile.setConversationIds(new ArrayList<String>());

                userProfile.getConversationIds().add(conversationId);
                userDatabaseReference.setValue(userProfile);

                if(currentUserListOfConversationIds == null)
                    currentUserProfile.setConversationIds(new ArrayList<String>());

                currentUserProfile.getConversationIds().add(conversationId);
                currentUserDataReference.setValue(currentUserProfile);
            }

            Intent intent = new Intent(ProfielActivity.this,ChatActivity.class);
            intent.putExtra("CONVERSATION_ID",conversationId);
            startActivity(intent);
        }catch (Exception ex){
            alert.showErrorMessage("Notificaion",ex.getMessage());
        }
    }

    private Chip getChip(String text){
        Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
        chip.setText(text);
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
        finish();
    }
}

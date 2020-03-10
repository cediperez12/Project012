package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Map;

public class SelfProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileCiv;
    private TextView tvMain,tvSub;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userData;
    private StorageReference profileImageReference;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);
        init();
    }

    private void init(){
        toolbar = findViewById(R.id.self_profile_toolbar);
        profileCiv = findViewById(R.id.civ_self_profile);
        tvMain = findViewById(R.id.self_profile_main_text);
        tvSub = findViewById(R.id.self_profile_sub_text);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        userData = database.getReference("users").child(currentUser.getUid());

        SetupProfile();
    }

    private void SetupProfile(){
        userData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                tvMain.setText(user.getFirstName() + " " + user.getLastName());
                tvSub.setText(user.getEmail());

                final File file;
                try{
                    file = File.createTempFile("image","png");

                    profileImageReference = FirebaseStorage.getInstance().getReference(user.getProfileImagePath());
                    profileImageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            Uri uri = Uri.fromFile(file);
                            profileCiv.setImageURI(uri);
                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                tvMain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = getLayoutInflater().inflate(R.layout.change_name_layout,null,false);

                        final EditText etxtFname = view.findViewById(R.id.etxt_change_name_fname);
                        final EditText etxtLname = view.findViewById(R.id.etxt_change_name_lname);

                        AlertDialog dialog = new AlertDialog.Builder(SelfProfile.this)
                                .setTitle("Change your name")
                                .setView(view)
                                .setNegativeButton("Cancel",null)
                                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String strFname = etxtFname.getText().toString().trim();
                                        String strLname = etxtLname.getText().toString().trim();

                                        try{
                                            if(strFname.isEmpty()){
                                                throw new Exception("You did not fill in the first name.");
                                            }else if(strLname.isEmpty()){
                                                throw new Exception("You did not fill in the last name.");
                                            }

                                            Map<String,Object> hashMap = new HashMap<>();
                                            hashMap.put("firstName",strFname);
                                            hashMap.put("lastName",strLname);

                                            userData.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        new Alert(SelfProfile.this).showErrorMessage("Notification","Successfully update your name");
                                                        SetupProfile();
                                                    }
                                                }
                                            });

                                        }catch (Exception ex){
                                            new Alert(getApplicationContext()).showErrorMessage("Notification",ex.getMessage());
                                        }
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String newPass,confrimPass,oldPass;

    private void ChangePassword(){
        View view = getLayoutInflater().inflate(R.layout.change_password_layout,null,false);

        final EditText etxtNewPass = view.findViewById(R.id.change_pass_etxtNewPass);
        final EditText etxtConfirmPass = view.findViewById(R.id.change_pass_etxtConfirmPass);
        final EditText etxtOldPass = view.findViewById(R.id.change_pass_etxtOldPass);

        if(newPass != null && confrimPass != null && oldPass != null){
            etxtNewPass.setText(newPass);
            etxtConfirmPass.setText(confrimPass);
            etxtOldPass.setText(oldPass);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(view)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newPass = etxtNewPass.getText().toString().trim();
                        confrimPass = etxtConfirmPass.getText().toString().trim();
                        oldPass = etxtOldPass.getText().toString().trim();

                        try{
                            if(newPass.isEmpty()){
                                throw new Exception("Enter your New Password");
                            }else if(confrimPass.equals(newPass)){
                                throw new Exception("Please confirm your password.");
                            }else if(oldPass.equals(user.getPassword())){
                                throw new Exception("Please confirm your old password.");
                            }

                            currentUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Map<String,Object> map = new HashMap<String, Object>();
                                        map.put("password",newPass);
                                        userData.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    new Alert(SelfProfile.this).showErrorMessage("Notification","Successfully updated your password.");
                                                    SetupProfile();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }catch (Exception ex){
                            new Alert(getApplicationContext()).showErrorMessage("Notification",ex.getMessage());
                        }
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.self_profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.self_profile_change_pass:
                ChangePassword();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}

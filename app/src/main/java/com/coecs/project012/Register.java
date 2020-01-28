package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

public class Register extends AppCompatActivity{

    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private TextInputLayout til_first_name,til_last_name,til_email,til_password,til_confirm_pass;
    private EditText etxt_fname,etxt_lname,etxt_email,etxt_password,etxt_confirm_pass;
    private CircleImageView civ_profile_image;
    private Toolbar toolbar;

    private Uri profileImage;

    private RegisterFunction registerFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){
        authentication = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        til_first_name = findViewById(R.id.til_email_register);
        til_last_name = findViewById(R.id.til_last_name_register);
        til_email = findViewById(R.id.til_email_register);
        til_password = findViewById(R.id.til_password_register);
        til_confirm_pass = findViewById(R.id.til_confirm_password_register);
        civ_profile_image = findViewById(R.id.civ_profile_image_register);
        toolbar = findViewById(R.id.toolbar_register);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Register");

        etxt_fname = til_first_name.getEditText();
        etxt_lname = til_last_name.getEditText();
        etxt_email = til_email.getEditText();
        etxt_password = til_password.getEditText();
        etxt_confirm_pass = til_confirm_pass.getEditText();

        etxt_fname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(s.toString().isEmpty())
                        throw new Exception("Please enter your first name.");
                    til_first_name.setErrorEnabled(false);
                }catch (Exception ex){
                    til_first_name.setError(ex.getMessage());
                    til_first_name.setErrorEnabled(true);
                }
            }
        });
        etxt_lname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(s.toString().isEmpty())
                        throw new Exception("Please enter your last name.");
                    til_last_name.setErrorEnabled(false);
                }catch (Exception ex){
                    til_last_name.setError(ex.getMessage());
                    til_last_name.setErrorEnabled(true);
                }
            }
        });
        etxt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(isValid(s.toString()))
                        throw new Exception("Your email address is invalid.");
                    if(s.toString().isEmpty())
                        throw new Exception("Please enter your email.");
                    til_email.setErrorEnabled(false);
                }catch(Exception ex){
                    til_email.setErrorEnabled(true);
                    til_email.setError(ex.getMessage());
                }
            }
        });
        etxt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(s.toString().isEmpty())
                        throw new Exception("Please enter your password");
                    if(s.toString().length() < 8)
                        throw new Exception("Your password must be 8 and above");
                    til_password.setErrorEnabled(false);
                }catch (Exception ex){
                    til_password.setErrorEnabled(true);
                    til_password.setError(ex.getMessage());
                }
            }
        });
        etxt_confirm_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(s.toString().isEmpty())
                        throw new Exception("Please confirm your password");
                    if(!s.toString().equals(etxt_password.getText().toString()))
                        throw new Exception("Your password does not matched");
                    til_confirm_pass.setErrorEnabled(false);
                }catch(Exception ex){
                    til_confirm_pass.setErrorEnabled(true);
                    til_confirm_pass.setError(ex.getMessage());
                }
            }
        });
    }

    public void clickRegister(View view){
        try{
            if(til_first_name.isErrorEnabled())
                throw new Exception(til_first_name.getError().toString());
            if(til_last_name.isErrorEnabled())
                throw new Exception(til_last_name.getError().toString());
            if(til_password.isErrorEnabled())
                throw new Exception(til_password.getError().toString());
            if(til_confirm_pass.isErrorEnabled())
                throw new Exception(til_confirm_pass.getError().toString());
            if(profileImage == null){
                throw new Exception("Please put your profile photo.");
            }

            String email = til_email.getEditText().getText().toString();
            String password = til_password.getEditText().getText().toString();
            String firstName = til_first_name.getEditText().getText().toString();
            String lastName = til_last_name.getEditText().getText().toString();

            User newUser = new User(email,password,firstName,lastName,null,null);

            registerFunction = new RegisterFunction(this,newUser);
            registerFunction.execute();

        }catch(Exception ex){
            new Alert(this).showErrorMessage("Error",ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickChooseImage(View view){
        CropImage.activity()
                .setAspectRatio(1,1)
                .setFixAspectRatio(true)
                .setActivityTitle("Crop your profile image")
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileImage = result.getUri();
                civ_profile_image.setImageURI(profileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception ex = result.getError();
            }
        }
    }

    private boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private class RegisterFunction extends AsyncTask<String,String,Void> {

        private ProgressDialog pg;
        private User newUser;
        private Context con;
        private Alert alert;

        RegisterFunction(Context con, User newUser){
            this.newUser = newUser;
            this.con = con;

            pg = new ProgressDialog(con);
            pg.setCancelable(false);
            pg.setMessage("Registering your new account...");
            pg.setIndeterminate(true);
            pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            alert = new Alert(con);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            authentication.createUserWithEmailAndPassword(newUser.getEmail(),newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        newUser.setUid(task.getResult().getUser().getUid());

                        String path = "images/" + newUser.getUid() + "/profile.jpg";
                        newUser.setProfileImagePath(path);

                        StorageReference reference = FirebaseStorage.getInstance().getReference().child(path);
                        reference.putFile(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                database.getReference("users").child(newUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pg.dismiss();
                                            alert.showErrorMessage("Register","Thank you for registering you may now use your account.");
                                            authentication.signOut();

                                            Intent intent = new Intent(getApplicationContext(),Login.class);
                                            startActivity(intent);
                                            Register.this.finish();
                                        }else{
                                            alert.showErrorMessage("Error",task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                pg.setMessage("Uploading your images...");
                            }
                        });
                    }else{
                        pg.dismiss();
                        alert.showErrorMessage("Error",task.getException().getMessage());
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(pg.isShowing()){
                pg.dismiss();
            }
        }
    }

    public class Alert{

        private Context con;

        Alert(Context context){
            con = context;
        }

        private void showErrorMessage(String title, String message){
            new AlertDialog.Builder(con)
                    .setCancelable(false)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Okay",null)
                    .create().show();
        }

    }
}












































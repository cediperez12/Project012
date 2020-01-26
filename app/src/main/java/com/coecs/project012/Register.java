package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
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

    private Uri profileImage;

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

            final String email = til_email.getEditText().getText().toString();
            final String password = til_password.getEditText().getText().toString();

            authentication.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String uid = task.getResult().getUser().getUid();
                    }else{

                    }
                }
            });

        }catch(Exception ex){
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(ex.getMessage())
                    .setPositiveButton("OKAY",null)
                    .setCancelable(false)
                    .create().show();
        }
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

        RegisterFunction(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}












































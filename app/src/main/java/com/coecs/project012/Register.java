package com.coecs.project012;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

public class Register extends AppCompatActivity{

    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private TextInputLayout til_first_name,til_last_name,til_email,til_password,til_confirm_pass;

    private EditText etxt_fname,etxt_lname,etxt_email,etxt_password,etxt_confirm_pass;

    private CircleImageView civ_profile_image;

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

            }
        });
    }

    public void clickRegister(View view){

    }
}












































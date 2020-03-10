package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;

    private TextInputLayout til_email,til_password;
    private EditText etxt_email, etxt_password;

    private Alert alert;

    private LoginFunction login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init(){
        auth = FirebaseAuth.getInstance();

        alert = new Alert(this);

        til_email = findViewById(R.id.til_email_login);
        til_password = findViewById(R.id.til_password_login);

        etxt_email = til_email.getEditText();
        etxt_password = til_password.getEditText();

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
                    if(s.toString().isEmpty()){
                        throw new Exception("Please enter your email.");
                    }

                    til_email.setErrorEnabled(false);
                }catch (Exception ex){
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
                    if(s.toString().isEmpty()){
                        throw new Exception("Your password cannot be empty.");
                    }

                    til_password.setErrorEnabled(false);
                }catch (Exception ex){
                    til_password.setErrorEnabled(true);
                    til_password.setError(ex.getMessage());
                }
            }
        });
    }

    private boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public void registerAccount(View view){
        Intent intent = new Intent(getApplicationContext(),Register.class);
        startActivity(intent);
    }

    public void login(View view){
        try{
            if(til_password.isErrorEnabled()){
                throw new Exception(til_password.getError().toString());
            }
            if(til_email.isErrorEnabled()){
                throw new Exception(til_email.getError().toString());
            }

            User user = new User(etxt_email.getText().toString(),etxt_password.getText().toString());

            login = new LoginFunction(this, user);
            login.execute();

        }catch (Exception ex){
            alert.showErrorMessage("Error",ex.getMessage());
        }
    }

    private class LoginFunction extends AsyncTask<String,String,Void> {

        private User loggingUser;
        private ProgressDialog pg;
        private Context context;

        LoginFunction(Context con, User loggingUser){
            this.context = con;
            this.loggingUser = loggingUser;

            pg = new ProgressDialog(context);
            pg.setMessage("Logging in...");
            pg.setIndeterminate(true);
            pg.setCancelable(false);
            pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try{
                Thread.sleep(5000);

                auth.signInWithEmailAndPassword(loggingUser.getEmail(),loggingUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Login.this.startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            Login.this.finish();
                        }else{
                            //Create Error
                            alert.showErrorMessage("Notification","The email or password you entered is incorrect.");
                        }
                        pg.dismiss();
                    }
                });

            }catch (Exception ex){
                pg.dismiss();
                alert.showErrorMessage("Error",ex.getMessage());
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

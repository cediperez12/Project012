package com.coecs.project012;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        try{
            Thread.sleep(5000);

            if(auth.getCurrentUser() != null){
                startActivity(new Intent(getApplicationContext(), Login.class));
            }else{
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }

            finish();
        }catch (Exception ex){
            new Alert(this).showErrorMessage("Error",ex.getMessage());
        }
    }
}

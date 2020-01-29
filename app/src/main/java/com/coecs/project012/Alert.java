package com.coecs.project012;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class Alert{

    private Context con;

    Alert(Context context){
        con = context;
    }

    public void showErrorMessage(String title, String message){
        new AlertDialog.Builder(con)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay",null)
                .create().show();
    }

}

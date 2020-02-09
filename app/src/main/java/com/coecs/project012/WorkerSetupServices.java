package com.coecs.project012;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.tv.TvContract;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkerSetupServices extends Fragment {


    public WorkerSetupServices() {
        // Required empty public constructor
    }

    private TextInputLayout tilServices;
    private ChipGroup chipGroupOtherServices;
    private CircleImageView civAddOtherService;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;

    private ArrayList<String> mainServicesMentioned;
    private ArrayList<String> otherServices;
    private String mainService;

    private ProgressDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_worker_setup_services,container,false);
        init(view);
        return view;
    }

    private void init(View view){
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage("Loading... Please wait.");
        loadingDialog.setCancelable(false);

        loadingDialog.show();

        reference = database.getReference("mainService");
        mainServicesMentioned = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                    mainServicesMentioned.add((String)ds.getValue());

                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                loadingDialog.dismiss();
            }
        });

        tilServices = view.findViewById(R.id.til_services);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tilServices.getEditText().setAutofillHints(String.valueOf(mainServicesMentioned));
        }

        chipGroupOtherServices = view.findViewById(R.id.chip_group_other_services);

        civAddOtherService = view.findViewById(R.id.worker_setup_add_services);
        civAddOtherService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View serviceEntryView = View.inflate(getContext(),R.layout.add_new_entry_other_service,null);

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setView(serviceEntryView)
                        .setTitle("Add other services you offer.")
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    EditText etxt = serviceEntryView.findViewById(R.id.etxt_entry_other_service);
                                    String otherService = etxt.getText().toString().trim();

                                    if(otherService.isEmpty()){
                                        throw new Exception ("Other service is empty.");
                                    }

                                    getChip(chipGroupOtherServices,otherService);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .create();

                dialog.show();
            }
        });
    }

    private Chip getChip(final ChipGroup entryChipGroup, String text) {
        final Chip chip = new Chip(getContext());
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entryChipGroup.removeView(chip);
            }
        });
        return chip;
    }

    public String fetchMainService(){
        return mainService;
    }

    public ArrayList<String> fetchOtherService(){
        return otherServices;
    }

}

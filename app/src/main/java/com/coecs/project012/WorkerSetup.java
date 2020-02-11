package com.coecs.project012;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

public class WorkerSetup extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private Button btnNext,btnPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_setup);
        init();
    }

    private void init() {
        toolbar = findViewById(R.id.worker_setup_toolbar);
        viewPager = findViewById(R.id.worker_setup_vp);
        btnNext = findViewById(R.id.worker_setup_btn_next);
        btnPrev = findViewById(R.id.worker_setup_btn_prev);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
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

    private class FragmentAdapter extends FragmentPagerAdapter {

        public WorkerSetupServices workerSetupServices;

        public FragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);

            workerSetupServices = new WorkerSetupServices();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position){

                case 0:
                    return new WorkerSetupServices();

            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}

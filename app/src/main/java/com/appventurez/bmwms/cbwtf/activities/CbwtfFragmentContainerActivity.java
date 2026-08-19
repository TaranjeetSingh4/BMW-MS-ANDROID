package com.appventurez.bmwms.cbwtf.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.appventurez.bmwms.R;
import com.appventurez.bmwms.cbwtf.fragments.CbwtfHcfScanFragment;
import com.appventurez.bmwms.cbwtf.fragments.CbwtfProfileFragment;
import com.appventurez.bmwms.cbwtf.fragments.ReportsFragment;


public class CbwtfFragmentContainerActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbwtf_fragment_container);

        int f_id = getIntent().getIntExtra("f_id",0);

        switch (f_id){

            case 0:
                Fragment fragment = new CbwtfHcfScanFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("scanType",0);
                fragment.setArguments(bundle);
                startFragment(fragment);
                break;
            case 1:

                startFragment(new CbwtfProfileFragment());
                break;
            case 9:
                Fragment fragment2 = new CbwtfHcfScanFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putInt("scanType",1);
                fragment2.setArguments(bundle2);
                startFragment(fragment2);
                break;
            case 10:
                startFragment(new ReportsFragment());
                break;
        }
    }

    public void startFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.cbwtf_fragment_container,fragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }



}
package com.bigbywolf.weathercloudcolorpicker;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    NsdHelper mNsdHelper;
    ProgressBar progressBar;
    Spinner deviceSpinner;
    CountDownTimer scanTimer;
    boolean scanInProgress = false;
    Button scanButton;
    int spinnerPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
        scanButton = (Button) findViewById(R.id.scanButton);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

//        deviceSpinner.setOnItemClickListener((AdapterView.OnItemClickListener) new SpinnerListener());

//        deviceSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                spinnerPosition = position;
//                Log.i("spinner", "position " + position);
//            }
//        });

        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("spinner", "position " + position);
                String selectedItemText = (String) parent.getItemAtPosition(position);
                Log.i("spinner", "position " + selectedItemText);
                spinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("spinner", "nothing pressed");

            }
        });

    }

    public void clickDiscover(View v) {
        if(!scanInProgress){
            scanButton.setText("CANCEL SCAN");
            progressBar.setVisibility(View.VISIBLE);
            mNsdHelper.discoverServices();

            scanTimer = new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mNsdHelper.stopDiscovery();
                    progressBar.setVisibility(View.INVISIBLE);
                    scanButton.setText("SCAN FOR DEVICES");
//                    ArrayList<NsdServiceInfo> foundDevices = mNsdHelper.getServiceList();

                    List<String> names = new ArrayList<>();
                    for(int i = 0; i < mNsdHelper.getServiceList().size(); i++){
                        names.add(mNsdHelper.getServiceList().get(i).getServiceName());
                    }

                    // Creating adapter for spinner
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, names);

                    // Drop down layout style - list view with radio button
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // attaching data adapter to spinner
                    deviceSpinner.setAdapter(dataAdapter);

//                    Log.i("discover", mNsdHelper.getServiceList().toString());
                    Log.i("discover", mNsdHelper.getServiceList().toString());
                    Toast toast = Toast.makeText(getApplicationContext(), "SCAN FINISHED", Toast.LENGTH_LONG);
                    toast.show();

                }
            }.start();

        }
        else if(scanInProgress){
            scanTimer.cancel();
            progressBar.setVisibility(View.INVISIBLE);
            scanButton.setText("SCAN FOR DEVICES");

        }

    }


    public void connectToDevice(View view){
        Log.i("connect", "connect pressed for " + spinnerPosition + " device " + mNsdHelper.getServiceList().get(spinnerPosition).getServiceName());
    }

}


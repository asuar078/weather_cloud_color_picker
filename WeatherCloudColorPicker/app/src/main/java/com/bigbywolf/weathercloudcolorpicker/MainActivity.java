package com.bigbywolf.weathercloudcolorpicker;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
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

import com.bigbywolf.weathercloudcolorpicker.utils.Cloud;
import com.bigbywolf.weathercloudcolorpicker.utils.NsdHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private NsdHelper mNsdHelper;
    private ProgressBar progressBar;
    private Spinner deviceSpinner;
    private CountDownTimer scanTimer;
    private boolean scanInProgress = false;
    private Button scanButton;
    private int spinnerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
        scanButton = (Button) findViewById(R.id.scanButton);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

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

            scanInProgress = true;
            scanButton.setText("CANCEL SCAN");
            progressBar.setVisibility(View.VISIBLE);
            mNsdHelper.discoverServices();

            // create timer to scan for devices
            scanTimer = new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mNsdHelper.stopDiscovery();
                    progressBar.setVisibility(View.INVISIBLE);
                    scanButton.setText("SCAN FOR DEVICES");

                    // convert found devices to string list
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

                    Log.i("discover", mNsdHelper.getServiceList().toString());

                    Toast.makeText(getApplicationContext(), "Scan complete", Toast.LENGTH_LONG).show();

                    scanInProgress = false;
                }
            }.start();

        }
        else if(scanInProgress){

            scanTimer.cancel();
            mNsdHelper.stopDiscovery();
            progressBar.setVisibility(View.INVISIBLE);
            scanButton.setText("SCAN FOR DEVICES");
            scanInProgress = false;
        }

    }


    public void connectToDevice(View view){
        if (!mNsdHelper.getServiceList().isEmpty()){

            NsdServiceInfo serviceInfo = mNsdHelper.resolveFoundService(mNsdHelper.getServiceList().get(spinnerPosition));

            if (serviceInfo != null){

                String sUrl = "http:/" + serviceInfo.getHost() + ":" + serviceInfo.getPort();

                try {

                    ConnectTask connectTask = new ConnectTask();

                    String response = connectTask.execute(sUrl).get();
//                    Log.i("connect", "reponse: " + response);

                    JSONObject jsonObject = new JSONObject(response);

                    // if device found switch activity
                    if (jsonObject.has("device")){

                        Toast.makeText(getApplicationContext(), "Device connected", Toast.LENGTH_LONG).show();

                        Cloud cloud = new Cloud(serviceInfo.getHost().toString(), serviceInfo.getPort(), jsonObject.getString("device") , jsonObject.getInt("mode"), jsonObject.getInt("color"));

                        Intent i = new Intent(getApplicationContext(), ConnectActivity.class);
                        i.putExtra("com.bigbywolf.weathercloudcolorpicker.utils.Cloud", cloud);
                        startActivity(i);

                    } else {

                        Toast.makeText(getApplicationContext(), "Error could not connect", Toast.LENGTH_LONG).show();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
        else {
            Toast.makeText(getApplicationContext(), "No device selected", Toast.LENGTH_LONG).show();
        }
    }

}

// task to send GET request to cloud webserver
class ConnectTask extends AsyncTask<String, Integer, String> {

    protected String doInBackground(String... urls) {
        try {
            URL url = null;
            url = new URL(urls[0]);
            Log.i("request", "url " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = "", line;
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }
            Log.i("request", content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("request", "error");
            return null;
        }

    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        // this is executed on the main thread after the process is over
        // update your UI here
    }
}


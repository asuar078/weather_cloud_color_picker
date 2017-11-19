package com.bigbywolf.weathercloudcolorpicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.bigbywolf.weathercloudcolorpicker.utils.Cloud;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ConnectActivity extends AppCompatActivity {

    private String ip;
    private int port;
    private EditText ipText;
    private EditText portText;
    private Cloud cloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ipText = (EditText) findViewById(R.id.ipText);
        portText = (EditText) findViewById(R.id.portText);

        Bundle bundle = getIntent().getExtras();
        cloud = bundle.getParcelable("com.bigbywolf.weathercloudcolorpicker.utils.Cloud");

//        Log.i("intent", "connected to " + cloud.getIp() + " " + cloud.getPort());

        ipText.setText(cloud.getIp());
        portText.setText(String.valueOf(cloud.getPort()));


    }

    public void modeSelect(View view){

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ConnectActivity.this);
        builderSingle.setTitle("Select Mode");

        final ArrayAdapter<Cloud.Mode> arrayAdapter = new ArrayAdapter<Cloud.Mode>(ConnectActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(Cloud.Mode.Auto);
        arrayAdapter.add(Cloud.Mode.Manual);
        arrayAdapter.add(Cloud.Mode.Clear);
        arrayAdapter.add(Cloud.Mode.BlueSky);
        arrayAdapter.add(Cloud.Mode.WhiteClouds);
        arrayAdapter.add(Cloud.Mode.Overcast);
        arrayAdapter.add(Cloud.Mode.Sunset);
        arrayAdapter.add(Cloud.Mode.Rain);
        arrayAdapter.add(Cloud.Mode.Cloudy);
//        arrayAdapter.add(Cloud.Mode.Disco);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Cloud.Mode cloudMode = arrayAdapter.getItem(which);
                cloud.setMode(cloudMode.ordinal());
                AlertDialog.Builder builderInner = new AlertDialog.Builder(ConnectActivity.this);

                builderInner.setMessage(cloudMode.toString());
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,int which) {

                        String sUrl = "http:/" + cloud.getIp() + ":" + cloud.getPort() + "/setting";

                        PostTask postTask = new PostTask(cloud, sUrl);
                        try {
                            String response = postTask.execute().get();
                            Log.i("mode", "response " + response);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();

    }

    public void colorSelect(View view){

        int initColor = -16776961; // default to blue

        if (cloud.getColor() != 0){
            initColor = cloud.getColor();
        }

        ColorPickerDialogBuilder
//                .with(getApplicationContext())
                .with(this)
                .setTitle("Choose color")
                .initialColor(initColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

//                        toast.("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                        Log.i("color", "on color seleced " + selectedColor + " red " + Color.red(selectedColor) + " green " + Color.green(selectedColor) + " blue " + Color.blue(selectedColor) );

                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
//                        changeBackgroundColor(selectedColor);
                        Log.i("color", "selected color " + selectedColor);

                        cloud.setMode(1); // set mode to manual
                        cloud.setColor(selectedColor);
                        String sUrl = "http:/" + cloud.getIp() + ":" + cloud.getPort() + "/setting";
                        PostTask postTask = new PostTask(cloud, sUrl);
                        try {
                            String response = postTask.execute().get();
                            Log.i("mode", "response " + response);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void disconnect(View view){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}

// task to send GET request to cloud webserver
class PostTask extends AsyncTask<Void, Integer, String> {

    Cloud cloud = null;
    String deviceUrl = null;

    PostTask(Cloud cloud, String url){
        this.cloud = cloud;
        this.deviceUrl = url;
    }

    protected String doInBackground(Void... voids) {
        try {
//            URL url = new URL(urls[0]);
            URL url = new URL(deviceUrl);
//            Log.i("request", "url " + url.toString());
            Log.i("request", "url " + deviceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("mode", cloud.getMode());
            jsonParam.put("color", cloud.getColor());

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            conn.disconnect();

            return null;

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
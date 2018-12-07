package cse.a535.group10.brainnet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    public static final int PICK_CSV = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }

        Button idButton = findViewById(R.id.idButton);
        idButton.setOnClickListener(new View.OnClickListener() {
            /* when clicked, the ID button on the main page will prompt the user to select EEG data
             in .csv format from their SD card to test the model */
            public void onClick(View v) {
                // allow user to log in and show stimulus image for X seconds
                login();
            }
        });
    }

    protected void login() {
        Login myDialog = new Login();
        myDialog.show(getSupportFragmentManager(), "test");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CSV && resultCode == RESULT_OK) {
            if (data == null) {
                dataAlert("Error Loading Data");
            } else {
                System.out.println("Data Selected");
                String csvDataPath = data.getData().getPath();
                String [] pathParts = csvDataPath.split("/");
                Bundle extras = this.getIntent().getExtras();
                if (extras != null) {
                    csvDataPath = extras.getString("storage") + pathParts[pathParts.length - 1];
                }
                System.out.println(csvDataPath);
                if (csvDataPath.endsWith(".csv") && new File(csvDataPath).exists()) {
                    getBatteryStatus();
                    final long start = System.currentTimeMillis();
                    new UploadUtils(new OnEventListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result) {
                                getBatteryStatus();
                                long end = System.currentTimeMillis();
                                long total = end - start;
                                Log.d("Time", "Total time taken (ms): " + total);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            dataAlert(e.getMessage());
                        }
                    }).execute(csvDataPath);
                } else {
                    dataAlert("Not .CSV File");
                }
            }
        }
    }

    /* get battery details */
    private void getBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;
            Log.d("batter level", String.valueOf(batteryPct));
        }
    }

    /* method to create alert pop-up when data folder is not found */
    protected void dataAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}


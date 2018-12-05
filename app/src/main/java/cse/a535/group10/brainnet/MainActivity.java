package cse.a535.group10.brainnet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
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

    /* method to create alert pop-up when data folder is not found */
    protected void dataAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    /* user selects csv to test and is sent to server */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                System.out.println("Error Loading Data");
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

        final Button idButton = findViewById(R.id.idButton);
        idButton.setOnClickListener(new View.OnClickListener() {
            /* when clicked, the ID button on the main page will prompt the user to select EEG data
             in .csv format from their SD card to test the model */
            public void onClick(View v) {
                File directory;
                String storagePath;
                // if no SD Card, check if data folder is already on phone
                if (Environment.getExternalStorageState() == null) {
                    System.out.println("No SD Card Found");
                    storagePath = Environment.getDataDirectory() + "/eegmmidb/";
                    System.out.println(storagePath);
                    directory = new File(storagePath);
                    if (directory.exists()) {
                        Uri uri = Uri.parse(storagePath);
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(uri, "*/*");
                        intent.putExtra("storage", storagePath);
                        setIntent(intent);
                        startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                    } else
                        dataAlert("EEG Data Folder Not Found");
                } else {
                    System.out.println("SD Card Found");
                    storagePath = Environment.getExternalStorageDirectory().getPath() + "/eegmmidb/";
                    System.out.println(storagePath);
                    directory = new File(storagePath);
                    if (directory.exists()) {
                        Uri uri = Uri.parse(storagePath);
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(uri, "*/*");
                        intent.putExtra("storage", storagePath);
                        setIntent(intent);
                        startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                    } else
                        dataAlert("EEG Data Folder Not Found");
                }
            }
        });
    }

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
}


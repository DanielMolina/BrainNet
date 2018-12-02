package cse.a535.group10.brainnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity
{
    public static final int PICK_CSV = 0;

    /* method to create alert pop-up when data folder is not found*/
    protected void dataAlert(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(msg);
        builder.setCancelable(true);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV && resultCode == Activity.RESULT_OK)
        {
            if (data == null)
            {
                //Display an error
                System.out.println("Error Loading Data");
                dataAlert("Error Loading Data");
            }
            else
            {
                System.out.println("Data Selected");
                String csvDataPath = data.getDataString();

                if(csvDataPath.endsWith(".csv"))
                {
                    // TODO: load and send data to server
                    // TODO: keep track of battery change from before loading data until before displaying results, and latency
                    // TODO: might need to make separate activity or view (not sure) for results display page
                }
                else
                {
                    dataAlert("Not .CSV File");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button idButton = findViewById(R.id.idButton);
        idButton.setOnClickListener(new View.OnClickListener()
        {
            /* when clicked, the ID button on the main page will prompt the user to select EEG data
             in .csv format from their SD card to test the model */
            public void onClick(View v)
            {
                File directory;
                String storagePath;

                // if no SD Card, check if data folder is already on phone
                if(Environment.getExternalStorageState() == null)
                {
                    System.out.println("No SD Card Found");
                    storagePath = Environment.getDataDirectory()+"/eegmmidb/";
                    directory = new File(storagePath);
                    if(directory.exists())
                    {
                        Uri uri = Uri.parse(storagePath);
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(uri, "*/*");
                        startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                    }
                    else
                        dataAlert("EEG Data Folder Not Found");
                }
                else
                {
                    System.out.println("SD Card Found");
                    storagePath = Environment.getExternalStorageDirectory().getPath()+"/eegmmidb/";
                    directory = new File(storagePath);
                    if(directory.exists())
                    {
                        Uri uri = Uri.parse(storagePath);
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(uri, "*/*");
                        startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                    }
                    else
                        dataAlert("EEG Data Folder Not Found");
                }
            }
        });
    }
}


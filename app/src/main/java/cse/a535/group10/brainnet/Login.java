package cse.a535.group10.brainnet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.io.File;

public class Login extends DialogFragment {
    public static final int PICK_CSV = 0;

    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Input Login ID");
        builder.setCancelable(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_signin, null);
        final EditText edit = (EditText) alertView.findViewById(R.id.username); // return empty string

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(alertView)
                // Add action buttons
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userID = edit.getText().toString();
                        if (userID.equals("S007")) {
                            // display stimulus while "recording" eeg
                            final Dialog settingsDialog = new Dialog(getContext());
                            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                            settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    settingsDialog.cancel();
                                }
                            });
                            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.popup_image, null));
                            settingsDialog.show();

                            // check phone for "recorded" brain waves
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
                                    getActivity().setIntent(intent);
                                    getActivity().startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                                } else {
                                    dataAlert("EEG Data Folder Not Found");
                                }
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
                                    getActivity().setIntent(intent);
                                    getActivity().startActivityForResult(Intent.createChooser(intent, "Open"), PICK_CSV);
                                } else {
                                    dataAlert("EEG Data Folder Not Found");
                                }
                            }
                        } else {
                            dataAlert("Incorrect ID");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    /* method to create alert pop-up when data folder is not found */
    protected void dataAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

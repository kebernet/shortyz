package com.totsp.crossword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpDownloadActivity extends Activity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1001;

    private File crosswordsFolder = new File(Environment.getExternalStorageDirectory(), "crosswords");

    /**
    * Copies the data from an InputStream object to an OutputStream object.
    *
    * @param sourceStream
    *            The input stream to be read.
    * @param destinationStream
    *            The output stream to be written to.
    * @return int value of the number of bytes copied.
    * @exception IOException
    *                from java.io calls.
    */
    public static int copyStream(InputStream sourceStream, OutputStream destinationStream)
        throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[1024];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }

        destinationStream.flush();
        destinationStream.close();

        return totalBytes;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Allow Permissions")
                        .setMessage("Please allow writing to storage when prompted. Shortyz needs this permission to store downloaded crossword files and cannot work without it.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(HttpDownloadActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_EXTERNAL_STORAGE);
            }

            return;
        }

        initializeDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeDownload();
                }
        }
    }

    private void initializeDownload() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            showSDCardHelp();
            finish();

            return;
        }

        Uri u = this.getIntent()
                    .getData();
        String filename = u.toString();
        filename = filename.substring(filename.lastIndexOf('/') + 1);

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading...\n" + filename);
        dialog.setCancelable(false);

        OkHttpClient client = new OkHttpClient();

        try {
            Request request = new Request.Builder()
                    .url(u.toString())
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                throw new IOException("Non 200 downloading...");
            }

            InputStream is = response.body().byteStream();
            File puzFile = new File(crosswordsFolder, filename);
            FileOutputStream fos = new FileOutputStream(puzFile);
            copyStream(is, fos);
            fos.close();

            Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this, PlayActivity.class);
            this.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();

            Toast t = Toast.makeText(this, "Unable to download from\n" + u.toString(), Toast.LENGTH_LONG);
            t.show();
        }

        finish();
    }

    private void showSDCardHelp() {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/sdcard.html"), this,
                HTMLActivity.class);
        this.startActivity(i);
    }
}

package com.totsp.crossword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;


public class HttpDownloadActivity extends Activity {
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

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();

        try {
            request.setURI(new URI(u.toString()));

            HttpResponse response = client.execute(request);

            if (response.getStatusLine()
                            .getStatusCode() != 200) {
                throw new IOException("Non 200 downloading...");
            }

            InputStream is = response.getEntity()
                                     .getContent();
            File puzFile = new File(crosswordsFolder, filename);
            FileOutputStream fos = new FileOutputStream(puzFile);
            copyStream(is, fos);
            fos.close();

            Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this, PlayActivity.class);
            this.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();

            Toast t = Toast.makeText(this, "Unabled to download from\n" + u.toString(), Toast.LENGTH_LONG);
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

package com.totsp.crossword;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.totsp.crossword.versions.AndroidVersionUtils;

public class ShortyzActivity extends BaseGameActivity {
	protected AndroidVersionUtils utils = AndroidVersionUtils.Factory
			.getInstance();
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}
		StatFs stats = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());

		if ( (long) stats.getAvailableBlocks() * (long) stats.getBlockSize() < 1024L * 1024L) {
			showSDCardFull();
			finish();

			return;
		}
		doOrientation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}
		doOrientation();
	}

	protected void showSDCardFull() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("file:///android_asset/sdcard-full.html"), this,
				HTMLActivity.class);
		this.startActivity(i);
	}

	protected void showSDCardHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("file:///android_asset/sdcard.html"), this,
				HTMLActivity.class);
		this.startActivity(i);
	}

	private void doOrientation() {
		if ("PORT".equals(prefs.getString("orientationLock", "UNLOCKED"))) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if ("LAND"
				.equals(prefs.getString("orientationLock", "UNLOCKED"))) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

    public void onSignInFailed() {
        Toast.makeText(this, "Not signed in.", Toast.LENGTH_SHORT);
    }

    public void onSignInSucceeded() {
        Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT);
        if(this.mHelper != null && !this.mHelper.getGamesClient().isConnected()){
            this.mHelper.getGamesClient().connect();
        }
    }

    protected Bitmap createBitmap(String fontFile, String character){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int size = (int)(48 * metrics.density);
        Bitmap bitmap = Bitmap.createBitmap( size , size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.createFromAsset(getAssets(), fontFile));
        p.setTextSize(size / 2);
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(character, size/2, size / 2 +size / 5, p );
        return bitmap;

    }
}

package com.totsp.crossword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.util.Log;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.totsp.crossword.util.NightModeHelper;
import com.totsp.crossword.versions.AndroidVersionUtils;

import java.lang.reflect.Field;

public class ShortyzActivity extends AppCompatActivity {
	private static final String TAG =  ShortyzActivity.class.getSimpleName();
	protected AndroidVersionUtils utils = AndroidVersionUtils.Factory
			.getInstance();
	protected SharedPreferences prefs;
	public NightModeHelper nightMode;

	protected GoogleSignInClient mGoogleSignInClient;
	private static final int RC_ACHIEVEMENT_UI = 9003;
	protected LeaderboardsClient mLeaderboardsClient;
	protected AchievementsClient mAchievementsClient;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGoogleSignInClient = GoogleSignIn.getClient(this,
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        if(this.nightMode == null) {
            this.nightMode = NightModeHelper.bind(this);
            this.utils.restoreNightMode(this);

        }

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

	protected void showMenuAlways(){
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        utils.restoreNightMode(this);
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
		try {
			if ("PORT".equals(prefs.getString("orientationLock", "UNLOCKED"))) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if ("LAND"
					.equals(prefs.getString("orientationLock", "UNLOCKED"))) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
		} catch(RuntimeException e) {

		}
	}

	protected void showAchievements() {
		Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.getAchievementsIntent()
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						startActivityForResult(intent, RC_ACHIEVEMENT_UI);
					}
				});
	}

	protected boolean isSignedIn() {
		return GoogleSignIn.getLastSignedInAccount(this) != null;
	}

	private void onConnected(GoogleSignInAccount googleSignInAccount) {
		Log.d(TAG, "onConnected(): connected to Google APIs");

		this.mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
		this.mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
		this.onSignInSucceeded();
	}

	protected void onSignInSucceeded(){

	}

	private void onDisconnected() {
		Log.d(TAG, "onDisconnected()");

		mAchievementsClient = null;
		mLeaderboardsClient = null;
	}

	protected void signInSilently() {
		Log.d(TAG, "signInSilently()");

		mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
				new OnCompleteListener<GoogleSignInAccount>() {
					@Override
					public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInSilently(): success");
							onConnected(task.getResult());
						} else {
							Log.d(TAG, "signInSilently(): failure", task.getException());
							onDisconnected();
						}
					}
				});
	}

    protected Bitmap createBitmap(String fontFile, String character){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int dpi = Math.round(160F * metrics.density);
        int size = dpi / 2;
		Bitmap bitmap = Bitmap.createBitmap(size , size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.createFromAsset(getAssets(), fontFile));
        p.setTextSize(size);
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(character, size/2, size - size / 9, p );
        return bitmap;

    }
}

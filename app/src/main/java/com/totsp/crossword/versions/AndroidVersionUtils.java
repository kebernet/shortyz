package com.totsp.crossword.versions;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.totsp.crossword.ShortyzActivity;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.File;
import java.net.URL;
import java.util.Map;

public interface AndroidVersionUtils {
	
	void storeMetas(Uri uri, PuzzleMeta meta);

	void setContext(Context ctx);

	boolean downloadFile(URL url, File destination,
						 Map<String, String> headers, boolean notification, String title);

	void finishOnHomeButton(AppCompatActivity a);

	void holographic(AppCompatActivity playActivity);

	void onActionBarWithText(MenuItem a);

	void onActionBarWithText(SubMenu reveal);

	void restoreNightMode(ShortyzActivity shortyzActivity);

	class Factory {
		private static AndroidVersionUtils INSTANCE;
		
		public static AndroidVersionUtils getInstance() {
			if(INSTANCE != null){
				return INSTANCE;
			}
			System.out.println("Creating utils for version: "
					+ android.os.Build.VERSION.SDK_INT);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				System.out.println("Using Oreo");
				return INSTANCE = new OreoUtil();
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				System.out.println("Using Lollipop");
				return INSTANCE = new LollipopUtil();
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				System.out.println("Using Honeycomb");
				return INSTANCE = new HoneycombUtil();
			} else {
				System.out.println("Using Gingerbread");
				return INSTANCE = new GingerbreadUtil();
			}
		}
	}

	View onActionBarCustom(AppCompatActivity a, int id);
	
	void hideWindowTitle(AppCompatActivity a);
	
	void hideActionBar(AppCompatActivity a);

    void onActionBarWithoutText(MenuItem a);

    void hideTitleOnPortrait(AppCompatActivity a);

	void toggleNightMode(ShortyzActivity activity);

	boolean isNightModeAvailable();

	// This has a dependency on JobScheduler which is only available in SDK version 21.
	//
	// TODO: It might be possible to replicate this functionality on older versions using
	// AlarmManager.
	boolean isBackgroundDownloadAvaliable();

	// Checks whether a background download may have updated the available puzzles, requiring a
	// UI refresh.
	boolean checkBackgroundDownload(SharedPreferences prefs, boolean hasWritePermissions);

	void clearBackgroundDownload(SharedPreferences prefs);

	void createNotificationChannel(Context context);
}

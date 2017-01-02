package com.totsp.crossword.versions;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.totsp.crossword.puz.PuzzleMeta;

import java.io.File;
import java.net.URL;
import java.util.Map;

public interface AndroidVersionUtils {
	
	void storeMetas(Uri uri, PuzzleMeta meta);

	void setContext(Context ctx);

	boolean downloadFile(URL url, File destination,
						 Map<String, String> headers, boolean notification, String title);

	void finishOnHomeButton(ActionBarActivity a);

	void holographic(ActionBarActivity playActivity);

	void onActionBarWithText(MenuItem a);

	void onActionBarWithText(SubMenu reveal);

	class Factory {
		private static AndroidVersionUtils INSTANCE;
		
		public static AndroidVersionUtils getInstance() {
			if(INSTANCE != null){
				return INSTANCE;
			}
			System.out.println("Creating utils for version: "
					+ android.os.Build.VERSION.SDK_INT);

			switch (android.os.Build.VERSION.SDK_INT) {
			case 10:
			case 9:
				System.out.println("Using Gingerbread.");
				return INSTANCE = new GingerbreadUtil();
			default:
				return INSTANCE = new HoneycombUtil();
			}
		}
	}

	View onActionBarCustom(ActionBarActivity a, int id);
	
	void hideWindowTitle(ActionBarActivity a);
	
	void hideActionBar(ActionBarActivity a);

    void onActionBarWithoutText(MenuItem a);

    void hideTitleOnPortrait(ActionBarActivity a);

	void toggleNightMode(Activity activity);

	boolean isNightModeAvailable();

	
}

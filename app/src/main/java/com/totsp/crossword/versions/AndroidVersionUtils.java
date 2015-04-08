package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.totsp.crossword.puz.PuzzleMeta;

public interface AndroidVersionUtils {
	
	public void storeMetas(Uri uri, PuzzleMeta meta);

	public void setContext(Context ctx);

	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title);

	public void finishOnHomeButton(ActionBarActivity a);

	public void holographic(ActionBarActivity playActivity);

	public void onActionBarWithText(MenuItem a);

	public void onActionBarWithText(SubMenu reveal);

	public static class Factory {
		private static AndroidVersionUtils INSTANCE;
		
		public static AndroidVersionUtils getInstance() {
			if(INSTANCE != null){
				return INSTANCE;
			}
			System.out.println("Creating utils for version: "
					+ android.os.Build.VERSION.SDK_INT);


			try {
				if(android.os.Build.VERSION.SDK_INT >= 16){
					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.JellyBeanUtil")
							.newInstance();
				}
				switch (android.os.Build.VERSION.SDK_INT) {
				case 10:
				case 9:
					System.out.println("Using Gingerbread.");
					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.GingerbreadUtil")
							.newInstance();
				case 12:
				case 13:
				case 14:
				case 15:

					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.HoneycombUtil")
							.newInstance();
				default:
					return INSTANCE = new DefaultUtil();
				}
			} catch (Exception e) {
				return INSTANCE = new DefaultUtil();
			}
		}
	}

	public View onActionBarCustom(ActionBarActivity a, int id);
	
	public void hideWindowTitle(ActionBarActivity a);
	
	public void hideActionBar(ActionBarActivity a);

    public void onActionBarWithoutText(MenuItem a);

    public void hideTitleOnPortrait(ActionBarActivity a);

	
}

package com.totsp.crossword.versions;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.totsp.crossword.ShortyzActivity;
import com.totsp.crossword.net.DownloadReceiver;
import com.totsp.crossword.puz.PuzzleMeta;


public class GingerbreadUtil extends DefaultUtil {
    protected Context ctx;

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public void finishOnHomeButton(AppCompatActivity a) {
    }

    public void holographic(AppCompatActivity playActivity) {
    }

    public void onActionBarWithText(MenuItem a) {
    }

    public void onActionBarWithText(SubMenu reveal) {
    }

    @Override
    public void restoreNightMode(ShortyzActivity shortyzActivity) {

    }

    public void storeMetas(Uri uri, PuzzleMeta meta) {
		DownloadReceiver.metas.put(uri, meta);
		
	}
	
	public void hideWindowTitle(AppCompatActivity a) {
		a.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void hideActionBar(AppCompatActivity a) {
		; //no op;
	}

    public void onActionBarWithoutText(MenuItem a) {
        ; //no op
    }

    public void hideTitleOnPortrait(AppCompatActivity a) {

    }

    @Override
    public boolean isBackgroundDownloadAvaliable() {
        return false;
    }

    @Override
    public boolean checkBackgroundDownload(SharedPreferences prefs, boolean hasWritePermissions) {
        return false;
    }

    @Override
    public void clearBackgroundDownload(SharedPreferences prefs) {

    }

    @Override
    public void createNotificationChannel(Context context) {

    }

    @Override
    public void toggleNightMode(ShortyzActivity activity) {

    }

    @Override
    public boolean isNightModeAvailable() {
        return false;
    }

    public View onActionBarCustom(AppCompatActivity a, int id) {
        System.out.println("Setting custom ActionBar view");
        ActionBar bar = a.getSupportActionBar();
        if(bar == null){
            return null;
        }
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayShowHomeEnabled(true);
        bar.setCustomView(id);
        return bar.getCustomView();
    }

}

package com.totsp.crossword.versions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;

import com.totsp.crossword.ShortyzActivity;
import com.totsp.crossword.net.DownloadReceiver;
import com.totsp.crossword.puz.PuzzleMeta;


@TargetApi(11)
public class HoneycombUtil extends DefaultUtil {
	
	{
		System.out.println("Honeycomb Utils.");
	}


    protected Context ctx;

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public void storeMetas(Uri uri, PuzzleMeta meta) {
        DownloadReceiver.metas.put(uri, meta);

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
    public void finishOnHomeButton(final AppCompatActivity a) {
		ActionBar bar = a.getSupportActionBar();
		if(bar == null){
			return;
		}
		bar.setDisplayHomeAsUpEnabled(true);
		View home = a.findViewById(android.R.id.home);
        if(home != null){
	        home.setOnClickListener(new OnClickListener() {
	                public void onClick(View arg0) {
	                    a.finish();
	                }
	            });
        }
    }

    @TargetApi(11)
	@Override
    public void holographic(AppCompatActivity a) {
        ActionBar bar = a.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean isNightModeAvailable(){
		return true;
	}

	@Override
    public void toggleNightMode(ShortyzActivity activity){
		activity.nightMode.toggle();
        if(activity.nightMode.isNightMode()){
            activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
	}

	@Override
	public void restoreNightMode(ShortyzActivity shortyzActivity) {
        if(shortyzActivity.nightMode.isNightMode()){
            shortyzActivity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            shortyzActivity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

	@Override
    public void onActionBarWithText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void onActionBarWithoutText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void hideTitleOnPortrait(AppCompatActivity a) {
        if(a.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT){
            return;
        }
        ActionBar bar = a.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }
    }

    public void onActionBarWithText(SubMenu a) {
        this.onActionBarWithText(a.getItem());
    }

    public View onActionBarCustom(AppCompatActivity a, int id) {
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
    
    public void hideWindowTitle(AppCompatActivity a) {
    	// no op;
    }

	public void hideActionBar(AppCompatActivity a) {
		ActionBar ab = a.getSupportActionBar();
		if(ab == null){
			return;
		}
		ab.hide();
	}
    
}

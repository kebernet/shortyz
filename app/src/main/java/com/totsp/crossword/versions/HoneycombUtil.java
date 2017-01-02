package com.totsp.crossword.versions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;

import com.totsp.crossword.util.NightModeHelper;


@TargetApi(11)
public class HoneycombUtil extends GingerbreadUtil {
	
	{
		System.out.println("Honeycomb Utils.");
	}
	
	@Override
    public void finishOnHomeButton(final ActionBarActivity a) {
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
    public void holographic(ActionBarActivity a) {
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
    public void toggleNightMode(Activity activity){
		NightModeHelper.bind(activity).toggle();
	}

    @Override
    public void onActionBarWithText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void onActionBarWithoutText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void hideTitleOnPortrait(ActionBarActivity a) {
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

    public View onActionBarCustom(ActionBarActivity a, int id) {
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
    
    public void hideWindowTitle(ActionBarActivity a) {
    	// no op;
    }

	public void hideActionBar(ActionBarActivity a) {
		ActionBar ab = a.getSupportActionBar();
		if(ab == null){
			return;
		}
		ab.hide();
	}
    
}

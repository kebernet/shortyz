package com.totsp.crossword.versions;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;

import com.totsp.crossword.net.DownloadReceiver;
import com.totsp.crossword.puz.PuzzleMeta;


public class GingerbreadUtil extends DefaultUtil {
    protected Context ctx;

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public void finishOnHomeButton(ActionBarActivity a) {
    }

    public void holographic(ActionBarActivity playActivity) {
    }

    public void onActionBarWithText(MenuItem a) {
    }

    public void onActionBarWithText(SubMenu reveal) {
    }

	public void storeMetas(Uri uri, PuzzleMeta meta) {
		DownloadReceiver.metas.put(uri, meta);
		
	}
	
	public void hideWindowTitle(ActionBarActivity a) {
		a.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void hideActionBar(ActionBarActivity a) {
		; //no op;
	}

    public void onActionBarWithoutText(MenuItem a) {
        ; //no op
    }

    public void hideTitleOnPortrait(ActionBarActivity a) {

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

}

package com.totsp.crossword.versions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;

import com.totsp.crossword.net.AbstractDownloader;
import com.totsp.crossword.puz.PuzzleMeta;

public class DefaultUtil implements AndroidVersionUtils {
	public void setContext(Context ctx) {
		// TODO Auto-generated method stub
	}

	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient
				.getParams()
				.setParameter(
						"User-Agent",
						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");

		HttpGet httpget = new HttpGet(url.toString());
		for(Entry<String, String> e : headers.entrySet()){
			httpget.setHeader(e.getKey(), e.getValue());
		}
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			FileOutputStream fos = new FileOutputStream(destination);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			AbstractDownloader.copyStream(entity.getContent(), baos);
			if(url.toExternalForm().indexOf("crnet") != -1){
				System.out.println(new String(baos.toByteArray()));
			}
			AbstractDownloader.copyStream(new ByteArrayInputStream(baos.toByteArray()), fos);
			fos.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	public void finishOnHomeButton(ActionBarActivity a) {
		// no op
	}

	public void holographic(ActionBarActivity playActivity) {
		// no op
	}

	public void onActionBarWithText(MenuItem a) {
		// no op
	}

	public void onActionBarWithText(SubMenu reveal) {
		// no op
	}

	public void storeMetas(Uri uri, PuzzleMeta meta) {
		// no op
	}

	public View onActionBarCustom(ActionBarActivity a, int id) {
		return null;
	}

	public void hideWindowTitle(ActionBarActivity a) {
		a.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void hideActionBar(ActionBarActivity a) {
		; //no op;
	}

    public void onActionBarWithoutText(MenuItem a) {
        ; //no op;
    }

    public void hideTitleOnPortrait(ActionBarActivity a) {
        ; //no op;
    }
}

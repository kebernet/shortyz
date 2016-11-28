package com.totsp.crossword.versions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.PuzzleMeta;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class DefaultUtil implements AndroidVersionUtils {
	public abstract void setContext(Context ctx);

	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title) {

		OkHttpClient httpclient = new OkHttpClient();

		Request.Builder requestBuilder = new Request.Builder()
				.url(url.toString());

		for (Entry<String, String> e : headers.entrySet()){
			requestBuilder.header(e.getKey(), e.getValue());
		}

		try {
			Response response = httpclient.newCall(requestBuilder.build()).execute();

			FileOutputStream fos = new FileOutputStream(destination);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			IO.copyStream(response.body().byteStream(), baos);

			if (url.toExternalForm().contains("crnet")){
				System.out.println(new String(baos.toByteArray()));
			}

			IO.copyStream(new ByteArrayInputStream(baos.toByteArray()), fos);
			fos.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	public abstract void finishOnHomeButton(ActionBarActivity a);

	public abstract void holographic(ActionBarActivity playActivity);

	public abstract void onActionBarWithText(MenuItem a);

	public abstract void onActionBarWithText(SubMenu reveal);

	public abstract void storeMetas(Uri uri, PuzzleMeta meta);

	public abstract View onActionBarCustom(ActionBarActivity a, int id);

	public void hideWindowTitle(ActionBarActivity a) {
		a.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public abstract void hideActionBar(ActionBarActivity a);

    public abstract void onActionBarWithoutText(MenuItem a);

    public abstract void hideTitleOnPortrait(ActionBarActivity a);
}

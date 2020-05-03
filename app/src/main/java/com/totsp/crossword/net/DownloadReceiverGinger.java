package com.totsp.crossword.net;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;

import java.io.File;
import java.net.URI;

public class DownloadReceiverGinger extends BroadcastReceiver {

	@Override
    @SuppressLint("NewApi")
	public void onReceive(Context ctx, Intent intent) {
		DownloadManager mgr = (DownloadManager) ctx
				.getSystemService(Context.DOWNLOAD_SERVICE);
		long id = intent.getLongExtra("extra_download_id", -1);

		if (!"application/x-crossword".equals(
				mgr.getMimeTypeForDownloadedFile(id))) {
			return;
		}
		Uri uri = null;
		Cursor c = null;
		try {
			Query q = new Query();
			q.setFilterById(id);
			q.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);

			c = mgr.query(q);

			c.moveToFirst();
			String uriString = c.getString(c
					.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
			System.out.println("uriString: " + uriString);
			uri = Uri.parse(uriString);
			c.close();
			if (uri == null || !uri.toString().endsWith(".puz")) {
				return;
			}
		} catch (CursorIndexOutOfBoundsException e) {
			c.close();
			return;
		}
		System.out.println("===RECEIVED: " + uri);

		try {
			Downloaders.processDownloadedPuzzle(new File(
					new URI(uri.toString())), DownloadReceiver.metas.remove(uri));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

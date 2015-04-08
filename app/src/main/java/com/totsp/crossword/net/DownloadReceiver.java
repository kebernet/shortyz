package com.totsp.crossword.net;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.totsp.crossword.puz.PuzzleMeta;

public class DownloadReceiver extends BroadcastReceiver {
	
	public static HashMap<Uri, PuzzleMeta> metas = new HashMap<Uri, PuzzleMeta>();

	
	private BroadcastReceiver impl; 
	{
		if(android.os.Build.VERSION.SDK_INT >= 9){
			try{
				BroadcastReceiver built = (BroadcastReceiver) Class.forName("com.totsp.crossword.net.DownloadReceiverGinger").newInstance();
				impl = built;
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		if(impl == null){
			impl = new DownloadReceiverNoop();
		}
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		impl.onReceive(ctx, intent);
	}
}

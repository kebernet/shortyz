package com.totsp.crossword.versions;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.totsp.crossword.shortyz.ShortyzApplication;

@TargetApi(Build.VERSION_CODES.O)
public class OreoUtil extends LollipopUtil {

    @Override
    public void createNotificationChannel(Context context) {
        Log.i(OreoUtil.class.getSimpleName(), "Creating notification channel");
        CharSequence name = "Downloads";
        String description = "Notifications about downloaded puzzles";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(ShortyzApplication.PUZZLE_DOWNLOAD_CHANNEL_ID
                , name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }
}

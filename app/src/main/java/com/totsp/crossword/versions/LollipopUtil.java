package com.totsp.crossword.versions;

import android.content.SharedPreferences;

import com.totsp.crossword.service.BackgroundDownloadService;

public class LollipopUtil extends HoneycombUtil {

    @Override
    public boolean isBackgroundDownloadAvaliable() {
        return true;
    }

    @Override
    public boolean checkBackgroundDownload(SharedPreferences prefs, boolean hasWritePermissions) {
        if (!hasWritePermissions) {
            return false;
        }

        boolean isPending =
                prefs.getBoolean(BackgroundDownloadService.DOWNLOAD_PENDING_PREFERENCE, false);

        clearBackgroundDownload(prefs);

        return isPending;
    }

    @Override
    public void clearBackgroundDownload(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(BackgroundDownloadService.DOWNLOAD_PENDING_PREFERENCE, false)
                .apply();
    }
}

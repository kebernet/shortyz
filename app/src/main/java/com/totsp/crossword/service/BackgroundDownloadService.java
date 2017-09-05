package com.totsp.crossword.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.totsp.crossword.net.Downloaders;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

// Currently only available on API version >=21 due to use of JobScheduler.
// It may be possible to implement this functionality using AlarmManager for lower SDK versions.
@TargetApi(21)
public class BackgroundDownloadService extends JobService {
    public static final String DOWNLOAD_PENDING_PREFERENCE = "backgroundDlPending";

    private static final Logger LOGGER =
            Logger.getLogger(BackgroundDownloadService.class.getCanonicalName());

    private static JobInfo getJobInfo(boolean requireUnmetered, boolean allowRoaming,
                                      boolean requireCharging) {
        JobInfo.Builder builder = new JobInfo.Builder(
                JobSchedulerId.BACKGROUND_DOWNLOAD.id(),
                new ComponentName("com.totsp.crossword.shortyz",
                        BackgroundDownloadService.class.getName()));

        builder.setPeriodic(TimeUnit.HOURS.toMillis(1))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresCharging(requireCharging)
                .setPersisted(true);

        if (!requireUnmetered) {
            if (allowRoaming) {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            } else {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
            }
        }

        return builder.build();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        LOGGER.info("Starting background download task");
        DownloadTask downloadTask = new DownloadTask(this);
        downloadTask.execute(job);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public static void updateJob(Context context) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        boolean enable = preferences.getBoolean("backgroundDownload", false);

        if (enable) {
            scheduleJob(context);
        } else {
            cancelJob(context);
        }
    }

    private static void scheduleJob(Context context) {
        JobScheduler scheduler =
                (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        JobInfo info = getJobInfo(
                preferences.getBoolean("backgroundDownloadRequireUnmetered", true),
                preferences.getBoolean("backgroundDownloadAllowRoaming", false),
                preferences.getBoolean("backgroundDownloadRequireCharging", false));


        LOGGER.info("Scheduling background download job: " + info);

        int result = scheduler.schedule(info);

        if (result == JobScheduler.RESULT_SUCCESS) {
            LOGGER.info("Successfully scheduled background downloads");
        } else {
            LOGGER.log(Level.WARNING, "Unable to schedule background downloads");
        }
    }

    private static void cancelJob(Context context) {
        LOGGER.info("Unscheduling background downloads");
        JobScheduler scheduler =
                (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JobSchedulerId.BACKGROUND_DOWNLOAD.id());
    }

    private static class DownloadTask extends AsyncTask<JobParameters, Void, JobParameters> {
        private final JobService jobService;

        public DownloadTask(JobService jobService) {
            this.jobService = jobService;
        }

        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            Context context = jobService.getApplicationContext();

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                LOGGER.info("Skipping download, no write permission");
                return params[0];
            }

            LOGGER.info("Downloading most recent puzzles");
            if(Looper.myLooper() == null) {
                Looper.prepare();
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final Downloaders dls = new Downloaders(prefs, nm, context, false);
            dls.downloadLatestIfNewerThanDate(new Date(), null);

            // This is used to tell BrowseActivity that puzzles may have been updated while
            // paused.
            prefs.edit()
                .putBoolean(DOWNLOAD_PENDING_PREFERENCE, true)
                .apply();

            return params[0];
        }

        protected void onPostExecute(JobParameters params) {
            jobService.jobFinished(params, false);
        }
    }
}

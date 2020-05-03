package com.totsp.crossword.net;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.totsp.crossword.BrowseActivity;
import com.totsp.crossword.PlayActivity;
import com.totsp.crossword.gmail.GmailDownloader;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.shortyz.ShortyzApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private Context context;
    private List<Downloader> downloaders = new LinkedList<Downloader>();
    private NotificationManager notificationManager;
    private boolean supressMessages;

    public Downloaders(SharedPreferences prefs,
                       NotificationManager notificationManager,
                       Context context) {
        this(prefs, notificationManager, context, true);
    }


    // Set isInteractive to true if this class can ask for user interaction when needed (e.g. to
    // refresh NYT credentials), false if otherwise.
    public Downloaders(SharedPreferences prefs,
                       NotificationManager notificationManager,
                       Context context,
                       boolean challengeForCredentials) {
        this.notificationManager = notificationManager;
        this.context = context;

//        if (prefs.getBoolean("downloadGlobe", true)) {
//            downloaders.add(new OldBostonGlobeDownloader());
//        }
//
//        if (prefs.getBoolean("downloadThinks", true)) {
//            downloaders.add(new ThinksDownloader());
//        }
//        if (prefs.getBoolean("downloadWaPo", true)) {
//         downloaders.add(new WaPoDownloader());
//         }
        
        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJFridayDownloader());
            downloaders.add(new WSJSaturdayDownloader());
        }

        if (prefs.getBoolean("downloadWaPoPuzzler", true)) {
            downloaders.add(new WaPoPuzzlerDownloader());
        }

//        if (prefs.getBoolean("downloadNYTClassic", true)) {
//            downloaders.add(new NYTClassicDownloader());
//        }

//        if (prefs.getBoolean("downloadInkwell", true)) {
//            downloaders.add(new InkwellDownloader());
//        }

        if (prefs.getBoolean("downloadJonesin", true)) {
            downloaders.add(new JonesinDownloader());
        }

        if (prefs.getBoolean("downloadLat", true)) {
//           downloaders.add(new UclickDownloader("tmcal", "Los Angeles Times", "Rich Norris", Downloader.DATE_NO_SUNDAY));
            downloaders.add(new LATimesDownloader());
        }

//        if (prefs.getBoolean("downloadAvClub", true)) {
//            downloaders.add(new AVClubDownloader());
//        }

//        if (prefs.getBoolean("downloadPhilly", true)) {
//            downloaders.add(new PhillyDownloader());
//        }

        if (prefs.getBoolean("downloadCHE", true)) {
            downloaders.add(new CHEDownloader());
        }

        if (prefs.getBoolean("downloadJoseph", true)) {
            downloaders.add(new KFSDownloader("joseph", "Joseph Crosswords",
                    "Thomas Joseph", Downloader.DATE_NO_SUNDAY));
        }

        if (prefs.getBoolean("downloadSheffer", true)) {
            downloaders.add(new KFSDownloader("sheffer", "Sheffer Crosswords",
                    "Eugene Sheffer", Downloader.DATE_NO_SUNDAY));
        }

//        if (prefs.getBoolean("downloadPremier", true)) {
//            downloaders.add(new KFSDownloader("premier", "Premier Crosswords",
//                    "Frank Longo", Downloader.DATE_SUNDAY));
//        }

        if (prefs.getBoolean("downloadNewsday", true)) {
            downloaders.add(new BrainsOnlyDownloader(
                    "http://brainsonly.com/servlets-newsday-crossword/newsdaycrossword?date=",
                    "Newsday"));
        }

        if (prefs.getBoolean("downloadUSAToday", true)) {
            downloaders.add(new UclickDownloader("usaon", "USA Today",
                    "USA Today", Downloader.DATE_NO_SUNDAY));
        }

        if (prefs.getBoolean("downloadUniversal", true)) {
            downloaders.add(new UclickDownloader("fcx", "Universal Crossword",
                    "uclick LLC", Downloader.DATE_DAILY));
        }

        if (prefs.getBoolean("downloadLACal", true)) {
            downloaders.add(new LATSundayDownloader());
        }

//        if (prefs.getBoolean("downloadISwear", true)) {
//            downloaders.add(new ISwearDownloader());
//        }
        
        
        if (prefs.getBoolean("downloadNYT", false)) {
            NYTDownloader nyt;
            if (challengeForCredentials) {
                nyt = new NYTDownloader(context);
                nyt.requestCredentialsIfNeeded();
            } else {
                nyt = new NYTDownloader(context, notificationManager);
            }
            downloaders.add(nyt);

        }

        ShortyzApplication application = (ShortyzApplication) context.getApplicationContext();
        System.out.println("Doing GMAIL: " + application.getGmailService() != null);
        if(application.getGmailService() != null){
            downloaders.add(new GmailDownloader(application.getGmailService()));
        }


        this.supressMessages = prefs.getBoolean("supressMessages", false);
    }


    private static Date clearTimeInDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public List<Downloader> getDownloaders(Date date) {
        date = clearTimeInDate(date);
        int dayOfWeek = date.getDay();
        List<Downloader> retVal = new LinkedList<Downloader>();

        for (Downloader d : downloaders) {
            // TODO: Downloader.getGoodThrough() should account for the day of week.
            if (Arrays.binarySearch(d.getDownloadDates(), dayOfWeek) >= 0) {
                if(date.getTime() >= d.getGoodFrom().getTime() && date.getTime() <= d.getGoodThrough().getTime()) {
                    retVal.add(d);
                }
            }
        }

        return retVal;
    }

    public void download(Date date) {
        download(date, getDownloaders(date));
    }

    // Downloads the latest puzzles newer/equal to than the given date for the given set of
    // downloaders.
    //
    // If downloaders is null, then the full list of downloaders will be used.
    public void downloadLatestIfNewerThanDate(Date oldestDate, List<Downloader> downloaders) {
        oldestDate = clearTimeInDate(oldestDate);

        if (downloaders == null) {
            downloaders = new ArrayList<Downloader>();
        }

        if (downloaders.size() == 0) {
            downloaders.addAll(this.downloaders);
        }

        HashMap<Downloader, Date> puzzlesToDownload = new HashMap<Downloader, Date>();
        for (Downloader d : downloaders) {
            Date goodThrough = clearTimeInDate(d.getGoodThrough());
            int goodThroughDayOfWeek = goodThrough.getDay();
            if ((Arrays.binarySearch(d.getDownloadDates(), goodThroughDayOfWeek) >= 0) &&
                    goodThrough.getTime() >= oldestDate.getTime()) {
                LOG.info("Will try to download puzzle " + d + " @ " + goodThrough);
                puzzlesToDownload.put(d, goodThrough);
            }
        }

        if (!puzzlesToDownload.isEmpty()) {
            download(puzzlesToDownload);
        }
    }

    public void download(Date date, List<Downloader> downloaders) {
        date = clearTimeInDate(date);

        if ((downloaders == null) || (downloaders.size() == 0)) {
            downloaders = getDownloaders(date);
        }

        HashMap<Downloader, Date> puzzlesToDownload = new HashMap<Downloader, Date>();
        for (Downloader d : downloaders) {
            puzzlesToDownload.put(d, date);
        }

        download(puzzlesToDownload);
    }

    private void download(Map<Downloader, Date> puzzlesToDownload) {
        String contentTitle = "Downloading Puzzles";

        NotificationCompat.Builder not =
                new NotificationCompat.Builder(context, ShortyzApplication.PUZZLE_DOWNLOAD_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(contentTitle)
                        .setWhen(System.currentTimeMillis());

        boolean somethingDownloaded = false;
        File crosswords = new File(Environment.getExternalStorageDirectory(), "crosswords/");
        File archive = new File(Environment.getExternalStorageDirectory(), "crosswords/archive/");
        crosswords.mkdirs();

        if (crosswords.listFiles() != null) {
            for (File isDel : crosswords.listFiles()) {
                if (isDel.getName()
                        .endsWith(".tmp")) {
                    isDel.delete();
                }
            }
        }

        HashSet<File> newlyDownloaded = new HashSet<File>();

        int nextNotificationId = 1;
        for (Map.Entry<Downloader, Date> puzzle : puzzlesToDownload.entrySet()) {
            File downloaded = downloadPuzzle(puzzle.getKey(),
                    puzzle.getValue(),
                    not,
                    nextNotificationId++,
                    crosswords,
                    archive);
            if (downloaded != null) {
                somethingDownloaded = true;
                newlyDownloaded.add(downloaded);
            }

        }

        if (this.notificationManager != null) {
            this.notificationManager.cancel(0);
        }

        if (somethingDownloaded) {
            this.postDownloadedGeneral();
        }
    }

    private File downloadPuzzle(Downloader d,
                                Date date,
                                NotificationCompat.Builder not,
                                int notificationId,
                                File crosswords,
                                File archive) {
        LOG.info("Downloading " + d.toString());
        d.setContext(context);

        try {
            String contentText = "Downloading from " + d.getName();
            Intent notificationIntent = new Intent(context, PlayActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            not.setContentText(contentText).setContentIntent(contentIntent);

            File downloaded = new File(crosswords, d.createFileName(date));
            File archived = new File(archive, d.createFileName(date));

            System.out.println(downloaded.getAbsolutePath() + " " + downloaded.exists() + " OR " +
                    archived.getAbsolutePath() + " " + archived.exists());

            if (!d.alwaysRun() && (downloaded.exists() || archived.exists())) {
                System.out.println("==Skipping " + d.toString());
                return null;
            }

            if (!this.supressMessages && this.notificationManager != null) {
                this.notificationManager.notify(0, not.build());
            }

            downloaded = d.download(date);

            if (downloaded == Downloader.DEFERRED_FILE) {
                return null;
            }

            if (downloaded != null) {
                boolean updatable = false;
                PuzzleMeta meta = new PuzzleMeta();
                meta.date = date;
                meta.source = d.getName();
                meta.sourceUrl = d.sourceUrl(date);
                meta.updatable = updatable;

                if (processDownloadedPuzzle(downloaded, meta)) {
                    if (!this.supressMessages) {
                        this.postDownloadedNotification(notificationId, d.getName(), downloaded);
                    }

                    return downloaded;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to download "+d.getName(), e);
            return null;
        }
        return null;
    }

    public static boolean processDownloadedPuzzle(File downloaded,
            PuzzleMeta meta) {
        try {
            System.out.println("==PROCESSING " + downloaded + " hasmeta: "
                    + (meta != null));

            Puzzle puz = IO.load(downloaded);
            if(puz == null){
                return false;
            }
            puz.setDate(meta.date);
            puz.setSource(meta.source);
            puz.setSourceUrl(meta.sourceUrl);
            puz.setUpdatable(meta.updatable);

            IO.save(puz, downloaded);

            return true;
        } catch (Exception ioe) {
            LOG.log(Level.WARNING, "Exception reading " + downloaded, ioe);
            downloaded.delete();

            return false;
        }
    }

    public void supressMessages(boolean b) {
        this.supressMessages = b;
    }

    private void postDownloadedGeneral() {
        String contentTitle = "Downloaded new puzzles!";

        Intent notificationIntent = new Intent(Intent.ACTION_EDIT, null,
                context, BrowseActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        Notification not = new NotificationCompat.Builder(context, ShortyzApplication.PUZZLE_DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(contentTitle)
                .setContentText("New puzzles were downloaded.")
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .build();

        if (this.notificationManager != null) {
            this.notificationManager.notify(0, not);
        }
    }

    private void postDownloadedNotification(int i, String name, File puzFile) {
        String contentTitle = "Downloaded " + name;

        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
                Uri.fromFile(puzFile), context, PlayActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        Notification not = new NotificationCompat.Builder(context, ShortyzApplication.PUZZLE_DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(contentTitle)
                .setContentText(puzFile.getName())
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .build();

        if (this.notificationManager != null) {
            this.notificationManager.notify(i, not);
        }
    }
}

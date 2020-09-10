package com.totsp.crossword.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.net.Uri;

import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.versions.AndroidVersionUtils;
import com.totsp.crossword.versions.DefaultUtil;


public abstract class AbstractDownloader implements Downloader {
    protected static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    public static File DOWNLOAD_DIR = ShortyzApplication.CROSSWORDS;
    @SuppressWarnings("unchecked")
	protected static final Map<String, String> EMPTY_MAP = Collections.EMPTY_MAP;
    protected File downloadDirectory;
    protected String baseUrl;
    protected final AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();
    private String downloaderName;
    protected File tempFolder;
    protected Date now = new Date();

    protected AbstractDownloader(String baseUrl, File downloadDirectory, String downloaderName) {
        this.baseUrl = baseUrl;
        this.downloadDirectory = downloadDirectory;
        this.downloaderName = downloaderName;
        this.tempFolder = new File(downloadDirectory, "temp");
        this.tempFolder.mkdirs();
    }

    public void setContext(Context ctx) {
        this.utils.setContext(ctx);
    }

    public String createFileName(Date date) {
        return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-" +
        this.downloaderName.replaceAll(" ", "") + ".puz";
    }

    public String sourceUrl(Date date) {
        return this.baseUrl + this.createUrlSuffix(date);
    }

    public String toString() {
        return getName();
    }

    protected abstract String createUrlSuffix(Date date);

    protected File download(Date date, String urlSuffix, Map<String, String> headers){
    	System.out.println("DL From ASD");
    	return download(date, urlSuffix, headers, true);
    }
    
    protected File download(Date date, String urlSuffix, Map<String, String> headers, boolean canDefer) {
        LOG.info("Mkdirs: " + this.downloadDirectory.mkdirs());
        LOG.info("Exist: " + this.downloadDirectory.exists());

        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            System.out.println(url);

            File f = new File(downloadDirectory, this.createFileName(date));
            PuzzleMeta meta = new PuzzleMeta();
            meta.date = date;
            meta.source = getName();
            meta.sourceUrl = url.toString();
            meta.updatable = false;
            
            utils.storeMetas(Uri.fromFile(f), meta);
            if( canDefer ){
	            if (utils.downloadFile(url, f, headers, true, this.getName())) {
	                DownloadReceiver.metas.remove(Uri.fromFile(f));
	
	                return f;
	            } else {
	                return Downloader.DEFERRED_FILE;
	            }
            } else {
            	AndroidVersionUtils.Factory.getInstance().downloadFile(url, f, headers, true, this.getName());
            	return f;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected File download(Date date, String urlSuffix) {
        return download(date, urlSuffix, EMPTY_MAP);
    }


    protected File downloadToTempFile(String fullName, Date date) {
        File downloaded = new File(downloadDirectory, this.createFileName(date));

        try {
            URL url = new URL(this.baseUrl + this.createUrlSuffix(date));
            LOG.log(Level.INFO, fullName +" "+url.toExternalForm());
            AndroidVersionUtils.Factory.getInstance().downloadFile(url, downloaded, EMPTY_MAP, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            downloaded.delete();
            downloaded = null;
        }

        if (downloaded == null) {
            LOG.log(Level.SEVERE, "Unable to download plain text KFS file.");

            return null;
        }

        System.out.println("Text file: " + downloaded);

        try {
            File tmpFile =  new File(this.tempFolder, "txt-tmp"+System.currentTimeMillis()+".txt"); //File.createTempFile("kfs-temp", "txt");
            downloaded.renameTo(tmpFile);
            LOG.log(Level.INFO, "Downloaded to text file "+tmpFile);
            return tmpFile;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to move KFS file to temporary location.");

            return null;
        }
    }

    @Override
    public boolean alwaysRun(){
        return false;
    }

    // By default, assume that "today's" puzzle is available at 12AM in the local time zone.
    //
    // This is not correct, as whether a puzzle is available at a given instant should not be
    // dependent on the user's local time zone.
    //
    // TODO: Consider reusing some of that logic in NYTDownloader for other sources with well known
    // publishing times.
    public Date getGoodThrough(){
        return now;
    }

    public Date getGoodFrom(){
        return new Date(0L);
    }
}

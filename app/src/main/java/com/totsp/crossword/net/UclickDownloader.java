package com.totsp.crossword.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.logging.Level;

import com.totsp.crossword.io.UclickXMLIO;
import com.totsp.crossword.versions.DefaultUtil;


/**
 * Uclick XML Puzzles
 * URL: http://picayune.uclick.com/comics/[puzzle]/data/[puzzle]YYMMDD-data.xml
 * crnet (Newsday) = Daily
 * usaon (USA Today) = Monday-Saturday (not holidays)
 * fcx (Universal) = Daily
 * lacal (LA Times Sunday Calendar) = Sunday
 */
public class UclickDownloader extends AbstractDownloader {
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    NumberFormat nf = NumberFormat.getInstance();
    private String copyright;
    private String fullName;
    private String shortName;
    private int[] days;

    public UclickDownloader(String shortName, String fullName, String copyright, int[] days) {
        super("http://picayune.uclick.com/comics/" + shortName + "/data/", DOWNLOAD_DIR, fullName);
        this.shortName = shortName;
        this.fullName = fullName;
        this.copyright = copyright;
        this.days = days;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return days;
    }

    public String getName() {
        return fullName;
    }

    public File download(Date date) {
        File downloadTo = new File(this.downloadDirectory, this.createFileName(date));

        if (downloadTo.exists()) {
            return null;
        }

        File plainText = downloadToTempFile(date);

        if (plainText == null) {
            return null;
        }

        try {
        	LOG.log(Level.INFO, "TMP FILE "+plainText.getAbsolutePath());
            InputStream is = new FileInputStream(plainText);
            DataOutputStream os = new DataOutputStream(new FileOutputStream(downloadTo));   
            boolean retVal = UclickXMLIO.convertUclickPuzzle(is, os,
                    "\u00a9 " + (date.getYear() + 1900) + " " + copyright, date);
            os.close();
            is.close();
            plainText.delete();

            if (!retVal) {
                LOG.log(Level.SEVERE, "Unable to convert uclick XML puzzle into Across Lite format.");
                downloadTo.delete();
                downloadTo = null;
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Exception converting uclick XML puzzle into Across Lite format.", ioe);
            downloadTo.delete();
            downloadTo = null;
        }

        return downloadTo;
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return this.shortName + nf.format(date.getYear() - 100) + nf.format(date.getMonth() + 1) +
        nf.format(date.getDate()) + "-data.xml";
    }

    private File downloadToTempFile(Date date) {
        DefaultUtil util = new DefaultUtil();
        File f = new File(downloadDirectory, this.createFileName(date));

        try {
            URL url = new URL(this.baseUrl + this.createUrlSuffix(date));
            LOG.log(Level.INFO, this.fullName+" "+url.toExternalForm());
            util.downloadFile(url, f, EMPTY_MAP, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            f = null;
        }

        if (f == null) {
            LOG.log(Level.SEVERE, "Unable to download uclick XML file.");

            return null;
        }

        try {
        	
            File tmpFile = new File(this.tempFolder, "uclick-temp"+System.currentTimeMillis()+".xml");
            f.renameTo(tmpFile);

            return tmpFile;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to move uclick XML file to temporary location.");

            return null;
        }
    }
}

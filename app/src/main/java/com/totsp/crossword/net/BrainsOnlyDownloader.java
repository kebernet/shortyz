package com.totsp.crossword.net;

import com.totsp.crossword.io.BrainsOnlyIO;
import com.totsp.crossword.io.KingFeaturesPlaintextIO;
import com.totsp.crossword.io.UclickXMLIO;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by keber_000 on 2/9/14.
 */
public class BrainsOnlyDownloader extends AbstractDownloader {

    private final SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
    private final String fullName;

    public BrainsOnlyDownloader(String baseUrl, String fullName) {
        super(baseUrl, DOWNLOAD_DIR, fullName);
        this.fullName = fullName;
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return df.format(date);
    }

    public int[] getDownloadDates() {
        return Downloader.DATE_DAILY;
    }

    public String getName() {
        return this.fullName;
    }

    public File download(Date date) {
        File downloadTo = new File(this.downloadDirectory, this.createFileName(date));

        if (downloadTo.exists()) {
            return null;
        }

        File plainText = downloadToTempFile(this.getName(), date);

        if (plainText == null) {
            return null;
        }

        try {
            LOG.log(Level.INFO, "Reading from "+plainText);
            InputStream is = new FileInputStream(plainText);
            DataOutputStream os = new DataOutputStream(new FileOutputStream(downloadTo));
            boolean retVal = BrainsOnlyIO.convertBrainsOnly(is, os, date);
            os.close();
            is.close();
            plainText.delete();
            LOG.log(Level.INFO, "Saved to "+downloadTo);
            if (!retVal) {
                LOG.log(Level.SEVERE, "Unable to convert KFS puzzle into Across Lite format.");
                downloadTo.delete();
                downloadTo = null;
            }
        } catch (Exception ioe) {
            LOG.log(Level.SEVERE, "Exception converting KFS puzzle into Across Lite format.", ioe);
            downloadTo.delete();
            downloadTo = null;
        }

        return downloadTo;
    }

}

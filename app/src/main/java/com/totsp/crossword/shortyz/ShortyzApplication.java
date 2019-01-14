package com.totsp.crossword.shortyz;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.totsp.crossword.gmail.GMConstants;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.versions.AndroidVersionUtils;
import com.totsp.crossword.view.PlayboardRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.fabric.sdk.android.Fabric;
import okhttp3.CookieJar;

public class ShortyzApplication extends Application {

	public static String PUZZLE_DOWNLOAD_CHANNEL_ID = "shortyz.downloads";
    private static ShortyzApplication INSTANCE;
	private Playboard board;
	private PlayboardRenderer renderer;
	public static File DEBUG_DIR;
	public static File CROSSWORDS = new File(
			Environment.getExternalStorageDirectory(), "crosswords");
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	private GoogleAccountCredential credential;
	private Gmail gmailService;
	private SharedPreferences settings;
	private AtomicReference<PersistentCookieJar> cookieJar = new AtomicReference<>(null);

	public void setBoard(Playboard board){
	    this.board = board;
    }

    public Playboard getBoard() {
         return board;
    }

    public void setRenderer(PlayboardRenderer renderer){
	    this.renderer = renderer;
    }

    public PlayboardRenderer getRenderer() {
        return renderer;
    }

    @Override
	public void onCreate() {
		INSTANCE = this;
		// Initialize credentials and service object.
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		updateCredential(settings);
		super.onCreate();

		AndroidVersionUtils.Factory.getInstance().createNotificationChannel(this);

		Fabric.with(this, new Crashlytics());

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			IO.TEMP_FOLDER = new File(CROSSWORDS, "temp");
			if(!IO.TEMP_FOLDER.mkdirs()){
				return;
			}
			DEBUG_DIR = new File(CROSSWORDS, "debug");
			if(!DEBUG_DIR.mkdirs()){
				return;
			}
			File info = new File(DEBUG_DIR, "device");
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new FileWriter(info));
				writer.println("VERSION INT: "
						+ android.os.Build.VERSION.SDK_INT);
				writer.println("VERSION STRING: "
						+ android.os.Build.VERSION.SDK);
				writer.println("VERSION RELEASE: "
						+ android.os.Build.VERSION.RELEASE);
				writer.println("MODEL: " + android.os.Build.DEVICE);
				writer.println("DISPLAY: " + android.os.Build.DISPLAY);
				writer.println("MANUFACTURER: " + android.os.Build.MANUFACTURER);
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if(writer != null){
					writer.close();
				}
			}
		}
	}


	public static Intent sendDebug() {
		File zip = new File(CROSSWORDS, "debug.stz");
		File debug = new File(CROSSWORDS, "debug");
		if (zip.exists()) {
			zip.delete();
		}
		ZipOutputStream zos = null;
		if (debug.exists())
			try {
				zos = new ZipOutputStream(new FileOutputStream(
						zip));
				zipDir(debug.getAbsolutePath(), zos);
				zos.close();
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { "kebernet@gmail.com" });
				sendIntent.putExtra(Intent.EXTRA_SUBJECT,
						"Shortyz Debug Package");
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zip));
				sendIntent.setType("application/octet-stream");
				return sendIntent;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				if(zos != null){
					try {
						zos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		return null;
	}

	public static void zipDir(String dir2zip, ZipOutputStream zos) {
		FileInputStream fis = null;
		try {
			File zipDir = new File(dir2zip);
			String[] dirList = zipDir.list();
			byte[] readBuffer = new byte[2156];
			int bytesIn = 0;
			for (int i = 0; i < dirList.length; i++) {
				File f = new File(zipDir, dirList[i]);
				if (f.isDirectory()) {
					String filePath = f.getPath();
					zipDir(filePath, zos);
					continue;
				}
				try {
					fis = new FileInputStream(f);

					ZipEntry anEntry = new ZipEntry(f.getPath());
					zos.putNextEntry(anEntry);
					while ((bytesIn = fis.read(readBuffer)) != -1) {
						zos.write(readBuffer, 0, bytesIn);
					}
				} finally {
					if(fis != null){
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isLandscape(DisplayMetrics metrics){
		return metrics.widthPixels > metrics.heightPixels;
	}

	public static boolean isTabletish(DisplayMetrics metrics) {
            double x = Math.pow(metrics.widthPixels / metrics.xdpi, 2);
            double y = Math.pow(metrics.heightPixels / metrics.ydpi, 2);
            double screenInches = Math.sqrt(x + y);
            if (screenInches > 9) { // look for a 9" or larger screen.
                return true;
            } else {
                return false;
            }
	}
	
	public static boolean isMiniTabletish(DisplayMetrics metrics) {
		switch (android.os.Build.VERSION.SDK_INT) {
		case 14:
		case 15:
		case 16:
			double x = Math.pow(metrics.widthPixels/metrics.xdpi,2);
		    double y = Math.pow(metrics.heightPixels/metrics.ydpi,2);
		    double screenInches = Math.sqrt(x+y);
			if (screenInches > 5.5 && screenInches <= 9) {
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}
	}

	public Gmail getGmailService(){
		return gmailService;
	}

	public GoogleAccountCredential getCredential() {
		return credential;
	}

	public SharedPreferences getSettings() {
		return settings;
	}

	public void updateCredential(SharedPreferences prefs){
		credential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(GMConstants.SCOPES))
				.setBackOff(new ExponentialBackOff())
				.setSelectedAccountName(prefs.getString(GMConstants.PREF_ACCOUNT_NAME, null));
		if(credential != null && credential.getSelectedAccount() != null) {
			gmailService = new com.google.api.services.gmail.Gmail.Builder(
					transport, jsonFactory, credential)
					.setApplicationName("Shortyz")
					.build();
		} else {
			gmailService = null;
		}
	}

	public CookieJar getCookieJar(){
		if(this.cookieJar.get() == null){
			this.cookieJar.compareAndSet(null,
			new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this)));
		}
		return this.cookieJar.get();
	}

	public static ShortyzApplication getInstance(){
		return INSTANCE;
	}
}

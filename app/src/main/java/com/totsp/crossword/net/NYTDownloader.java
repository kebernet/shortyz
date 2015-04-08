package com.totsp.crossword.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;

/**
 * New York Times URL: http://select.nytimes.com/premium/xword/[Mon]DDYY.puz
 * Date = Daily
 */
public class NYTDownloader extends AbstractDownloader {
	private static final String[] MONTHS = new String[] { "Jan", "Feb", "Mar",
			"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	public static final String NAME = "New York Times";
	private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=http://select.nytimes.com/premium/xword/puzzles.html";
	NumberFormat nf = NumberFormat.getInstance();
	private Context context;
	private Handler handler = new Handler();
	private HashMap<String, String> params = new HashMap<String, String>();

	protected NYTDownloader(Context context, String username, String password) {
		super("http://www.nytimes.com/svc/crosswords/v2/puzzle/", DOWNLOAD_DIR, NAME);
		this.context = context;
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
		params.put("is_continue", "true");
		params.put("SAVEOPTION", "YES");
		params.put("URI",
				"http://select.nytimes.com/premium/xword/puzzles.html");
		params.put("OQ", "");
		params.put("OP", "");
		params.put("userid", username);
		params.put("password", password);
		params.put("USERID", username);
		params.put("PASSWORD", password);
	}

	public int[] getDownloadDates() {
		return DATE_DAILY;
	}

	public String getName() {
		return NYTDownloader.NAME;
	}

	public File download(Date date) {
		return this.download(date, this.createUrlSuffix(date));
	}

	public File update(File source) {
		try {
			Puzzle oPuz = IO.load(source);

			if (!oPuz.isUpdatable()) {
				return null;
			}

			System.out.println("Source URL:" + oPuz.getSourceUrl());

			URL url = new URL(oPuz.getSourceUrl());
			HttpClient client = this.login();

			HttpGet get = new HttpGet(url.toString());
			HttpResponse response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == 200) {
				File f = File.createTempFile(
						"update" + System.currentTimeMillis(), ".tmp");
				f.deleteOnExit();

				FileOutputStream fos = new FileOutputStream(f);
				AbstractDownloader.copyStream(
						response.getEntity().getContent(), fos);
				fos.close();

				Puzzle nPuz = IO.load(f);
				System.out.println("Temp puzzle loaded. " + nPuz.getTitle());

				boolean updated = false;

				for (int x = 0; x < oPuz.getBoxes().length; x++) {
					for (int y = 0; y < oPuz.getBoxes()[x].length; y++) {
						Box oBox = oPuz.getBoxes()[x][y];
						Box nBox = nPuz.getBoxes()[x][y];

						if ((oBox != null) && (nBox != null)) {
							System.out.println(oBox.getSolution() + "="
									+ nBox.getSolution());

							if (oBox.getSolution() != nBox.getSolution()) {
								oBox.setSolution(nBox.getSolution());
								updated = true;
							}
						}
					}
				}

				f.delete();

				if (updated) {
					System.out.println("Saving puzzle as updated.");
					oPuz.setUpdatable(false);
					IO.save(oPuz, source);
				} else {
					return null;
				}

				return source;
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected String createUrlSuffix(Date date) {
		return "daily-"+(date.getYear() + 1900) + "-"
				+ this.nf.format(date.getMonth() + 1) + "-"
				+ this.nf.format(date.getDate()) + ".puz";
	}

	@Override
	protected File download(Date date, String urlSuffix) {
		try {
			URL url = new URL(this.baseUrl + urlSuffix);
			HttpClient client = this.login();

			HttpGet fetchIndex = new HttpGet(
					"http://select.nytimes.com/premium/xword/puzzles.html");
			HttpResponse indexResponse = client.execute(fetchIndex);
			AbstractDownloader.copyStream(indexResponse.getEntity()
					.getContent(),
					new FileOutputStream(downloadDirectory.getAbsolutePath()
							+ "/debug/xword-puzzles.html"));

			HttpGet get = new HttpGet(url.toString());
			get.addHeader("Referer",
					"http://select.nytimes.com/premium/xword/puzzles.html");

			HttpResponse response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == 200) {
				File f = new File(downloadDirectory, this.createFileName(date));
				FileOutputStream fos = new FileOutputStream(f);
				AbstractDownloader.copyStream(
						response.getEntity().getContent(), fos);
				fos.close();

				AbstractDownloader.copyStream(
						new FileInputStream(f),
						new FileOutputStream(downloadDirectory
								.getAbsolutePath() + "/debug/debug.puz"));

				return f;
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private HttpClient login() throws IOException {

        DefaultHttpClient httpclient = null;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);


            SSLSocketFactory sf = new TrustAllSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            httpclient = new DefaultHttpClient(ccm, params);

        } catch (Exception e) {
           throw new IOException("Failed to set up ssl", e);
        }

		httpclient
				.getParams()
				.setParameter(
						"User-Agent",
						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");


        HttpGet httpget = new HttpGet(LOGIN_URL);

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		System.out.println("Login form get: " + response.getStatusLine());

		if (entity != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.writeTo(baos);

			String resp = new String(baos.toByteArray());
			String tok = "name=\"token\" value=\"";
			String expires = "name=\"expires\" value=\"";
			int tokIndex = resp.indexOf(tok);

			if (tokIndex != -1) {
				params.put(
						"token",
						resp.substring(tokIndex + tok.length(),
								resp.indexOf("\"", tokIndex + tok.length())));
				System.out.println("Got token: " + params.get("token"));
			}

			int expiresIndex = resp.indexOf(expires);

			if (expiresIndex != -1) {
				params.put(
						"expires",
						resp.substring(
								expiresIndex + expires.length(),
								resp.indexOf("\"",
										expiresIndex + expires.length())));
				System.out.println("Got expires: " + params.get("expires"));
			}
		}

		System.out.println("Initial set of cookies:");

		List<Cookie> cookies = httpclient.getCookieStore().getCookies();

		if (cookies.isEmpty()) {
			System.out.println("None");
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				System.out.println("- " + cookies.get(i).toString());
			}
		}

		HttpPost httpost = new HttpPost(LOGIN_URL);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		for (Entry<String, String> e : this.params.entrySet()) {
			nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
			System.out.println(e.getKey() + "=" + e.getValue());
		}

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		response = httpclient.execute(httpost);
		entity = response.getEntity();

		System.out.println("Login form get: " + response.getStatusLine());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (entity != null) {
			entity.writeTo(baos);

			new File(this.downloadDirectory, "debug/").mkdirs();
			copyStream(
					new ByteArrayInputStream(baos.toByteArray()),
					new FileOutputStream(this.downloadDirectory
							.getAbsolutePath() + "/debug/authresp.html"));

			String resp = new String(baos.toByteArray());

			if (resp.indexOf("Log in to manage") != -1) {
				System.out.println("=================== Password error\n"
						+ resp);
				this.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(
								context,
								"New York Times login failure. Is your password correct?",
								Toast.LENGTH_LONG).show();
					}
				});

				return null;
			}
		}

		System.out.println("Post logon cookies:");
		cookies = httpclient.getCookieStore().getCookies();

		if (cookies.isEmpty()) {
			System.out.println("None");
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				System.out.println("- " + cookies.get(i).toString());
			}
		}

		return httpclient;
	}
}

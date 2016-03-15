package com.totsp.crossword.net;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

	public NYTDownloader(Context context, String username, String password) {
		super("http://www.nytimes.com/svc/crosswords/v2/puzzle/", DOWNLOAD_DIR, NAME);
		this.context = context;
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
		params.put("is_continue", "true");
		params.put("SAVEOPTION", "YES");
		params.put("URI",
				"http://www.nytimes.com/crosswords/index.html");
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
			OkHttpClient client = this.login();

			Request get = new Request.Builder().url(url).get().build();
			Response response = client.newCall(get).execute();

			if (response.code() == 200) {
				File f = File.createTempFile(
						"update" + System.currentTimeMillis(), ".tmp");
				f.deleteOnExit();

				FileOutputStream fos = new FileOutputStream(f);
				IO.copyStream(
						response.body().byteStream(), fos);
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
                http://www.nytimes.com/crosswords/index.html
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
			OkHttpClient client = this.login();

			Request request = new Request.Builder()
					.url(url)
					.header("Referer", "http://www.nytimes.com/crosswords/index.html")
					.build();


			Response response = client.newCall(request).execute();

			if (response.code() == 200) {
				File f = new File(downloadDirectory, this.createFileName(date));
				FileOutputStream fos = new FileOutputStream(f);
				IO.copyStream(
						response.body().byteStream(), fos);
				fos.close();

				IO.copyStream(
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

	public OkHttpClient login() throws IOException {

        OkHttpClient httpclient =  new OkHttpClient.Builder()
				.cookieJar(new CookieJar() {
                    List<Cookie> cookies = new ArrayList<Cookie>();
					@Override
					public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
						this.cookies.addAll(cookies);
					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl url) {
						return cookies;
					}
				})
				.addInterceptor(new  Interceptor() {

			@Override
			public Response intercept(Chain chain) throws IOException {
				Request originalRequest = chain.request();
				Request requestWithUserAgent = originalRequest.newBuilder()
						.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36")
						.build();
				return chain.proceed(requestWithUserAgent);
			}
		})
				.build();

		Request request = new Request.Builder()
				.url(LOGIN_URL)
				.get()
				.build();


		Response response = httpclient.newCall(request).execute();


		System.out.println("Login form get: " + response.code());

		if (response.code() == 200 && response.body() != null) {

			String resp = response.body().string();
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

		FormBody.Builder requestBuilder = new FormBody.Builder();
		for (Entry<String, String> e : this.params.entrySet()) {
			requestBuilder = requestBuilder.add(e.getKey(), e.getValue());
		}


		Request httpost = new Request.Builder().url(LOGIN_URL)
				.post(requestBuilder.build())
				.build();

		response = httpclient.newCall(httpost).execute();

		if (response.body() != null) {
            String resp = response.body().string();
			new File(this.downloadDirectory, "debug/").mkdirs();
			IO.copyStream(
					new ByteArrayInputStream(resp.getBytes()),
					new FileOutputStream(this.downloadDirectory
							.getAbsolutePath() + "/debug/authresp.html"));


			if (resp.indexOf("The email and password provided do not match an account in our system.") != -1) {
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

		return httpclient;
	}
}

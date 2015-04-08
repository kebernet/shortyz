package com.totsp.crossword.versions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.totsp.crossword.net.AbstractDownloader;

public class JellyBeanUtil extends HoneycombUtil {
	
	
	@Override
	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient
				.getParams()
				.setParameter(
						"User-Agent",
						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");

		HttpGet httpget = new HttpGet(url.toString());
		for(Entry<String, String> e : headers.entrySet()){
			httpget.setHeader(e.getKey(), e.getValue());
		}
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			FileOutputStream fos = new FileOutputStream(destination);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			AbstractDownloader.copyStream(entity.getContent(), baos);
			if(url.toExternalForm().indexOf("crnet") != -1){
				System.out.println(new String(baos.toByteArray()));
			}
			AbstractDownloader.copyStream(new ByteArrayInputStream(baos.toByteArray()), fos);
			fos.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		// try {
		// HttpURLConnection connection = (HttpURLConnection)
		// url.openConnection();
		// connection.setDoOutput(true);
		// connection.setRequestMethod("GET");
		//
		// for (Entry<String, String> entry : headers.entrySet()) {
		// System.out.println(entry.getKey()+" "+entry.getValue());
		// connection.setRequestProperty(entry.getKey(), entry.getValue());
		// }
		//
		// System.out.println(url + "\n Response : " +
		// connection.getResponseCode());
		//
		// if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		// FileOutputStream fos = new FileOutputStream(destination);
		// AbstractDownloader.copyStream(connection.getInputStream(), fos);
		// fos.close();
		//
		// return true;
		// } else {
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// JPZIO.copyStream(connection.getInputStream(), baos);
		// System.out.println("CONTENT: "+ new String(baos.toByteArray()));
		// throw new RuntimeException();
		// }
		// } catch (IOException ioe) {
		// throw new RuntimeException(ioe);
		// }
	}

}

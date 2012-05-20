package com.shazam.practice.server.logic.download;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.shazam.practice.server.logic.TransferOperation;
import com.shazam.practice.server.utils.ServerProperties;
import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;

public class SoundCloudDownloader implements TransferOperation {

	private static Logger logger = Logger.getLogger(SoundCloudDownloader.class);

	private ApiWrapper apiWrapper;
	private final String uri;

	private long contentLength;
	private long downloadedBytes;

	public SoundCloudDownloader(String uri) {
		this.uri = uri;
	}

	public String execute() throws IOException, JSONException {
		initConnection();
		return download(uri);

	}

	public String download(String uri) throws IOException, JSONException {

		HttpResponse resp = apiWrapper.get(Request.to(uri));

		JSONObject o = new JSONObject(Http.getString(resp));

		final String moved = o.get("location").toString();

		return dlWhileCounting(moved);
	}

	public int getTransferPercentage() {

		// In case clients polls for download percentage before the download is
		// initialized
		try {
			int percentage = (int) ((downloadedBytes * 100) / contentLength);

			return percentage;
		} catch (java.lang.ArithmeticException e) {
			return 0;
		}
	}

	private class ProgressListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			downloadedBytes = ((DownloadCountingOutputStream) e.getSource())
					.getWrittenBytes();
		}
	}

	public String dlWhileCounting(String url) throws IOException {
		Random r = new Random();

		// not very effective, but don't want to import Apache RandomStrinUtils
		String fileName = r.nextInt(100000) + "_" + r.nextInt(100000) + "_"
				+ r.nextInt(100000) + ".mp3";

		ProgressListener progressListener = new ProgressListener();

		File fl = new File(getDLFolder() + fileName);
		URL dl = new URL(url);
		OutputStream os = new FileOutputStream(fl);
		InputStream is = dl.openStream();

		DownloadCountingOutputStream dcount = new DownloadCountingOutputStream(
				os);
		dcount.setListener(progressListener);

		contentLength = Long.valueOf(dl.openConnection().getHeaderField(
				"Content-Length"));

		logger.debug("Downloadble file size:" + contentLength);

		IOUtils.copy(is, dcount);

		os.close();
		is.close();

		return fileName;
	}

	private void initConnection() throws FileNotFoundException, IOException {
		Properties accountProperties = ServerProperties.get();

		if (accountProperties.getProperty("clientId") == null
				|| accountProperties.getProperty("clientSecret") == null
				|| accountProperties.getProperty("username") == null
				|| accountProperties.getProperty("password") == null) {
			throw new IllegalArgumentException(
					"Server properties doesn't contain the expected properties 'clientId', 'clientSecret', 'username' and 'password'.");
		}

		String clientId = accountProperties.getProperty("clientId");
		String clientSecret = accountProperties.getProperty("clientSecret");
		String username = accountProperties.getProperty("username");
		String password = accountProperties.getProperty("password");

		if (apiWrapper == null) {
			apiWrapper = new ApiWrapper(clientId, clientSecret, null, null,
					Env.LIVE);

			apiWrapper.login(username, password);
		}

	}

	private String getDLFolder() throws FileNotFoundException, IOException {
		Properties accountProperties = ServerProperties.get();

		if (accountProperties.getProperty("dlFolder") == null) {
			throw new IllegalArgumentException(
					"Server properties doesn't contain the expected properties 'dlFolder'.");
		}
		String folder = accountProperties.getProperty("dlFolder");

		if (folder.endsWith("/"))
			return folder;
		else
			return folder + "/";
	}

}

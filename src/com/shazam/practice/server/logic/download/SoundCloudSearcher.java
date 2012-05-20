package com.shazam.practice.server.logic.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import com.shazam.practice.server.logic.SearchOperation;
import com.shazam.practice.server.utils.ServerProperties;
import com.shazam.practice.shared.model.SongParam;
import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;

public class SoundCloudSearcher implements SearchOperation {

	ApiWrapper apiWrapper;
	private List<SongParam> results;

	public SoundCloudSearcher() {
	}

	public List<SongParam> getResults(String query) throws Exception {
		initConnection();

		results = new ArrayList<SongParam>();

		HttpResponse resp = apiWrapper.get(Request.to("/users/" + query
				+ "/tracks"));

		if (resp.getStatusLine().getStatusCode() != 200)
			throw new HttpException(resp.getStatusLine().toString());

		String responseBodyJson = Http.formatJSON(Http.getString(resp));

		JSONArray jsonArray = new JSONArray(responseBodyJson);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject current = (JSONObject) jsonArray.get(i);

			JSONObject userObject = (JSONObject) current.get("user");
			String username = userObject.getString("username").toString();

			String title = current.get("title").toString();

			boolean isDownloadable = current.get("downloadable").toString()
					.equals("true") ? true : false;

			String downloadURI = "";
			if (isDownloadable)
				downloadURI = current.get("download_url").toString();

			SongParam result = new SongParam(username, title, isDownloadable,
					downloadURI);

			results.add(result);
		}

		return results;
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
}

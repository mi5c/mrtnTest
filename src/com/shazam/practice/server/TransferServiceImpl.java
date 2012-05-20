package com.shazam.practice.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.shazam.practice.client.TransferService;
import com.shazam.practice.server.logic.TransferOperation;
import com.shazam.practice.server.logic.download.SoundCloudDownloader;
import com.shazam.practice.server.logic.download.SoundCloudSearcher;
import com.shazam.practice.server.logic.upload.AmazonS3Uploader;
import com.shazam.practice.shared.FieldVerifier;
import com.shazam.practice.shared.ServerSideException;
import com.shazam.practice.shared.model.SongParam;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class TransferServiceImpl extends RemoteServiceServlet implements
		TransferService {

	private static Logger logger = Logger.getLogger(TransferServiceImpl.class);

	private Map<Integer, TransferOperation> transfers = new HashMap<Integer, TransferOperation>();

	@Override
	public List<SongParam> getSongList(String name) throws ServerSideException {

		name = escapeHtml(name);

		if (!FieldVerifier.isValidName(name)) {
			throw new ServerSideException("Please enter at least one character");
		}

		logger.info("Getting SoundCloud songList for user: " + name);

		SoundCloudSearcher s = new SoundCloudSearcher();
		try {
			return s.getResults(name);
		} catch (HttpException e) {
			throw new ServerSideException(e.getMessage());
		} catch (Exception e) {
			logger.error(e);
			throw new ServerSideException(e.getMessage());
		}

	}

	@Override
	public String downloadSong(int transferId, String uri)
			throws ServerSideException {
		logger.info("Starting to download a song from uri: " + uri
				+ ", setting transferId: " + transferId);

		TransferOperation to = new SoundCloudDownloader(uri);
		transfers.put(transferId, to);
		try {
			return to.execute();
		} catch (Exception e) {
			logger.error(e);
			throw new ServerSideException(e.getClass() + ": " + e.getMessage());
		}
	}

	@Override
	public void uploadSong(int transferId, String fileName)
			throws ServerSideException {
		logger.info("Starting to upload a song : " + fileName
				+ ", setting transferId: " + transferId);

		TransferOperation to = new AmazonS3Uploader(fileName);
		transfers.put(transferId, to);
		try {
			to.execute();
		} catch (Exception e) {
			logger.error(e);
			throw new ServerSideException(e.getClass() + ": " + e.getMessage());
		}
	}

	@Override
	public int pollTransferStatus(int transferId) {

		logger.info("Poll request for transfer with id : " + transferId
				+ " received.");

		if (transfers.get(transferId) != null) {

			int percentage = transfers.get(transferId).getTransferPercentage();

			if (percentage == 100)
				transfers.remove(transferId);

			logger.info("Poll request for transfer with id : " + transferId
					+ ", returning: " + percentage);

			return percentage;
		} else {
			logger.warn("Download with id:" + transferId + ", does not exist!");
			return 0;
		}
	}

	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

}

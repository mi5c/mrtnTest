package com.shazam.practice.server.logic.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.shazam.practice.server.logic.TransferOperation;
import com.shazam.practice.server.utils.ServerProperties;

public class AmazonS3Uploader implements TransferOperation {

	private static Logger logger = Logger.getLogger(AmazonS3Uploader.class);

	private final String fileName;
	private String bucketName;
	private Upload upload;

	protected int upLoadPercentage;

	private static TransferManager tx;

	public AmazonS3Uploader(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public int getTransferPercentage() {
		return upLoadPercentage;
	}

	@Override
	public String execute() throws Exception {
		PropertiesCredentials credentials = new PropertiesCredentials(this
				.getClass().getClassLoader()
				.getResourceAsStream(ServerProperties.FILENAME));

		tx = new TransferManager(credentials);

		bucketName = getBucketName() + "-"
				+ credentials.getAWSAccessKeyId().toLowerCase();

		createAmazonS3Bucket();

		ProgressListener progressListener = new ProgressListener() {
			public void progressChanged(ProgressEvent progressEvent) {
				if (upload == null)
					return;

				upLoadPercentage = (int) upload.getProgress()
						.getPercentTransfered();

				switch (progressEvent.getEventCode()) {
				case ProgressEvent.COMPLETED_EVENT_CODE:
					logger.info("File: " + fileName
							+ " upload to Amazon complete");
					break;
				case ProgressEvent.FAILED_EVENT_CODE:
					try {
						upload.waitForException();
						logger.error("Amazon S3 sent failure code: "
								+ ProgressEvent.FAILED_EVENT_CODE);
					} catch (InterruptedException e) {
					}
					break;
				}
			}
		};

		File fileToUpload = new File(getDLFolder() + fileName);
		PutObjectRequest request = new PutObjectRequest(bucketName,
				fileToUpload.getName(), fileToUpload)
				.withProgressListener(progressListener);
		upload = tx.upload(request);

		return upload.getDescription();
	}

	private void createAmazonS3Bucket() throws Exception {
		try {
			if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
				tx.getAmazonS3Client().createBucket(bucketName);
			}
		} catch (AmazonClientException ace) {
			throw new Exception("Unable to create a new Amazon S3 bucket: "
					+ ace.getMessage());
		}
	}

	private String getBucketName() throws FileNotFoundException, IOException {
		Properties accountProperties = ServerProperties.get();

		if (accountProperties.getProperty("bucketName") == null) {
			throw new IllegalArgumentException(
					"Server properties doesn't contain the expected properties 'bucketName'.");
		}
		return bucketName = accountProperties.getProperty("bucketName");
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

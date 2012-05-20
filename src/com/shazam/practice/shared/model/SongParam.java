package com.shazam.practice.shared.model;

import java.io.Serializable;

public class SongParam implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5910498313567833339L;

	private String user;
	private String title;
	private boolean isDownloadable;
	private String downloadURI;

	public SongParam() {
	}

	public SongParam(String user, String title, boolean isDownloadable,
			String downloadURI) {
		this.user = user;
		this.title = title;
		this.isDownloadable = isDownloadable;
		this.downloadURI = downloadURI;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDownloadable(boolean isDownloadable) {
		this.isDownloadable = isDownloadable;
	}

	public boolean isDownloadable() {
		return isDownloadable;
	}

	public void setDownloadURI(String downloadURI) {
		this.downloadURI = downloadURI;
	}

	public String getDownloadURI() {
		return downloadURI;
	}

}

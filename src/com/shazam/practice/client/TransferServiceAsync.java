package com.shazam.practice.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.shazam.practice.shared.model.SongParam;

/**
 * The async counterpart of <code>TransferService</code>.
 * 
 * 
 * See {@link TransferService} for Javadoc.
 */
public interface TransferServiceAsync {

	void getSongList(String name, AsyncCallback<List<SongParam>> callback);

	void downloadSong(int downloadId, String uri, AsyncCallback<String> callback);

	void pollTransferStatus(int downloadId, AsyncCallback<Integer> callback);

	void uploadSong(int transferId, String fileName,
			AsyncCallback<Void> asyncCallback);
}

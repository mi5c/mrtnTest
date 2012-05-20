package com.shazam.practice.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.shazam.practice.shared.ServerSideException;
import com.shazam.practice.shared.model.SongParam;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("transfer")
public interface TransferService extends RemoteService {

	/**
	 * Method for querying all the songs by a certain user.
	 * 
	 * @param name
	 *            the name of the user
	 * @return list of songs the user has
	 * @throws ServerSideException
	 *             returns the error to client
	 */
	List<SongParam> getSongList(String name) throws ServerSideException;

	/**
	 * Method for the client to ask the server about the transfer status
	 * 
	 * @param transferId
	 *            id of the transfer being polled
	 * @return percentage of the transfer completed
	 */
	int pollTransferStatus(int transferId);

	/**
	 * Method for the client to call the server to start the download. The
	 * client receives the response to this method when the download is 100%
	 * complete.
	 * 
	 * @param transferId
	 *            id that comes from client and the client can use this id to
	 *            poll for transfer status
	 * @param uri
	 *            the URI where the object that needs to be downloaded resides
	 * @return song name stored in the server
	 * @throws ServerSideException
	 *             returns the error to client
	 */
	String downloadSong(int transferId, String uri) throws ServerSideException;

	/**
	 * Method for the client to call the server to start the upload. This
	 * differs from download method - this will instantly tell the client that
	 * the upload has started. The client can use
	 * {@link #pollTransferStatus(int)} to ask for the upload status. This
	 * behavior was caused by Amazons S3 API, which does no blocking calls.
	 * 
	 * 
	 * @param transferId
	 *            id that comes from client and the client can use this id to
	 *            poll for transfer status
	 * @param fileName
	 *            the file name in server's stored files folder that must be
	 *            uploaded
	 * 
	 * @throws ServerSideException
	 *             returns the error to client
	 */
	void uploadSong(int transferId, String fileName) throws ServerSideException;
}

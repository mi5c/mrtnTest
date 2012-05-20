package com.shazam.practice.client;

import java.util.List;
import java.util.Random;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.shazam.practice.shared.model.SongParam;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ShazamTransfer implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Transfer
	 * service.
	 */
	private final TransferServiceAsync transfterService = GWT
			.create(TransferService.class);

	private final Label errorLabel = new Label();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Send");
		final TextBox nameField = new TextBox();
		nameField.setText("SoundCloud User");

		sendButton.addStyleName("sendButton");

		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				RootPanel.get("songsTable").clear();
				getSongListFromServer(nameField.getText());
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					RootPanel.get("songsTable").clear();
					getSongListFromServer(nameField.getText());
				}
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}

	private void getSongListFromServer(String name) {
		RootPanel.get("processbarContainer").clear();
		RootPanel.get("statusLogBar").clear();
		errorLabel.setText("");

		transfterService.getSongList(name,
				new AsyncCallback<List<SongParam>>() {
					@Override
					public void onFailure(Throwable caught) {
						errorLabel.setText(caught.getMessage());
					}

					@Override
					public void onSuccess(List<SongParam> result) {
						drawSongListTable(result);
					}
				});

	}

	protected void drawSongListTable(List<SongParam> result) {

		CellTable<SongParam> table = new CellTable<SongParam>();

		TextColumn<SongParam> nameColumn = new TextColumn<SongParam>() {
			@Override
			public String getValue(SongParam object) {
				return object.getUser();
			}
		};
		table.addColumn(nameColumn, "Name");

		TextColumn<SongParam> titleColumn = new TextColumn<SongParam>() {
			@Override
			public String getValue(SongParam object) {
				return object.getTitle();
			}
		};
		table.addColumn(titleColumn, "Title");

		ButtonCell buttonCell = new ButtonCell();
		Column<SongParam, String> transferColumn = new Column<SongParam, String>(
				buttonCell) {
			@Override
			public String getValue(SongParam object) {
				return "Transfer";
			}

			@Override
			public void render(Context context, SongParam object,
					SafeHtmlBuilder sb) {
				if (object.isDownloadable())
					super.render(context, object, sb);
			}
		};

		transferColumn.setFieldUpdater(new FieldUpdater<SongParam, String>() {

			@Override
			public void update(int index, SongParam object, String value) {
				downloadSong(object.getDownloadURI());

			}
		});

		table.addColumn(transferColumn, "");
		

		table.setRowCount(result.size(), true);

		table.setRowData(0, result);
		RootPanel.get("songsTable").add(table);
	}

	protected void downloadSong(String downloadURI) {
		final int transferId = new Random().nextInt(100000);

		final ProgressBar dlBar = new ProgressBar(0.0, 100.0);
		final ProgressBar upBar= new ProgressBar(0.0, 100.0);
		dlBar.setWidth("99px");
		upBar.setWidth("99px");

		HorizontalPanel p = new HorizontalPanel();
		p.add(dlBar);
		p.add(upBar);
		p.setWidth("200px");
		RootPanel.get("processbarContainer").add(p);

		final Timer delayedPolling = new Timer() {
			public void run() {
				scheduleTransferStatusPoll();
			}

			private void scheduleTransferStatusPoll() {
				transfterService.pollTransferStatus(transferId,
						new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								errorLabel.setText(caught.getMessage());

							}

							@Override
							public void onSuccess(Integer result) {
								dlBar.setProgress(result);
								if (result < 100)
									schedule(1000);
							}
						});

			}
		};
		delayedPolling.schedule(1000);

		transfterService.downloadSong(transferId, downloadURI,
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						errorLabel.setText(caught.getMessage());

					}

					@Override
					public void onSuccess(String result) {
						dlBar.setProgress(100);
						delayedPolling.cancel();
						RootPanel
								.get("statusLogBar")
								.add(new Label(
										result
												+ " downloaded from External Source (default: SoundCloud)"));
						scheduleUpload(result, upBar);
					}
				});

	}

	protected void scheduleUpload(final String fileName, final ProgressBar upBar) {
		final int transferId = new Random().nextInt(100000);
		final Timer delayedPolling = new Timer() {
			public void run() {
				scheduleTransferStatusPoll();
			}

			private void scheduleTransferStatusPoll() {
				transfterService.pollTransferStatus(transferId,
						new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								errorLabel.setText(caught.getMessage());

							}

							@Override
							public void onSuccess(Integer result) {
								upBar.setProgress(result);
								if (result < 100)
									schedule(1000);
								else {
									RootPanel
											.get("statusLogBar")
											.add(new Label(
													fileName
															+ " upload to External Source finished (default: Amazon S3)"));
								}
							}
						});

			}
		};
		delayedPolling.schedule(1000);

		transfterService.uploadSong(transferId, fileName,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						errorLabel.setText(caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
						RootPanel
								.get("statusLogBar")
								.add(new Label(
										fileName
												+ " upload to External Source started (default: Amazon S3)"));
					}
				});
	}
}

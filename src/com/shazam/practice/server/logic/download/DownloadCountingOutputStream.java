package com.shazam.practice.server.logic.download;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.CountingOutputStream;

/**
 * Extension for CountingOutputStream. Might be obsolete now. (I used apache
 * commons-io 1.3 originally)
 * 
 */
public class DownloadCountingOutputStream extends CountingOutputStream {

	private ActionListener listener = null;
	private long writtenBytes;

	public DownloadCountingOutputStream(OutputStream out) {
		super(out);
	}

	public void setListener(ActionListener listener) {
		this.listener = listener;
	}

	protected void afterWrite(int n) throws IOException {
		setWrittenBytes(getWrittenBytes() + n);
		if (listener != null) {
			listener.actionPerformed(new ActionEvent(this, 0, null));
		}
	}

	public void write(byte[] bts, int st, int end) throws IOException {
		try {
			out.write(bts, st, end);
			afterWrite(end);
		} catch (IOException e) {
			throw e;
		}
	}

	public void setWrittenBytes(Long writtenBytes) {
		this.writtenBytes = writtenBytes;
	}

	public Long getWrittenBytes() {
		return writtenBytes;
	}
}
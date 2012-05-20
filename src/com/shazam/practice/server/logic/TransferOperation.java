package com.shazam.practice.server.logic;

public interface TransferOperation {

	int getTransferPercentage();

	String execute() throws Exception;
}

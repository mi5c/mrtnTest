package com.shazam.practice.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {

	public static String FILENAME = "server.properties";

	private static Properties accountProperties;

	public static Properties get() {
		if (accountProperties == null)
			new ServerProperties().init();

		return accountProperties;
	}

	void init() {
		accountProperties = new Properties();
		InputStream s = this.getClass().getClassLoader()
				.getResourceAsStream(FILENAME);

		try {
			accountProperties.load(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package com.ailk.sets.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {
	private static Properties config;
	static {
		config = new Properties();
		InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			config.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getController(String key)
	{
		return config.getProperty(key);
	}
	
	public static void main(String[] args)
	{
	}
}

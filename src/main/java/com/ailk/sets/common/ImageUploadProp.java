package com.ailk.sets.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 图片上传配置
 * @author zengjie
 *
 */
public class ImageUploadProp {
	private final static Properties imageUploadProp = new Properties();
	
	private ImageUploadProp(){
	}
	static {
		InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("imageUpload.properties");
		try {
			imageUploadProp.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getController(String key)
	{
		return imageUploadProp.getProperty(key);
	}
}

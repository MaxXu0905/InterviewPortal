package com.ailk.sets.common;

import java.io.ByteArrayOutputStream;
/**
 * Image缓冲区类
 * */

public class ImageByteArrayOutputStream extends ByteArrayOutputStream {
	/**
	 * Image缓冲区大小
	 * */
	public int getCount() {
		return super.count;
	}

}

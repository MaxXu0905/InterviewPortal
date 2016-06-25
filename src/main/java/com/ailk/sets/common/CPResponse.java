package com.ailk.sets.common;

import java.io.Serializable;
/**
 * 基本的响应对象
 * 
 * @author 毕希研
 * 
 */
public class CPResponse<T> implements Serializable {
	private static final long serialVersionUID = 707889549300240834L;

	private String code;
	private String message="";

	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}

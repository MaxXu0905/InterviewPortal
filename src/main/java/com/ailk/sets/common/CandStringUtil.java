package com.ailk.sets.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.apache.http.NameValuePair;  

import org.springframework.util.StringUtils;

public class CandStringUtil extends StringUtils{
	public static String convertIStoStr(InputStream is) {
		StringBuilder sb = new StringBuilder();
		String readline = "";
		try {
			/**
			 * 若乱码，请改为new InputStreamReader(is, "GBK").
			 */
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (br.ready()) {
				readline = br.readLine();
				sb.append(readline);
			}
			br.close();
		} catch (IOException ie) {
			System.out.println("converts failed.");
		}
		return sb.toString();
	}
	
	public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}

}

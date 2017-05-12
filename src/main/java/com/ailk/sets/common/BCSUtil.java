package com.ailk.sets.common;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.JSONObject;
import com.alibaba.dubbo.common.json.ParseException;
import com.baidu.inf.iis.bcs.BaiduBCS;
import com.baidu.inf.iis.bcs.auth.BCSCredentials;
import com.baidu.inf.iis.bcs.model.ObjectMetadata;
import com.baidu.inf.iis.bcs.request.DeleteObjectRequest;
import com.baidu.inf.iis.bcs.request.PutObjectRequest;

public class BCSUtil {
	private BaiduBCS BAIDU_BCS = null;
	
	public BCSUtil() {
		
		String AK = ConfigProperties.getController("BAIDUPUBLICKEY");
		String SK = ConfigProperties.getController("BAIDUSECRETKEY");
		String HOST = ConfigProperties.getController("BAIDUENDPOINT");
		
		BAIDU_BCS = new BaiduBCS(new BCSCredentials(AK, SK), HOST);
	}
/**
 * 把一个图片存到云
 * */
	public boolean putObject(String bucketName, String fileName,
			ByteArrayInputStream is, long contentLength) throws IOException {
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentEncoding("utf-8");
		objectMetadata.setContentLength(contentLength);
		PutObjectRequest request = new PutObjectRequest(bucketName, fileName,
				is, objectMetadata);

		try{
			BAIDU_BCS.putObject(request);
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public boolean deleteObject(String bucketName,String objectName)
	{
		DeleteObjectRequest request = new DeleteObjectRequest(bucketName,objectName);
		try{
			BAIDU_BCS.deleteObject(request);
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 这个是百度的
	 * */
	public String getAccessToken()
	{
		String accessTokenStr = getAccessJson();
		JSONObject json;
		try {
			json = (JSONObject)JSON.parse(accessTokenStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
			
		return json.get("access_token").toString();
		
		
//		try {
//			String faceRecUrl  = ConfigProperties.getController("FACEREC");
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("access_token", URLEncoder.encode(BCSUtil.AccessToken, "UTF-8"));
//			params.put("url", URLEncoder.encode(imgUrl));
//			
//			String jsonStr = NetConnectionUtil.httpRequest(faceRecUrl, params);
//			JSONObject json = (JSONObject)JSON.parse(jsonStr);
//			if(json.contains("error_code"))
//			{
//				return -1;
//			}
//			else
//			{
//				JSONArray array = json.getArray("Face");
//				if(array == null || array.length() == 0)
//				{
//					return 0;
//				}
//				else
//					return array.length();
//			}
//			
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return 0;
	}
	
	public String getAccessJson()
	{
		String accessToken = ConfigProperties.getController("ACCESSTOKENURL") +"&client_id="+"xxx"+"&client_secret="+"xxx";
		
		return NetConnectionUtil.httpRequest(accessToken);
	}
	

//	public List<String> getObjectList(String bucketName)
//	{
//		List<Interview> interviews = new ArrayList<Interview>();
//		BaiduBCSResponse<ObjectListing> objectList = BAIDU_BCS.listObject(new ListObjectRequest(bucketName));
//		if(objectList != null)
//		{
//			List<ObjectSummary> list = objectList.getResult().getObjectSummaries();
//			for(int i=0;i<list.size();i++)
//			{
//				
//				
//			}
//		}
//	}
//	
	/**
	 * 什么功能?
	 * */
	public static String setSHA1Code(String httpMethodName,String bucketName,String objectName) {
		
//		String content = "MBO" + "\n";
//		for(String key : map.keySet())
//		{
//			content += key + map.get(key) + "\n";
//		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR,100);  

		String content = httpMethodName + "\n" + "\n" + "\n"
				+ (calendar.getTimeInMillis() / 1000) + "\n"
				+ "/"+bucketName +"/"+objectName;
		System.out.println(content);

		String digestStr = "";
		try {
			 SecretKeySpec signingKey = new SecretKeySpec(
			 "xxx".getBytes(), "HmacSHA1");
			 Mac mac = Mac.getInstance("HmacSHA1");

			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(content.getBytes("UTF-8"));
			digestStr = URLEncoder.encode(Base64.encodeBase64String(rawHmac),
					"UTF-8");
//			System.out.println(digestStr);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
//		String content = "MBO" + "\n" + "Method="+httpMethodName+"" + "\n"
//				+ "Bucket="+bucketName+"" + "\n" + "Object="+objectName+""
//				+ "\n";
//
//		String digestStr = "";
//		try {
//			SecretKeySpec signingKey = new SecretKeySpec(
//					ConfigProperties.getController("BAIDUSECRETKEY").getBytes(), "HmacSHA1");
//			Mac mac = Mac.getInstance("HmacSHA1");
//			mac.init(signingKey);
//
//			byte[] rawHmac = mac.doFinal(content.getBytes());
//			digestStr = URLEncoder.encode(
//					Base64.encodeBase64String(rawHmac), "UTF-8");
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//			return null;
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//			return null;
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return null;
//		}
		
		return digestStr;
	}
	
	public static String setSHA1Code(String httpMethodName,String bucketName,String objectName,long contentLength) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR,100);  

		String content = httpMethodName + "\n" + "\n" + "\n"
				+ (calendar.getTimeInMillis() / 1000) + "\n"
				+ "/"+bucketName +"/"+objectName;

		String digestStr = "";
		try {
			 SecretKeySpec signingKey = new SecretKeySpec(
			 "OtUTMq8xjSw4f2v9X4s8Wbgna7OfLT".getBytes(), "HmacSHA1");
			 Mac mac = Mac.getInstance("HmacSHA1");

			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(content.getBytes("UTF-8"));
			digestStr = URLEncoder.encode(Base64.encodeBase64String(rawHmac),
					"UTF-8");
			System.out.println(digestStr);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
//		String content = "MBO" + "\n" + "Method="+httpMethodName+"" + "\n"
//				+ "Bucket=setsvideo" + "\n" + "Object="+objectName+""
//				+ "\n"+"Content-Type = application/octet-stream"+
//				"\n" +"Content-Length="+contentLength +"\n";
//
//		String digestStr = "";
//		try {
//			SecretKeySpec signingKey = new SecretKeySpec(
//					ConfigProperties.getController("BAIDUSECRETKEY").getBytes(), "HmacSHA1");
//			Mac mac = Mac.getInstance("HmacSHA1");
//			mac.init(signingKey);
//
//			byte[] rawHmac = mac.doFinal(content.getBytes());
//			digestStr = URLEncoder.encode(
//					Base64.encodeBase64String(rawHmac), "UTF-8");
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//			return null;
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//			return null;
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return null;
//		}
		
		return digestStr;
	}

	public static void main(String[] args)  {
		System.out.println(BCSUtil.setSHA1Code("GET","111","dddd"));
	}
}

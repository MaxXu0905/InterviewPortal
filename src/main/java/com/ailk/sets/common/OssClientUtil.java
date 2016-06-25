package com.ailk.sets.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.aliyun.openservices.ClientConfiguration;
import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.aliyun.openservices.oss.model.CannedAccessControlList;
import com.aliyun.openservices.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.openservices.oss.model.GetObjectRequest;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.aliyun.openservices.oss.model.PutObjectResult;

/**
 * 阿里云OSS服务相关操作工具类
 * */
public class OssClientUtil {

	// 指定阿里云的数据中心
	private static final String OSS_ENDPOINT = "oss-cn-beijing.aliyuncs.com";
	private static final int EXPIRE_YEAR = 30;

	/**
	 * 初始化一个OSS服务交互客户端
	 * */
	public OSSClient init(String accessId, String accessKey) {
		ClientConfiguration config = new ClientConfiguration();// 代理
		OSSClient client = new OSSClient("http://" + OSS_ENDPOINT + "/", accessId, accessKey, config);
		return client;
	}

	/**
	 * 如果Bucket不存在，则创建它
	 * */
	public void ensureBucket(OSSClient client, String bucketName) throws OSSException, ClientException {

		if (client.doesBucketExist(bucketName)) {
			return;
		}

		// 创建bucket
		client.createBucket(bucketName);
		client.setBucketAcl(bucketName, CannedAccessControlList.Private);
	}

	/**
	 * 删除一个Bucket和其中的Objects
	 * */
	public void deleteBucket(OSSClient client, String bucketName) throws OSSException, ClientException {

		ObjectListing ObjectListing = client.listObjects(bucketName);
		List<OSSObjectSummary> listDeletes = ObjectListing.getObjectSummaries();
		for (int i = 0; i < listDeletes.size(); i++) {
			String objectName = listDeletes.get(i).getKey();
			// 如果不为空，先删除bucket下的文件
			client.deleteObject(bucketName, objectName);
		}
		client.deleteBucket(bucketName);
	}

	/**
	 * 把Bucket设置为所有人可读
	 * */
	public void setBucketPublicReadable(OSSClient client, String bucketName) throws OSSException, ClientException {
		// 创建bucket
		client.createBucket(bucketName);

		// 设置bucket的访问权限，public-read-write权限
		client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
	}

	/**
	 * 删除一个文件
	 * */
	public void deleteFile(OSSClient client, String bucketName, String objectName) {
		client.deleteObject(bucketName, objectName);
	}

	/**
	 * 上传文件到指定bucket
	 * */
	public boolean uploadFile(OSSClient client, String bucketName, String objectName, InputStream is, String contentType)
			throws Exception {
		boolean uploadResult = false;
		ImageByteArrayOutputStream bos = new ImageByteArrayOutputStream();// 新建一个内存缓冲区
		int ch;
		// long length = 0;
		while ((ch = is.read()) != -1) {
			bos.write(ch);// 将图片读到内存缓冲区
			// length++;
		}
		byte[] tem = bos.toByteArray();// 复制一块内存地址用来存图片
		// System.out.println("length:"+length+" count:"+bos.getCount()+"tem[] length"+tem.length);

		ByteArrayInputStream bis = new ByteArrayInputStream(tem);// 准备上传的流

		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setContentLength(tem.length);
		String localMD5 = MD5Util.getMD5String(tem);// 生成本地文件的MD5值,准备和返回的MD5进行校验
		// System.out.println("生成的MD5:"+localMD5);
		; // 可以在metadata中标记文件类型
		objectMeta.setContentType(contentType);
		// objectMeta.setContentEncoding("GBK");
		// objectMeta.setContentDisposition("inline"); // 不用下载直接显示
		// 上传
		PutObjectResult result = client.putObject(bucketName, objectName, bis, objectMeta);
		// System.out.println("返回的MD5:"+result.getETag());
		uploadResult = localMD5.equals(result.getETag().toString());// 上传结果成功为true,失败为false
		return uploadResult;
		// if (result != null)
		// return result.getETag();
		// else
		// return null;
	}

	// 下载文件
	public void downloadFile(OSSClient client, String bucketName, String key, String filename) throws OSSException,
			ClientException {
		client.getObject(new GetObjectRequest(bucketName, key), new File(filename));
	}

	public static String setSHA1Code(String httpMethodName, String bucketName, String objectName, long timeStamp) {

		String content = httpMethodName + "\n" + "\n" + "\n" + (timeStamp) + "\n" + "/" + bucketName + "/" + objectName;

		String digestStr = "";
		try {
			SecretKeySpec signingKey = new SecretKeySpec(ConfigProperties.getController("BAIDUSECRETKEY").getBytes(),
					"HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");

			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(content.getBytes("UTF-8"));
			digestStr = URLEncoder.encode(Base64.encodeBase64String(rawHmac), "UTF-8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return digestStr;
	}

	// 过期时间
	public static long getExpireTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, EXPIRE_YEAR);
		// System.out.println(calendar.getTime());
		return calendar.getTimeInMillis() / 1000;
		// return calendar.getTimeInMillis() ;
	}

	public static void main(String[] args) {
		// getExpireTime();
		// System.out.println(new Date(getExpireTime()));
		String bucketName = "your-bucket-name";
		String key = "your-object-key";
		System.out.println(new Date(getExpireTime() * 1000));
		// OssClientUtil ocu = new OssClientUtil().init(accessId, accessKey)

	}

	/**
	 * 根据默认过期时间生成一个预签名的URl,供其他应用调用
	 * 
	 * @param bucketName
	 * @param key
	 *            project名
	 * @return URL 可以调用的URL,默认为GET方式访问
	 * */
	public static URL getUrl(OSSClient client, String bucketName, String key) {

		// 设置URL过期时间为
		Date expiration = new Date(getExpireTime() * 1000);
		// 生成url所需要的参数bucketName,key,expiration
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);
		generatePresignedUrlRequest.setExpiration(expiration);
		// 生成URL
		URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
		return url;
	}
}

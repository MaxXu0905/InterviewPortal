package com.ailk.sets.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import sun.misc.BASE64Decoder;

import com.ailk.sets.common.BCSUtil;
import com.ailk.sets.common.CPResponse;
import com.ailk.sets.common.ConfigProperties;
import com.ailk.sets.common.Constant;
import com.ailk.sets.common.ImageUtil;
import com.ailk.sets.common.NetConnectionUtil;
import com.ailk.sets.common.OssClientUtil;
import com.ailk.sets.common.SysBaseResponse;
import com.ailk.sets.model.ImageSets;
import com.ailk.sets.model.SnapShotResult;
import com.ailk.sets.platform.intf.empl.domain.CandidateTest;
import com.aliyun.openservices.oss.OSSClient;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
public class File2AliController {

	/**
	 * 笔试过程中的监控图片上传到云
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/snapShotsUpload")
	public void snapShotsUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
		CPResponse<SnapShotResult> cpresponse = new CPResponse<SnapShotResult>();
		try {
			String bucketName = ConfigProperties.getController("MONITORBUCKET");// 文件夹?
			InputStream is = request.getInputStream();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int ch;
			long length = 0;
			while ((ch = is.read()) != -1) {
				bos.write(ch);
				length++;
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

			CandidateTest ce = (CandidateTest) request.getSession().getAttribute(Constant.CANDIDATE);
			long invitationId = ce.getTestId();
			BCSUtil bcs = new BCSUtil();
			String filename = "/" + invitationId + "/SETS" + (new Date()).getTime() + ".jpg";// 文件名称
			// 少加candidateId
			boolean result = bcs.putObject(bucketName, filename, bis, length);// 把图片存到ali云
			// 少加人脸识别
			String imgUrl = "";
			if (result) {// 如果存到了云
				String sha1Code = BCSUtil.setSHA1Code("GET", bucketName, filename);
				imgUrl = "http://" + ConfigProperties.getController("BAIDUENDPOINT") + "/" + bucketName + filename
						+ "?sign=MBO:" + ConfigProperties.getController("BAIDUPUBLICKEY") + ":" + sha1Code
						+ "&response-cache-control=private";
				// 组装上传图像结果信息,应该包括api_key,api_secret,url(待检测的图片地址)
				SnapShotResult sResult = new SnapShotResult(result, ConfigProperties.getController("FACEREC"), imgUrl,
						bcs.getAccessToken());
				cpresponse.setCode(SysBaseResponse.SUCCESS);
				cpresponse.setData(sResult);// 组装到基本相应类型,返回给客户端
			} else {
				cpresponse.setCode(SysBaseResponse.ESYSTEM);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			cpresponse.setCode(SysBaseResponse.ESYSTEM);
			e.printStackTrace();
		}

		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(cpresponse)); // 上传成功
	}

	/**
	 * 笔试过程中的监控图片上传到云,并检测
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @param invitationId
	 *            应聘者ID
	 * @param httpMethodName
	 *            ?
	 * @param snapshotType
	 *            要检测的图片的种类
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/snapShotsUploadTemp/{invitationId}/{objectName}/{httpMethodName}/{snapshotType}/{isPhotoFaceRec}")
	public void snapShotsUploadTemp(HttpServletRequest request, HttpServletResponse response,
			@PathVariable long invitationId, @PathVariable String objectName, @PathVariable String httpMethodName,
			@PathVariable String snapshotType,@PathVariable int isPhotoFaceRec) throws IOException {

		// CPResponse<SnapShotResult> cpresponse = new
		// CPResponse<SnapShotResult>();
		CPResponse<Object> cpresponse = new CPResponse<Object>();
		try {
			String bucketName = ConfigProperties.getController("MONITORBUCKET");
			InputStream _is = request.getInputStream();
			
			InputStream is = File2AliController.GenerateImage(_is);
			
			objectName += ".jpg";
			// objectName = invitationId + "/" + objectName;//
			// 阿里云上面的objectName是由文件夹和object名字组成.这里是有invitationId和时间戳组合而成

			// 获取指定文件的输入流
			OssClientUtil clientUtil = new OssClientUtil();
			OSSClient client = clientUtil.init(ConfigProperties.getController("BAIDUPUBLICKEY"),
					ConfigProperties.getController("BAIDUSECRETKEY"));

			if ("DELETE".equals(httpMethodName)) {// 如果是删除图片
				clientUtil.deleteFile(client, bucketName, objectName);
				System.out.println("删除成功!");
			} else if ("PUT".equals(httpMethodName)) {// 如果是上传图片
				boolean uploadResult = clientUtil.uploadFile(client, bucketName, objectName, is, "image/jpeg");
				// System.out.println(result);
				if (uploadResult) {// 上传成功
					// BCSUtil bcs = new BCSUtil();
					// long timeStamp = OssClientUtil.getExpireTime();
					// String sha1Code = OssClientUtil.setSHA1Code("GET",
					// bucketName, invitationId + "/" + objectName,timeStamp);
					// String imgUrl = "http://" +bucketName+"."+
					// ConfigProperties.getController("BAIDUENDPOINT") + "/" +
					// invitationId + "/" + objectName + "?Expires=" + timeStamp
					// +"&OSSAccessKeyId="+
					// ConfigProperties.getController("BAIDUPUBLICKEY") +
					// "&Signature=" + sha1Code ;
					String imgUrl = OssClientUtil.getUrl(client, bucketName, objectName).toString();

					// System.out.println(invitationId);
					System.out.println(imgUrl);
					if(isPhotoFaceRec == 0 && snapshotType.equals("TAKEPHOTO"))  //照片不用检查，直接返回照片
					{
						cpresponse.setMessage(imgUrl);
					}
					else
						cpresponse = faceRec2(client, snapshotType, bucketName, objectName, imgUrl, invitationId);
					// SnapShotResult sResult = new SnapShotResult(true,
					// ConfigProperties.getController("FACEREC"), imgUrl,
					// bcs.getAccessToken());
					// cpresponse.setData(sResult);
					// cpresponse.setCode(SysBaseResponse.SUCCESS);
				} else {// 上传失败
					cpresponse.setCode(SysBaseResponse.ESYSTEM);
				}
			}

		} catch (Exception e) {// 不管怎样异常了都是失败的
			// TODO Auto-generated catch block
			cpresponse.setCode(SysBaseResponse.ESYSTEM);
			e.printStackTrace();
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(cpresponse)); // 上传成功
	}

	public static InputStream GenerateImage(InputStream is)
    {//对字节数组字符串进行Base64解码并生成图片
        if (is == null) //图像数据为空
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try 
        {
            //Base64解码
            byte[] b = decoder.decodeBuffer(is);
            for(int i=0;i<b.length;++i)
            {
                if(b[i]<0)
                {//调整异常数据
                    b[i]+=256;
                }
            }
            return new ByteArrayInputStream(b) ;
        } 
        catch (Exception e) 
        {
            return null;
        }
    }
	
	/**
	 * 检测一个图片中的头像信息(面试头像\监控头像)
	 * 
	 * @param imgUrl
	 *            图片地址
	 * @param snapshotType
	 *            检测种类(报告图片\异常图片)
	 * @return CPResponse<Object> 返回一个响应类型
	 * */
	private CPResponse<Object> faceRec2(OSSClient client, String snapshotType, String bucketName, String objectName,
			String imgUrl, long invitationId) {
		CPResponse<Object> cpresponse = new CPResponse<Object>();// 系统基本响应类型
		JSONObject result = null;
		int personCount = 0;
		HttpRequests httpRequests = new HttpRequests(ConfigProperties.getController("APIKEY"),
				ConfigProperties.getController("APISECRET"), true, true);
		try {
			// String.imgUrl.
			PostParameters postParameters = new PostParameters();
			postParameters.setUrl(imgUrl);
			postParameters.setAttribute("all");
			result = httpRequests.detectionDetect(postParameters);// 设置RUL
			personCount = result.getJSONArray("face").length();// 解析图片,一共有多少个人
		} catch (Exception e) {// 解析失败.图片异常.
			cpresponse.setCode(SysBaseResponse.ESYSTEM);
			e.printStackTrace();
			cpresponse.setCode(SysBaseResponse.ESYSTEM);
			
			return cpresponse;
		}

		if (snapshotType.equals("TAKEPHOTO")) {// 报告图片

			if (personCount == 0 || personCount > 1 || result == null) {
				// 照片不正常，无法作为报告照片
				System.out.println("不能用作报告图片");
				cpresponse.setCode(SysBaseResponse.ILLEGAL);
				client.deleteObject(bucketName, objectName);
			} else {
				// candidateTestService.updateCandidatePic(invitationId,
				// map.get("url").getAsString());
				System.out.println("可以作为报告图片");
				cpresponse.setMessage(imgUrl);
				System.out.println(imgUrl);

				cpresponse.setCode(SysBaseResponse.SUCCESS);
			}
		} else if (snapshotType.equals("MONITOR")) {// 监控图片

			if (result == null || personCount == 0 || personCount > 1) {// 异常图片
				System.out.println("异常图片");
				Map<String, String> params = new HashMap<String, String>();
				params.put("imgUrl", imgUrl);
				params.put("faceNum", personCount + "");
				params.put("invitationId", invitationId + "");
				cpresponse.setCode(SysBaseResponse.SUCCESS);
				cpresponse.setData(params);
			} else {// 正常图片
				System.out.println("正常图片");
				client.deleteObject(bucketName, objectName);
				cpresponse.setCode(SysBaseResponse.SUCCESS);
			}

		}
		return cpresponse;
	}

	/**
	 * 人脸识别
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/faceRec/{snapshotType}/{invitationId}/{objectName}")
	public void faceRec(HttpServletRequest request, HttpServletResponse response, @PathVariable String snapshotType,
			@PathVariable long invitationId, @PathVariable String objectName) throws IOException {

		CPResponse<Object> cpresponse = new CPResponse<Object>();
		try {
			String faceRecUrl = request.getParameter("faceRecUrl");
			String jsonStr = NetConnectionUtil.httpRequest(faceRecUrl);
			jsonStr = jsonStr == null ? "{}" : jsonStr;
			JsonObject map = new JsonParser().parse(jsonStr).getAsJsonObject();

			if (map.has("error_code")) {
				cpresponse.setCode(SysBaseResponse.ESYSTEM);
			} else {
				JsonArray array = (JsonArray) map.get("face");
				if (snapshotType.equals("TAKEPHOTO")) {
					if (array == null || array.size() == 0 || array.size() > 1) {
						// 照片不正常，无法作为报告照片
						cpresponse.setCode(SysBaseResponse.ILLEGAL);
						String bucketName = ConfigProperties.getController("MONITORBUCKET");
						objectName += ".jpg";
						String message = "http://" + "" + bucketName + ".bcs.duapp.com" + "/" + invitationId + "/"
								+ objectName + "?sign=MBO:" + "lpzTfYczyf9jB3Uc0xluTWi3" + ":"
								+ BCSUtil.setSHA1Code("DELETE", bucketName, "/" + invitationId + "/" + objectName);
						NetConnectionUtil.httpRequest(message, "DELETE");
					} else {
						// candidateTestService.updateCandidatePic(invitationId,
						// map.get("url").getAsString());
						cpresponse.setCode(SysBaseResponse.SUCCESS);
						cpresponse.setMessage(map.get("url").getAsString());
						cpresponse.setCode(SysBaseResponse.SUCCESS);
					}
				} else if (snapshotType.equals("MONITOR")) {
					if (array == null || array.size() > 1) {
						Map<String, String> params = new HashMap<String, String>();
						params.put("imgUrl", map.get("url").getAsString());
						params.put("faceNum", array.size() + "");
						params.put("invitationId", invitationId + "");
						cpresponse.setData(params);
						// String cpResCode =
						// NetConnectionUtil.httpRequest(saveMonitorImgUrl,"POST",new
						// ByteArrayInputStream(new
						// Gson().toJson(params).getBytes()));
						cpresponse.setCode(SysBaseResponse.SUCCESS);
					} else {
						String bucketName = ConfigProperties.getController("MONITORBUCKET");
						objectName += ".jpg";
						String message = "http://" + "" + bucketName + ".bcs.duapp.com" + "/" + invitationId + "/"
								+ objectName + "?sign=MBO:" + "lpzTfYczyf9jB3Uc0xluTWi3" + ":"
								+ BCSUtil.setSHA1Code("DELETE", bucketName, "/" + invitationId + "/" + objectName);
						NetConnectionUtil.httpRequest(message, "DELETE");
						cpresponse.setCode(SysBaseResponse.SUCCESS);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			cpresponse.setCode(SysBaseResponse.ESYSTEM);
			e.printStackTrace();
		}

		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(cpresponse)); // 上传成功
	}

	/**
	 * 面试视频
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/videoUpload/{interviewId}")
	public void videoUpload(HttpServletRequest request, HttpServletResponse response, @PathVariable long interviewId)
			throws IOException {
		CPResponse<Object> cpResponse = new CPResponse<Object>();

		try {
			String red5Path = ConfigProperties.getController("REDPATH"); // red5视频的路径
			String bucketName = ConfigProperties.getController("INTERVIEWBUCKET");

			String videoName = interviewId + ".flv";
			File videoFile = new File(red5Path + videoName);
			InputStream is = new FileInputStream(videoFile);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int ch;
			long length = 0;
			while ((ch = is.read()) != -1) {
				bos.write(ch);
				length++;
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

			BCSUtil bcs = new BCSUtil();
			CandidateTest ce = (CandidateTest) request.getSession().getAttribute(Constant.CANDIDATE);
			long invitationId = ce.getTestId();
			videoName = "/" + invitationId + "/" + videoName;
			boolean result = bcs.putObject(bucketName, videoName, bis, length); // videoName
																				// 需要加个EMPLOYID//面试题ID

			if (result) {
				// LinkedTreeMap<String, String> map = new LinkedTreeMap<String,
				// String>();
				// map.put("Method", "GET");
				// map.put("Bucket", bucketName);
				// map.put("Object", videoName);
				String sha1Code = BCSUtil.setSHA1Code("GET", bucketName, videoName, length);
				String imgUrl = "http://" + ConfigProperties.getController("BAIDUENDPOINT") + "/" + bucketName
						+ videoName + "?sign=MBO:" + ConfigProperties.getController("BAIDUPUBLICKEY") + ":" + sha1Code
						+ "&response-cache-control=private";
				// paperInstanceService.updatePaperInstanceQuesUrl(invitationId,
				// interviewId, imgUrl);
				cpResponse.setCode(SysBaseResponse.SUCCESS);
			}
			videoFile.delete();
			cpResponse.setCode(SysBaseResponse.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			cpResponse.setCode(SysBaseResponse.ESYSTEM);
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(cpResponse));
	}

	/**
	 * 面试视频
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/videoUploadTemp/{httpMethodName}/{invitationId}/{oldVideoName}/{interviewId}")
	public void videoUploadTemp(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String httpMethodName, @PathVariable long invitationId, @PathVariable String oldVideoName,
			@PathVariable long interviewId) throws IOException {

		CPResponse<Object> cpResponse = new CPResponse<Object>();
		try {

			// String red5Path =
			// "/unibss/tstusers/tstsets1/red5/webapps/SOSample/streams/"; //
			// red5视频的路径
			String red5Path = ConfigProperties.getController("REDPATH");
			String bucketName = ConfigProperties.getController("INTERVIEWBUCKET");

			oldVideoName += ".flv"; // red5上面的视频
			String videoName = interviewId + ".flv"; // 每个题号的视频
			File videoFile = new File(red5Path + oldVideoName);
			if (videoFile.exists()) {
				InputStream is = new FileInputStream(videoFile);

				// int invitationId = 111111;
				/*
				 * String message = "http://" + "" + bucketName +
				 * ".bcs.duapp.com" + "/" + invitationId + "/" + videoName +
				 * "?sign=MBO:" + "lpzTfYczyf9jB3Uc0xluTWi3" + ":" +
				 * BCSUtil.setSHA1Code(httpMethodName, bucketName, "/" +
				 * invitationId + "/" + videoName);
				 */

				// 上传面试视频
				OssClientUtil clientUtil = new OssClientUtil();
				OSSClient client = clientUtil.init(ConfigProperties.getController("BAIDUPUBLICKEY"),
						ConfigProperties.getController("BAIDUSECRETKEY"));

				clientUtil.ensureBucket(client, bucketName);

				boolean uploadResult = clientUtil.uploadFile(client, bucketName, invitationId + "/" + videoName, is,
						"video/x-flv");
				// String jsonStr =
				// NetConnectionUtil.httpRequest(httpMethodName, is); //
				// videoName

				// System.out.println(result);
				if (uploadResult) {
					// LinkedTreeMap<String, String> map = new
					// LinkedTreeMap<String,
					// String>();
					// map.put("Method", "GET");
					// map.put("Bucket", bucketName);
					// map.put("Object", videoName);

					long timeStamp = OssClientUtil.getExpireTime();
					String sha1Code = OssClientUtil.setSHA1Code("GET", bucketName, invitationId + "/" + videoName,
							timeStamp);
					String imgUrl = "http://" + bucketName + "." + ConfigProperties.getController("BAIDUENDPOINT")
							+ "/" + invitationId + "/" + videoName + "?Expires=" + timeStamp + "&OSSAccessKeyId="
							+ ConfigProperties.getController("BAIDUPUBLICKEY") + "&Signature=" + sha1Code;

					// 上传视频地址
					Map<String, String> params = new HashMap<String, String>();

					params.put("videoUrl", imgUrl);
					params.put("interviewId", interviewId + "");
					params.put("invitationId", invitationId + "");

					cpResponse.setData(params);
					// System.out.println("saveMonitorImgUrl"+saveMonitorImgUrl);
					// String cpResCode =
					// NetConnectionUtil.httpRequest(saveMonitorImgUrl,"POST",new
					// Gson().toJson(params));
					cpResponse.setCode(SysBaseResponse.SUCCESS);
				} else {// 上传失败
					cpResponse.setCode(SysBaseResponse.ESYSTEM);
				}
			} else {
				cpResponse.setCode(SysBaseResponse.ILLEGAL);
			}
			videoFile.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
			cpResponse.setCode(SysBaseResponse.ESYSTEM);
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(cpResponse)); // 上传成功
	}

	/**
	 * 得到加密MBO
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @param httpMethodName
	 *            PUT GET DELETE LIST....
	 * @param keyBucket
	 *            取得bucketName的key
	 * @param objectName
	 *            bcs objectName
	 * @throws IOException
	 * @return
	 */
	@RequestMapping(value = "/getBcsUploadPath/{keyBucket}/{objectName}")
	public void getBCSUploadPath(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String keyBucket, @PathVariable String objectName) throws IOException {

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			// BCSUtil bcs = new BCSUtil();

			String bucketName = "setstitleimg";

			CandidateTest ce = (CandidateTest) request.getSession().getAttribute(Constant.CANDIDATE);
			long invitationId = ce.getTestId();

			String message = "http://" + "" + bucketName + ".bcs.duapp.com" + "/" + invitationId + "/" + objectName
					+ "?sign=MBO:" + "lpzTfYczyf9jB3Uc0xluTWi3" + ":"
					+ BCSUtil.setSHA1Code("PUT", bucketName, "/" + invitationId + "/" + objectName);

			result.put("code", SysBaseResponse.SUCCESS);
			result.put("message", message);
		} catch (Exception e) {
			result.put("code", SysBaseResponse.ESYSTEM);
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(new Gson().toJson(result)); // 上传成功
	}

	/**
	 * 得到accessToken
	 * 
	 * @author Mia
	 * @param request
	 * @param response
	 * @throws IOException
	 * @return
	 */
	@RequestMapping("/accesstoken")
	public void getAccessToken(HttpServletRequest request, HttpServletResponse response, @PathVariable String videoName)
			throws IOException {

		BCSUtil bcs = new BCSUtil();

		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(bcs.getAccessJson()); // 上传成功
	}
	
	public static void main(String[] args) {

		String message = "http://" + "" + "monitorimgs" + ".bcs.duapp.com" + "/" + 111 + "/" + 121 + "?sign=MBO:"
				+ "lpzTfYczyf9jB3Uc0xluTWi3" + ":" + BCSUtil.setSHA1Code("PUT", "monitorimgs", "/" + 111 + "/" + 121);

		System.out.println(message);
		// System.out.println(BCSUtil.setSHA1Code("GET", "setsvideo", "/" + 670
		// + "/" + "0.flv" ));

	}
}

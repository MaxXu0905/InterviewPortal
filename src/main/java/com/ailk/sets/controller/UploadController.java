package com.ailk.sets.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ailk.sets.common.ConfigProperties;
import com.ailk.sets.common.ImageUtil;
import com.ailk.sets.common.OssClientUtil;
import com.ailk.sets.model.ImageSets;
import com.aliyun.openservices.oss.OSSClient;

@Controller
@RequestMapping("/upload")
public class UploadController {
	private final static Logger log = Logger.getLogger(UploadController.class);
	@RequestMapping(value="/uploadQbPic/{qbId}", method = RequestMethod.POST)
	public void uploadPic(HttpServletRequest request, HttpServletResponse response,@PathVariable String qbId) throws IOException, JSONException {
		 if (!ServletFileUpload.isMultipartContent(request)) {
	            throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
	        }

	        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
	        response.setContentType("application/json");
	        JSONArray jsonarr = new JSONArray();
	        JSONObject json = new JSONObject();
	        try {
				List<FileItem> items = uploadHandler.parseRequest(request);
	            for (FileItem item : items) {
	                if (!item.isFormField()) {
	                        InputStream in = item.getInputStream();
	                      //上传到云存储
	        				String suffix=item.getName().substring(item.getName().lastIndexOf(".")+1);
	        				if(suffix.toLowerCase().equals("jpg") || suffix.toLowerCase().equals("png")  || suffix.toLowerCase().equals("jpeg")){
	        				}else{
	        					json.put("formatError", "文件格式不正确（必须为jpg/jpeg/png文件）");
	        				}
	        				if(item.getSize()>2* 1024 * 1024){
	        					json.put("sizeError", "文件不得大于2M");
	        				}
	        				if(item.getSize()<0){
	        					json.put("sizeError", "文件为空文件或者已损坏");
	        				}
	        				if(!json.has("formatError")  && !json.has("sizeError")){
	        					//上传图片
	        					String contenType = item.getContentType();
	        					//testType:招聘类型，默认写1：社招，2：校招
	        					String imgUrl = toUploadQbpic(qbId,suffix,contenType,in,1);
	        					JSONObject jsono = new JSONObject();
	        					jsono.put("name", item.getName());
	        					jsono.put("size", (item.getSize()/1024)+"kb");
	        					jsono.put("src",imgUrl);
	        					jsonarr.put(jsono);
	        				}
	                }
	            }
	            json.put("result", jsonarr.toString());
	        } catch (Exception e) {
	        		log.error("连接阿里云上传图片出错："+e.getMessage(),e);
	        		json.put("error", "连接阿里云上传图片出错："+e.getMessage());
	        }  finally {
	        	response.setContentType("text/html;charset=utf-8");
	            PrintWriter printWriter = response.getWriter();
	            printWriter.write(json.toString());
	            printWriter.flush();
	            printWriter.close();
	        }
	}
	
	/**
	 * 上传题库图片
	 * @param qbId
	 * @param prefix
	 * @param contentType
	 * @param in
	 * @param json
	 * @return
	 * @throws Exception 
	 */
	private String toUploadQbpic(String qbId,String suffix,String contentType,InputStream in,int testType) throws Exception{
		String bucketName = ConfigProperties.getController("QUESTIONBUCKET");// 文件夹
		String imgUrl = null;
		//生成新的图片名
		long currentTime = System.currentTimeMillis();
//		SimpleDateFormat df = new SimpleDateFormat("yyMMdd");//设置日期格式
//		String currentTime = df.format(new Date());
		String objectName = qbId+ "-"+String.valueOf(currentTime)+"."+suffix; //文件名，qbId+时间戳+格式
		// 获取指定文件的输入流
		OssClientUtil clientUtil = new OssClientUtil();
		OSSClient client = clientUtil.init(ConfigProperties.getController("BAIDUPUBLICKEY"),ConfigProperties.getController("BAIDUSECRETKEY"));
		//上传
		 ImageSets img  = ImageUtil.aliResize(objectName,in,suffix);
		 if(null!=img && null!=img.getIn()){
			 boolean uploadResult = clientUtil.uploadFile(client, bucketName, objectName, img.getIn(), contentType);
			 if (uploadResult) {
			  //imgUrl = OssClientUtil.getUrl(client, bucketName, objectName).toString();
				 imgUrl = img.getUrl();
				 //缩小
				 System.out.println(imgUrl);
			 }
		 }
		return imgUrl;
	}
}

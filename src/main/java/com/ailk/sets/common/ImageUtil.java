package com.ailk.sets.common;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ailk.sets.model.ImageSets;

/**
 * 图片处理工具，图片水印，文字水印，缩放，补白等
 * @author zengjie
 *14/8/29
 */
public final class ImageUtil {
		private final static String USERDOMAIN =  ImageUploadProp.getController("USERDOMAIN");
	  /**
	       * 添加图片水印
	       * @param targetImg 目标图片路径，如：C://myPictrue//1.jpg
	      * @param waterImg  水印图片路径，如：C://myPictrue//logo.png
	       * @param x 水印图片距离目标图片左侧的偏移量，如果x<0, 则在正中间
	      * @param y 水印图片距离目标图片上侧的偏移量，如果y<0, 则在正中间
	       * @param alpha 透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	  */
	     public final static void pressImage(String targetImg, String waterImg, int x, int y, float alpha) {
	    	 
	     }
	     
	     /**
	           * 添加文字水印
	         www.101test.com
	      */
	      public static String pressWaterMarkText() {
	    	  String text = "|watermark=2&text=d3d3LjEwMXRlc3QuY29t&type=ZmFuZ3poZW5na2FpdGk&size=24&color=IzVmYjBkZQ&p=9&y=11&x=10&t=98"; //文字水印
	    	  return text;
	      }
	    /**
	     *  ali云处理图片长宽		    
	     * @param url 上传ali云图片
	     * @return
	     * @throws IOException 
	     */
	     public static ImageSets aliResize(String object,InputStream input,String suffix) throws IOException{
	    	 String url = USERDOMAIN+object;
	    	 String style = "@";
	    	 ImageSets image = resize(1,input,suffix);
	    	 if(null==image){
	    		 return null;
	    	 }
	    	 if(image.getWidth()>0 && image.getHeight()>0){
	    		 String size = image.getWidth()+"w_"+ image.getHeight()+"h_";
	    		 style+=size;
	    	 }
	    	 style+="90Q.png";
	    	 url +=style;
	    	 //url+=pressWaterMarkText();//添加水印
	    	 image.setUrl(url);
	    	 return image;
	     }
	   
		 /**
		  * 长高等比缩小图片
		  * @param input
		  * @throws IOException
		  */
		 private static ImageSets resize(int testType,InputStream input,String suffix) throws IOException {
			 	// 1. 构造IMG对象
			 	BufferedImage src =  ImageIO.read(input);
			 	if(null==src){
			 		return null;
			 	}
			 	ImageSets image = new ImageSets();
			 	int originWidth = src.getWidth();
			 	image.setWidth(originWidth);
			 	int originHeight = src.getHeight();
			 	image.setHeight(originHeight);
			 	//缩小尺寸
			 	image = setImageRatio(testType,image);
			 	ByteArrayOutputStream os = new ByteArrayOutputStream();  
			 	ImageIO.write(src,suffix, os);  
		 		InputStream is = new ByteArrayInputStream(os.toByteArray()); 
		 		image.setIn(is);
			 	return image;
		 	}
		 /**
		  * 按规则设置图片比例
		  * @param image
		  * @return
		  */
		 private static  ImageSets  setImageRatio(int testType,ImageSets image ){
			 String type = "";
			 //1. 读取原图宽高
			 int origWidth = image.getWidth(),origHeight = image.getHeight();
			 //1.读取配置
			 if(testType==1){
				 //社招
				 type = "SOCIAL";
			 }else if(testType==2){
				 //校招
				 type = "CAMPUS";
			 }
			 int tagWidth = Integer.valueOf(ImageUploadProp.getController(type+"MAXWIDTH"));
			 int tagHeight = Integer.valueOf(ImageUploadProp.getController(type+"MAXHEIGHT"));
			 
			 //2.设置比例
			 if(origWidth>=origHeight){
				 //横图，以宽算比例
				 if(origWidth>tagWidth){
					 //超过设置宽度,重新计算宽高，比例四舍五入保留2位小数
					 double ratio = (double)origWidth/origHeight;
					 int newHeight = (int)(Math.round(tagWidth/ratio));
					 image.setHeight(newHeight);
					 image.setWidth(tagWidth);
				 }
			 }else{
				 //竖图，以高算比例
				 if(origHeight>tagHeight){
					 double ratio = (double) origHeight/origWidth;
					 int newWidth = (int)(Math.round(tagHeight/ratio));
					 image.setWidth(newWidth);
					 image.setHeight(tagHeight);
					 
				 }
			 }
			 
			 return image;
		 }
		 public static void main(String[] args) throws IOException {
//			 URL url = new URL("http://qbank-self.oss-cn-beijing.aliyuncs.com/100000306-140828.png?Expires=2355977179&OSSAccessKeyId=YvDPojSAo3YgjMXF&Signature=nrEX9eADCmBFSd1vCzZF7rQ%2BUus%3D");
//			 URLConnection urlConnection = url.openConnection();
//			 HttpURLConnection httpConnection  = (HttpURLConnection) urlConnection;
//			 httpConnection.setConnectTimeout(30000); 
//			 httpConnection.setReadTimeout(30000);
//			 if(httpConnection.getResponseCode()==200){
//				 InputStream input = httpConnection.getInputStream();
//				 ImageSets img  = ImageUtil.aliResize("100000306-140828.png",input,"png");
//				System.out.println(img.getUrl());
//			 }
		}
	
}

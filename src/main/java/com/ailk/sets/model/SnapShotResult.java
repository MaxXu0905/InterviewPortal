package com.ailk.sets.model;

public class SnapShotResult {
	
	private boolean result;
	private String bcsFaceRec;
	private String imgUrl;
	private String accessToken;
	
	public SnapShotResult(boolean result)
	{
		this.result = result;
	}
	
	public SnapShotResult(boolean result,String bcsFaceRec, String imgUrl, String accessToken)  {
		this.result = result;
		this.bcsFaceRec = bcsFaceRec;
		this.imgUrl = imgUrl;
		this.accessToken = accessToken;
	}
	
	
}

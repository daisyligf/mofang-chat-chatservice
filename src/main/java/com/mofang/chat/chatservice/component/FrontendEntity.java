package com.mofang.chat.chatservice.component;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 
 * @author zhaodx
 *
 */
public class FrontendEntity
{
	private FrontendInfo frontendInfo;
	private CloseableHttpClient httpClient;
	
	public FrontendInfo getFrontendInfo()
	{
		return frontendInfo;
	}
	
	public void setFrontendInfo(FrontendInfo frontendInfo)
	{
		this.frontendInfo = frontendInfo;
	}
	
	public CloseableHttpClient getHttpClient()
	{
		return httpClient;
	}
	
	public void setHttpClient(CloseableHttpClient httpClient)
	{
		this.httpClient = httpClient;
	}
}
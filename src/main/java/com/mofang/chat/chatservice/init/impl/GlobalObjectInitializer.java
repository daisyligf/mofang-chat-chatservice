package com.mofang.chat.chatservice.init.impl;

import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;

import com.mofang.chat.business.sysconf.SysObject;
import com.mofang.chat.chatservice.init.AbstractInitializer;
import com.mofang.chat.chatservice.component.FrontendEntity;
import com.mofang.chat.chatservice.component.FrontendInfo;
import com.mofang.chat.chatservice.component.FrontendProvider;
import com.mofang.chat.chatservice.global.GlobalConfig;
import com.mofang.chat.chatservice.global.GlobalObject;
import com.mofang.framework.net.http.HttpClientConfig;
import com.mofang.framework.net.http.HttpClientProvider;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalObjectInitializer extends AbstractInitializer
{
	@Override
	public void load() throws Exception
	{
		SysObject.initRedisMaster(GlobalConfig.REDIS_MASTER_CONFIG_PATH);
		SysObject.initRedisSlave(GlobalConfig.REDIS_SLAVE_CONFIG_PATH);
		SysObject.initWriteQueue(GlobalConfig.WRITE_QUEUE_CONFIG_PATH);
		SysObject.initPushQueue(GlobalConfig.PUSH_QUEUE_CONFIG_PATH);
		SysObject.initMysql(GlobalConfig.MYSQL_CONFIG_PATH);
		
		initFeServerConfig();
	}
	
	private void initFeServerConfig()
	{
		FrontendProvider feprovider = new FrontendProvider();
		List<FrontendInfo> frontendList = feprovider.getFrontendList(GlobalConfig.FESERVER_CONFIG_PATH);
		if(null == frontendList || frontendList.size() == 0)
			return;
		
		FrontendEntity entity = null;
		HttpClientConfig config = null;
		CloseableHttpClient httpClient = null;
		for(FrontendInfo feInfo : frontendList)
		{
			config = new HttpClientConfig();
			config.setHost(feInfo.getHost());
			config.setPort(feInfo.getPort());
			config.setMaxTotal(feInfo.getMaxTotal());
			config.setCharset(feInfo.getCharset());
			config.setConnTimeout(feInfo.getConnTimeout());
			config.setSocketTimeout(feInfo.getSocketTimeout());
			config.setDefaultKeepAliveTimeout(feInfo.getDefaultKeepAliveTimeout());
			config.setCheckIdleInitialDelay(feInfo.getCheckIdleInitialDelay());
			config.setCheckIdlePeriod(feInfo.getCheckIdlePeriod());
			config.setCloseIdleTimeout(feInfo.getCloseIdleTimeout());
			
			HttpClientProvider provider = new HttpClientProvider(config);
			httpClient = provider.getHttpClient();
			
			entity = new FrontendEntity();
			entity.setFrontendInfo(feInfo);
			entity.setHttpClient(httpClient);
			GlobalObject.FRONTEND_MAP.put(feInfo.getHost(), entity);
		}
	}
}
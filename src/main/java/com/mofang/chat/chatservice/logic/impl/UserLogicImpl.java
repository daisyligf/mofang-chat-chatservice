package com.mofang.chat.chatservice.logic.impl;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import com.mofang.chat.business.redis.UserRedis;
import com.mofang.chat.business.redis.impl.UserRedisImpl;
import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.business.sysconf.ReturnCode;
import com.mofang.chat.business.sysconf.ReturnCodeHelper;
import com.mofang.chat.chatservice.component.FrontendEntity;
import com.mofang.chat.chatservice.global.GlobalObject;
import com.mofang.chat.chatservice.logic.UserLogic;
import com.mofang.framework.net.http.HttpClientSender;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class UserLogicImpl implements UserLogic
{
	private final static UserLogicImpl LOGIC = new UserLogicImpl();
	private UserRedis userRedis = UserRedisImpl.getInstance();
	
	private UserLogicImpl()
	{}
	
	public static UserLogicImpl getInstance()
	{
		return LOGIC;
	}
	
	@Override
	public ResultValue prohibit(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("prohibit_user_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long userId = json.optLong("uid", 0);
			if(0 == userId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			///根据uid获取FE信息
			String feHost = userRedis.getFrontend(userId);
			if(!GlobalObject.FRONTEND_MAP.containsKey(feHost))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				return result;
			}
			
			///为指定FE的指定uid发送消息通知
			JSONObject message = new JSONObject();
			message.put("act", "prohibit_user");
			message.put("uid", userId);
			FrontendEntity entity = GlobalObject.FRONTEND_MAP.get(feHost);
			CloseableHttpClient httpClient = entity.getHttpClient();
			String url = entity.getFrontendInfo().getReqUrl();
			String response = HttpClientSender.post(httpClient, url, message.toString());
			GlobalObject.INFO_LOG.info("req url:" + url + " result:" + response);
			
			///返回
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at UserLogicImpl.prohibit throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue logout(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("user_logout_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long userId = json.optLong("uid", 0);
			if(0 == userId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			///根据uid获取FE信息
			String feHost = userRedis.getFrontend(userId);
			if(!GlobalObject.FRONTEND_MAP.containsKey(feHost))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				return result;
			}
			
			///为指定FE的指定uid发送消息通知
			JSONObject message = new JSONObject();
			message.put("act", "user_logout");
			message.put("uid", userId);
			FrontendEntity entity = GlobalObject.FRONTEND_MAP.get(feHost);
			CloseableHttpClient httpClient = entity.getHttpClient();
			String url = entity.getFrontendInfo().getReqUrl();
			String response = HttpClientSender.post(httpClient, url, message.toString());
			GlobalObject.INFO_LOG.info("req url:" + url + " result:" + response);
			
			///返回
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at UserLogicImpl.logout throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}
}
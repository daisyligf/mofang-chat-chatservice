package com.mofang.chat.chatservice.logic.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.business.redis.RoomRedis;
import com.mofang.chat.business.redis.impl.RoomRedisImpl;
import com.mofang.chat.business.service.RoomService;
import com.mofang.chat.business.service.impl.RoomServiceImpl;
import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.business.sysconf.ReturnCode;
import com.mofang.chat.business.sysconf.ReturnCodeHelper;
import com.mofang.chat.chatservice.global.GlobalObject;
import com.mofang.chat.chatservice.logic.RoomLogic;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class RoomLogicImpl implements RoomLogic
{
	private final static RoomLogicImpl LOGIC = new RoomLogicImpl();
	private RoomService roomService = RoomServiceImpl.getInstance();
	private RoomRedis roomRedis = RoomRedisImpl.getInstance();
	
	private RoomLogicImpl()
	{}
	
	public static RoomLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue subscribe(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("sync_user_room_list_response");
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
			JSONArray array = json.getJSONArray("rid_list");
			if(0 == userId || null == array || array.length() == 0)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			Set<Integer> roomSet = new HashSet<Integer>();
			for(int i=0; i<array.length(); i++)
				roomSet.add(array.getInt(i));
			roomService.subscribe(userId, roomSet);
			
			///返回
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at RoomLogicImpl.subscribe throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue getRoomInfo(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("get_room_info_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			int roomId = json.optInt("rid", 0);
			if(0 == roomId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			long timestamp = roomRedis.getLastTimestamp(roomId);
			int enterUserCount = roomRedis.getEnterUserCount(roomId);
			JSONObject data = new JSONObject();
			data.put("time_stamp", timestamp);
			
			////为推广增加的
			if(roomId == 10050745)
			{
				Random rnd = new Random();
				enterUserCount = 3500 + rnd.nextInt(200);
			}
			data.put("enter_user_count", enterUserCount);
			
			///返回
			result.setData(data);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at RoomLogicImpl.subscribe throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}
}
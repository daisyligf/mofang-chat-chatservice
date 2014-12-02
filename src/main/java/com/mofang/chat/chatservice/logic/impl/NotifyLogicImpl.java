package com.mofang.chat.chatservice.logic.impl;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.business.model.PostReplyNotify;
import com.mofang.chat.business.model.SysMessageNotify;
import com.mofang.chat.business.redis.PushQueueRedis;
import com.mofang.chat.business.redis.impl.PushQueueRedisImpl;
import com.mofang.chat.business.service.PostReplyNotifyService;
import com.mofang.chat.business.service.SysMessageNotifyService;
import com.mofang.chat.business.service.impl.PostReplyNotifyServiceImpl;
import com.mofang.chat.business.service.impl.SysMessageNotifyServiceImpl;
import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.business.sysconf.ReturnCode;
import com.mofang.chat.business.sysconf.ReturnCodeHelper;
import com.mofang.chat.business.sysconf.common.PushDataType;
import com.mofang.chat.chatservice.global.GlobalObject;
import com.mofang.chat.chatservice.logic.NotifyLogic;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class NotifyLogicImpl implements NotifyLogic
{
	private final static NotifyLogicImpl LOGIC = new NotifyLogicImpl();
	private PostReplyNotifyService postReplyNotifyService = PostReplyNotifyServiceImpl.getInstance();
	private SysMessageNotifyService sysMessageNotifyService = SysMessageNotifyServiceImpl.getInstance();
	private PushQueueRedis pushRedis = PushQueueRedisImpl.getInstance();
	
	private NotifyLogicImpl()
	{}
	
	public static NotifyLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue pushPostReplyNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_post_reply_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long userId = json.optLong("uid", 0L);
			if(0 == userId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			JSONObject messageJson = json.getJSONObject("msg");
			if(null == messageJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			JSONObject contentJson = messageJson.getJSONObject("content");
			if(null == contentJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			JSONObject replyContentJson = contentJson.getJSONObject("reply_content");
			if(null == replyContentJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			///构建消息体
			PostReplyNotify notify = new PostReplyNotify();
			notify.setUserId(userId);
			notify.setIsShowNotify(json.optBoolean("is_show_notify", false));
			notify.setClickAction(json.optString("click_act", ""));
			notify.setCreateTime(new Date());
			notify.setMessageType(messageJson.optInt("msg_type", 1));
			notify.setPostId(contentJson.optInt("post_id", 0));
			notify.setPostTitle(contentJson.optString("post_title", ""));
			notify.setReplyId(contentJson.optInt("reply_id", 0));
			notify.setReplyTime(new Date());
			notify.setReplyUserId(contentJson.optLong("reply_uid", 0L));
			notify.setReplyType(contentJson.optInt("reply_type", 0));
			notify.setReplyText(replyContentJson.optString("text", ""));
			JSONArray pics = replyContentJson.getJSONArray("pictures");
			if(null != pics)
			{
				StringBuilder strPics = new StringBuilder();
				for(int i=0; i< pics.length(); i++)
					strPics.append(pics.getString(i) + ",");
				
				notify.setReplyPictures(strPics.length() == 0 ? "" :  strPics.substring(0, strPics.length() - 1));
			}
			
			postReplyNotifyService.pushNotify(notify);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at NotifyLogicImpl.pushPostReplyNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pushSysMessageNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_sys_msg_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			JSONArray userIdList = json.optJSONArray("uid_list");
			if(null == userIdList || userIdList.length() == 0)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			JSONObject messageJson = json.getJSONObject("msg");
			if(null == messageJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			JSONObject contentJson = messageJson.getJSONObject("content");
			if(null == contentJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			long userId;
			SysMessageNotify notify = null;
			for(int i=0; i<userIdList.length(); i++)
			{
				userId = userIdList.getLong(i);
				///构建消息体
				notify = new SysMessageNotify();
				notify.setUserId(userId);
				notify.setIsShowNotify(json.optBoolean("is_show_notify", false));
				notify.setClickAction(json.optString("click_act", ""));
				notify.setMessageType(messageJson.optInt("msg_type", 1));
				notify.setMessageCategory(messageJson.optString("msg_category", ""));
				notify.setTitle(contentJson.optString("title", ""));
				notify.setDetail(contentJson.optString("detail", ""));
				notify.setIcon(contentJson.optString("icon", ""));
				notify.setCreateTime(new Date());
				boolean hasSource = contentJson.has("source");
				if(hasSource)
				{
					JSONObject sourceJson = contentJson.getJSONObject("source");
					notify.setSource(sourceJson.toString());
				}
				sysMessageNotifyService.pushNotify(notify);
			}
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at NotifyLogicImpl.pushSysMessageNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pushGuildGiftNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_guild_gift_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			JSONArray guildList = json.optJSONArray("push_guild");
			if(null == guildList || guildList.length() == 0)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			JSONObject messageJson = json.getJSONObject("msg");
			if(null == messageJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			JSONObject contentJson = messageJson.getJSONObject("content");
			if(null == contentJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			long userId;
			long guildId;
			SysMessageNotify notify = null;
			JSONObject guildJson = null;
			for(int i=0; i<guildList.length(); i++)
			{
				guildJson = guildList.getJSONObject(i);
				if(null == guildJson)
					continue;
				
				guildId = guildJson.getLong("guild_id");
				userId = guildJson.getLong("chairman_uid");
				
				///构建消息体
				notify = new SysMessageNotify();
				notify.setUserId(userId);
				notify.setIsShowNotify(json.optBoolean("is_show_notify", false));
				notify.setClickAction(json.optString("click_act", ""));
				notify.setMessageType(messageJson.optInt("msg_type", 1));
				notify.setMessageCategory(messageJson.optString("msg_category", ""));
				notify.setTitle(contentJson.optString("title", ""));
				notify.setDetail(contentJson.optString("detail", ""));
				notify.setCreateTime(new Date());
				boolean hasSource = contentJson.has("source");
				if(hasSource)
				{
					JSONObject sourceJson = contentJson.getJSONObject("source");
					sourceJson.put("guild_id", guildId);
					notify.setSource(sourceJson.toString());
				}
				sysMessageNotifyService.pushNotify(notify);
			}
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at NotifyLogicImpl.pushGuildGiftNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pushTaskNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_task_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long userId = json.optLong("uid", 0L);
			if(0 == userId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			JSONObject messageJson = json.getJSONObject("msg");
			if(null == messageJson)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			///构建push消息
			JSONObject pushJson = new JSONObject();
			pushJson.put("push_data_type", PushDataType.USER_TASK_NOTIFY);
			pushJson.put("to_uid", userId);
			pushJson.put("msg", messageJson);
			pushJson.put("is_show_notify", json.optBoolean("is_show_notify", false));
			pushJson.put("click_act", json.optString("click_act", ""));
			///添加到push queue
			pushRedis.put(pushJson.toString());
			GlobalObject.INFO_LOG.info("TaskNotifyWorker add push queue message:" + pushJson.toString());
			
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at NotifyLogicImpl.pushTaskNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}
}
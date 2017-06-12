package com.mofang.chat.chatservice.logic.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.business.entity.FriendMessage;
import com.mofang.chat.business.entity.PrivateMessage;
import com.mofang.chat.business.entity.PrivateMessageCollection;
import com.mofang.chat.business.entity.RoomMessage;
import com.mofang.chat.business.entity.User;
import com.mofang.chat.business.redis.PushQueueRedis;
import com.mofang.chat.business.redis.impl.PushQueueRedisImpl;
import com.mofang.chat.business.service.FriendMessageService;
import com.mofang.chat.business.service.PrivateMessageService;
import com.mofang.chat.business.service.RoomMessageService;
import com.mofang.chat.business.service.UserService;
import com.mofang.chat.business.service.impl.FriendMessageServiceImpl;
import com.mofang.chat.business.service.impl.PrivateMessageServiceImpl;
import com.mofang.chat.business.service.impl.RoomMessageServiceImpl;
import com.mofang.chat.business.service.impl.UserServiceImpl;
import com.mofang.chat.business.sysconf.ReturnCode;
import com.mofang.chat.business.sysconf.ReturnCodeHelper;
import com.mofang.chat.business.sysconf.common.ChatType;
import com.mofang.chat.business.sysconf.common.MessageType;
import com.mofang.chat.business.sysconf.common.PushDataType;
import com.mofang.chat.chatservice.global.GlobalObject;
import com.mofang.chat.chatservice.logic.MessageLogic;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class MessageLogicImpl implements MessageLogic
{
	private final static MessageLogicImpl LOGIC = new MessageLogicImpl();
	private final static long MESSAGE_EXPIRE_TIME =  1577808000000L;    ///2020-01-01
	private FriendMessageService friendNotifyService = FriendMessageServiceImpl.getInstance();
	private RoomMessageService roomMessageService = RoomMessageServiceImpl.getInstance();
	private PrivateMessageService privateMessageService = PrivateMessageServiceImpl.getInstance();
	private PushQueueRedis pushRedis = PushQueueRedisImpl.getInstance();
	private UserService userService = UserServiceImpl.getInstance();
	
	private MessageLogicImpl()
	{}
	
	public static MessageLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue pushFriendNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_friend_notify_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long fromUserId = json.optLong("from_uid", 0);
			long toUserId = json.optLong("to_uid", 0);
			if(0 == fromUserId || 0 == toUserId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			///构建消息体
			FriendMessage message = new FriendMessage();
			message.setToUserId(toUserId);
			message.setFromUserId(fromUserId);
			message.setContent(json.optString("content", ""));
			message.setMessageType(json.optInt("msg_type", MessageType.TEXT));
			message.setDuration(0);
			message.setChatType(ChatType.FRIEND);
			message.setShowNotify(json.optBoolean("is_show_notify", false));
			message.setClickAction(json.optString("click_act", ""));
			message.setTimeStamp(System.currentTimeMillis());
			friendNotifyService.sendMessage(message);
			
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.pushFriendNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pullRoomNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("pull_room_notify_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			JSONArray array = json.getJSONArray("rid_list");
			if(null == array || array.length() == 0)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			List<Integer> ridList = new ArrayList<Integer>();
			int roomId;
			for(int i=0; i<array.length(); i++)
			{
				roomId = array.getInt(i);
				ridList.add(roomId);
			}
			List<RoomMessage> messages = roomMessageService.getPullNotify(ridList);
			JSONArray data = new JSONArray();
			JSONObject item = null;
			for(RoomMessage message : messages)
			{
				item = new JSONObject();
				item.put("target_id", message.getRoomId());
				item.put("time_stamp", message.getTimeStamp());
				data.put(item);
			}
			
			result.setData(data);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.pullRoomNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	public ResultValue pullPrivateNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("pull_private_notify_response");
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
			if(0L == userId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				return result;
			}
			
			JSONArray data = new JSONArray();
			JSONObject item = null;
			List<PrivateMessage> messages = privateMessageService.getPullNotify(userId);
			if(null != messages && messages.size() > 0)
			{
				long fromUserId = 0;
				for(PrivateMessage message : messages)
				{
					fromUserId = message.getFromUserId();
					item = new JSONObject();
					item.put("target_id", userId);
					item.put("unread_count", message.getUnreadCount());
					item.put("is_show_notify", message.isShowNotify());
					item.put("click_act", message.getClickAction());
					JSONObject msgJson = new JSONObject();
					msgJson.put("content", message.getContent());
					msgJson.put("msg_type", message.getMessageType());
					msgJson.put("time_stamp", message.getTimeStamp());
					JSONObject userJson = new JSONObject();
					userJson.put("id", fromUserId);
					User user = userService.getInfo(fromUserId);
					if(null != user)
					{
						userJson.put("nick_name", user.getNickName());
						userJson.put("avatar", user.getAvatar());
						userJson.put("type", user.getType());
					}
					msgJson.put("user", userJson);
					item.put("msg", msgJson);
					data.put(item);
				}
			}
			result.setData(data);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.pullPrivateNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}
	
	@Override
	public ResultValue sendGroupSendMessage(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("send_groupsend_message_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			JSONArray array = json.getJSONArray("uid_list");
			long fromUserId = json.optLong("from_uid", 0);
			if(null == array || array.length() == 0)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			if(0 == fromUserId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			long expire = json.optLong("expire", MESSAGE_EXPIRE_TIME);
			long toUserId;
			PrivateMessage message = null;
			for(int i=0; i<array.length(); i++)
			{
				toUserId = array.optLong(i, 0L);
				if(0 == toUserId)
					continue;
				
				///构建消息体
				message = new PrivateMessage();
				message.setToUserId(toUserId);
				message.setFromUserId(fromUserId);
				message.setContent(json.optString("content", ""));
				message.setMessageType(json.optInt("msg_type", MessageType.TEXT));
				message.setDuration(json.optInt("duration", 0));
				message.setChatType(ChatType.PRIVATE);
				message.setShowNotify(json.optBoolean("is_show_notify", false));
				message.setClickAction(json.optString("click_act", ""));
				message.setTimeStamp(System.currentTimeMillis());
				message.setExpireTime(expire);  ///永不过期
				privateMessageService.sendMessage(message);
			}
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.sendGroupSendMessage throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue deleteRoomMessage(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("del_room_msg_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long messageId = json.optLong("msg_id", 0L);
			if(0 == messageId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER);
				return result;
			}
			
			///将消息状态置为已删除
			roomMessageService.updateStatus(messageId, 0);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.deleteRoomMessage throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue sendRoomMessage(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("send_room_message_response");
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
			long fromUserId = json.optLong("from_uid", 0);
			if(0 == roomId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			///构建消息体
			RoomMessage message = new RoomMessage();
			message.setRoomId(roomId);
			message.setFromUserId(fromUserId);
			message.setContent(json.optString("content", ""));
			message.setMessageType(json.optInt("msg_type", MessageType.TEXT));
			message.setDuration(json.optInt("duration", 0));
			message.setFontColor(json.optString("font_color", ""));
			message.setChatType(ChatType.ROOM);
			message.setShowNotify(false);
			message.setClickAction("");
			message.setTimeStamp(System.currentTimeMillis());
			roomMessageService.sendMessage(message);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.sendRoomMessage throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pushRoomActivityNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("push_room_activity_notify_response");
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
			
			///构建push消息
			JSONObject pushMsg = new JSONObject();
			pushMsg.put("push_data_type", PushDataType.ROOM_ACTIVITY_NOTIFY);
			pushMsg.put("rid", roomId);
			///添加到push queue中
			pushRedis.put(pushMsg.toString());
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.pushRoomNotify throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue pullPrivateMessage(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("pull_private_message_response");
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
			long fromUserId = json.optLong("target_id", 0L);
			
			if(0L == userId || 0L == fromUserId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			Long minMsgId = json.optLong("msg_id_min", 0L);
			Long maxMsgId = json.optLong("msg_id_max", Long.MAX_VALUE);
			Integer pageSize = json.optInt("page_size", 50);
			
			///返回聊天消息列表
			JSONObject data = new JSONObject();
			PrivateMessageCollection msgCollection = privateMessageService.getPullMessages(fromUserId, userId, minMsgId, maxMsgId, pageSize);
			if(null != msgCollection)
			{
				data.put("msg_count", msgCollection.getCount());
				JSONArray array = new JSONArray();
				JSONObject item = null;
				List<PrivateMessage> messages = msgCollection.getMessage();
				if(null != messages && messages.size() > 0)
				{
					for(PrivateMessage message : messages)
					{
						item = new JSONObject();
						item.put("msg_id", message.getMessageId());
						item.put("content", message.getContent());
						item.put("msg_type", message.getMessageType());
						item.put("time_stamp", message.getTimeStamp());
						item.put("duration", message.getDuration());
						///构建消息发送者用户信息
						JSONObject userJson = new JSONObject();
						userJson.put("id", message.getFromUserId());
						User user = userService.getInfo(message.getFromUserId());
						if(null != user)
						{
							userJson.put("nick_name", user.getNickName());
							userJson.put("avatar", user.getAvatar());
							userJson.put("type", user.getType());
						}
						item.put("user", userJson);
						array.put(item);
					}
				}
				data.put("msg_list", array);
			}
			result.setData(data);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.pullPrivateMessage throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}

	@Override
	public ResultValue sendPrivateMessage(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		result.setAction("send_private_message_response");
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long toUserId = json.optLong("to_uid", 0);
			long fromUserId = json.optLong("from_uid", 0);
			
			if(0 == fromUserId || 0 == toUserId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR);
				return result;
			}
			
			///构建消息实体
			PrivateMessage message = new PrivateMessage();
			message.setToUserId(toUserId);
			message.setFromUserId(fromUserId);
			message.setContent(json.optString("content", ""));
			message.setMessageType(json.optInt("msg_type", MessageType.TEXT));
			message.setDuration(json.optInt("duration", 0));
			message.setChatType(ChatType.PRIVATE);
			message.setShowNotify(json.optBoolean("is_show_notify", false));
			message.setClickAction(json.optString("click_act", ""));
			message.setTimeStamp(System.currentTimeMillis());
			message.setExpireTime(MESSAGE_EXPIRE_TIME);  ///永不过期
			long messageId = privateMessageService.sendMessage(message);
			JSONObject data = new JSONObject();
			data.put("msg_id", messageId);
			result.setData(data);
			result.setCode(ReturnCode.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at MessageLogicImpl.sendPrivateMessage throw an error. parameter:" + postData, e);
			return ReturnCodeHelper.serverError(result);
		}
	}
}
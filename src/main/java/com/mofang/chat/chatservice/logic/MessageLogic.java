package com.mofang.chat.chatservice.logic;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface MessageLogic
{
	/**
	 * 推送好友申请/处理结果通知
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue pushFriendNotify(HttpRequestContext context) throws Exception;
	
	/**
	 * 发送公聊消息
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue sendRoomMessage(HttpRequestContext context) throws Exception;
	
	/**
	 * 拉取公聊通知
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue pullRoomNotify(HttpRequestContext context) throws Exception;
	
	/**
	 * 推送公聊活动通知
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue pushRoomActivityNotify(HttpRequestContext context) throws Exception;
	
	/**
	 * 运营后台群发消息
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue sendGroupSendMessage(HttpRequestContext context) throws Exception;
	
	/**
	 * 删除房间消息
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public ResultValue deleteRoomMessage(HttpRequestContext context) throws Exception;
}
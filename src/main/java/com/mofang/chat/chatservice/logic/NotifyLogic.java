package com.mofang.chat.chatservice.logic;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface NotifyLogic
{
	public ResultValue pushPostReplyNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue pushSysMessageNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue pushGuildGiftNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue pushTaskNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue getPostReplyNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue getSysMessageNotify(HttpRequestContext context) throws Exception;
}
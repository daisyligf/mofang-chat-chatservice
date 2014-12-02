package com.mofang.chat.chatservice.logic;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface RoomLogic
{
	public ResultValue subscribe(HttpRequestContext context) throws Exception;
	
	public ResultValue getRoomInfo(HttpRequestContext context) throws Exception;
}
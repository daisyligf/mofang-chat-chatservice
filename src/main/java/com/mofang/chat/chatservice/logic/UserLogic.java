package com.mofang.chat.chatservice.logic;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface UserLogic
{
	public ResultValue prohibit(HttpRequestContext context) throws Exception;
	
	public ResultValue logout(HttpRequestContext context) throws Exception;
}
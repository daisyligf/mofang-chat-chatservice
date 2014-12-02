package com.mofang.chat.chatservice.controller;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.chatservice.logic.UserLogic;
import com.mofang.chat.chatservice.logic.impl.UserLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;
import com.mofang.framework.web.server.reactor.context.RequestContext;

/**
 * 
 * @author zhaodx
 *
 */
@Action(url="prohibit_user")
public class ProhibitUserAction extends AbstractActionExecutor
{
	private UserLogic logic = UserLogicImpl.getInstance();
	
	@Override
	protected ResultValue exec(RequestContext context) throws Exception
	{
		HttpRequestContext ctx = (HttpRequestContext)context;
		return logic.prohibit(ctx);
	}
}
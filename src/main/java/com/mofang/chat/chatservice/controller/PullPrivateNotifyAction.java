package com.mofang.chat.chatservice.controller;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.chatservice.logic.MessageLogic;
import com.mofang.chat.chatservice.logic.impl.MessageLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;
import com.mofang.framework.web.server.reactor.context.RequestContext;

/**
 * 
 * @author zhaodex
 *
 */
@Action(url="pull_private_notify")
public class PullPrivateNotifyAction extends AbstractActionExecutor
{
	private MessageLogic logic = MessageLogicImpl.getInstance();

	@Override
	protected ResultValue exec(RequestContext context) throws Exception
	{
		HttpRequestContext ctx = (HttpRequestContext)context;
		return logic.pullPrivateNotify(ctx);
	}
}
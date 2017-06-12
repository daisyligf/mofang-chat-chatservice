package com.mofang.chat.chatservice.controller;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.chatservice.logic.NotifyLogic;
import com.mofang.chat.chatservice.logic.impl.NotifyLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;
import com.mofang.framework.web.server.reactor.context.RequestContext;

/**
 * 
 * @author zhaodx
 *
 */
@Action(url="pull_feed_recommend_notify")
public class PullFeedRecommendNotifyAction extends AbstractActionExecutor
{
	private NotifyLogic logic = NotifyLogicImpl.getInstance();

	@Override
	protected ResultValue exec(RequestContext context) throws Exception
	{
		HttpRequestContext ctx = (HttpRequestContext)context;
		return logic.getFeedRecommendNotify(ctx);
	}
}
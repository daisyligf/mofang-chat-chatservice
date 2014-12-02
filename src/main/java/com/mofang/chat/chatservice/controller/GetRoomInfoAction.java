package com.mofang.chat.chatservice.controller;

import com.mofang.chat.business.sysconf.ResultValue;
import com.mofang.chat.chatservice.logic.RoomLogic;
import com.mofang.chat.chatservice.logic.impl.RoomLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;
import com.mofang.framework.web.server.reactor.context.RequestContext;

/**
 * 
 * @author zhaodx
 *
 */
@Action(url="get_room_info")
public class GetRoomInfoAction extends AbstractActionExecutor
{
	private RoomLogic logic = RoomLogicImpl.getInstance();

	@Override
	protected ResultValue exec(RequestContext context) throws Exception
	{
		HttpRequestContext ctx = (HttpRequestContext)context;
		return logic.getRoomInfo(ctx);
	}
}
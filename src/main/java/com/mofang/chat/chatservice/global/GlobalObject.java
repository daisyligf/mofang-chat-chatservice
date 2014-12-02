package com.mofang.chat.chatservice.global;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mofang.chat.chatservice.component.FrontendEntity;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalObject
{
	/**
	 * Global Info Logger Instance 
	 */
	public final static Logger INFO_LOG = Logger.getLogger("chatservice.info");
	
	/**
	 * Global Error Logger Instance
	 */
	public final static Logger ERROR_LOG = Logger.getLogger("chatservice.error");
	
	/**
	 * Global Frontend Map Instance
	 */
	public final static Map<String, FrontendEntity> FRONTEND_MAP = new HashMap<String, FrontendEntity>();
}
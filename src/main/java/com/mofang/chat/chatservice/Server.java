package com.mofang.chat.chatservice;

import com.mofang.chat.chatservice.action.HttpActionResolve;
import com.mofang.chat.chatservice.init.Initializer;
import com.mofang.chat.chatservice.init.impl.MainInitializer;
import com.mofang.chat.chatservice.global.GlobalConfig;
import com.mofang.framework.web.server.action.ActionResolve;
import com.mofang.framework.web.server.conf.ChannelConfig;
import com.mofang.framework.web.server.main.WebServer;
import com.mofang.framework.web.server.reactor.parse.PostDataParserType;

/**
 * 
 * @author zhaodx
 *
 */
public class Server
{
	public static void main(String[] args)
	{
		//String configpath = "/Users/milo/document/workspace/mofang.chat.chatservice/src/main/resources/config.ini";
		
		if(args.length <= 0)
		{
			System.out.println("usage:java -server -Xms1024m -Xmx1024m -jar mofang-chat-chatservice.jar configpath");
			System.exit(1);
		}
		String configpath = args[0];
		
		try
		{
			///服务器初始化
			System.out.println("prepare to initializing config......");
			Initializer initializer = new MainInitializer(configpath);
			initializer.init();
			System.out.println("initialize config completed!");
			
			///启动服务器
			ActionResolve httpActionResolve = new HttpActionResolve();
			int port = GlobalConfig.SERVER_PORT;
			WebServer server = new WebServer(port, PostDataParserType.Json);
			
			ChannelConfig channelConfig = new ChannelConfig();
			channelConfig.setConnTimeout(GlobalConfig.CONN_TIMEOUT);
			channelConfig.setSoTimeout(GlobalConfig.READ_TIMEOUT);
			server.setChannelConfig(channelConfig);
			server.setScanPackagePath(GlobalConfig.SCAN_PACKAGE_PATH);
			server.setHttpActionResolve(httpActionResolve);
			try
			{
				System.out.println("Chat Server Start on " + GlobalConfig.SERVER_PORT);
				server.start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			System.out.println("chat server start error. message:" + e.getMessage());
		}
	}
}
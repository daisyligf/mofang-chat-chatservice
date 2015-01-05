package com.mofang.chat.chatservice.init.impl;

import java.io.IOException;

import com.mofang.chat.chatservice.init.AbstractInitializer;
import com.mofang.chat.chatservice.global.GlobalConfig;
import com.mofang.framework.util.IniParser;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalConfigInitializer extends AbstractInitializer
{
	private String configPath;
	
	public GlobalConfigInitializer(String configPath)
	{
		this.configPath = configPath;
	}
	
	@Override
	public void load() throws IOException 
	{
		IniParser config = new IniParser(configPath);
		GlobalConfig.SERVER_PORT = config.getInt("common", "server_port");
		GlobalConfig.CONN_TIMEOUT = config.getInt("common", "conn_timeout");
		GlobalConfig.READ_TIMEOUT = config.getInt("common", "read_timeout");
		
		GlobalConfig.SCAN_PACKAGE_PATH = config.get("conf", "scan_package_path");
		GlobalConfig.MYSQL_CONFIG_PATH = config.get("conf", "mysql_config_path");
		GlobalConfig.WRITE_QUEUE_CONFIG_PATH = config.get("conf", "write_queue_config_path");
		GlobalConfig.REDIS_MASTER_CONFIG_PATH = config.get("conf", "redis_master_config_path");
		GlobalConfig.REDIS_SLAVE_CONFIG_PATH = config.get("conf", "redis_slave_config_path");
		GlobalConfig.PUSH_QUEUE_CONFIG_PATH = config.get("conf", "push_queue_config_path");
		GlobalConfig.LOG4J_CONFIG_PATH = config.get("conf", "log4j_config_path");
		GlobalConfig.FESERVER_CONFIG_PATH = config.get("conf", "feserver_config_path");
		GlobalConfig.HTTP_CLIENT_CONFIG_PATH = config.get("conf", "http_client_config_path");
		
		GlobalConfig.USER_AUTH_URL = config.get("api", "user_auth_url");
		GlobalConfig.USER_INFO_URL = config.get("api", "user_info_url");
		GlobalConfig.ALLOW_SEND_URL = config.get("api", "allow_send_url");
	}
}
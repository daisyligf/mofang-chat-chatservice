package com.mofang.chat.chatservice.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mofang.chat.chatservice.global.GlobalObject;

/**
 * 
 * @author zhaodx
 *
 */
public class FrontendProvider
{
	@SuppressWarnings("unchecked")
	public List<FrontendInfo> getFrontendList(String configPath)
	{
		Document doc = null;
		try
		{
			SAXReader reader = new SAXReader();
			doc = reader.read(new File(configPath));
			Element root = doc.getRootElement();
			Element second = root.element("items");
			List<Element> eleList = second.elements();
			List<FrontendInfo> frontendList = new ArrayList<FrontendInfo>();
			FrontendInfo feInfo = null;
			
			String host = null;
			Integer port = null;
			String pushUrl = null;
			int maxTotal = 100;
			String charset = "utf-8";
			int connTimeout = 30000;
			int socketTimeout = 30000;
			int defaultKeepAliveTimeout = 30000;
			int checkIdleInitialDelay = 5000;
			int checkIdlePeriod = 5000;
			int closeIdleTimeout = 30000;
			for(Element item : eleList)
			{
				host = getElementText(item, "host");
				port = Integer.valueOf(getElementText(item, "port"));
				pushUrl = getElementText(item, "req_url");
				maxTotal = Integer.valueOf(getElementText(item, "max_total"));
				charset = getElementText(item, "charset");
				connTimeout = Integer.valueOf(getElementText(item, "conn_timeout"));
				socketTimeout = Integer.valueOf(getElementText(item, "socket_timeout"));
				defaultKeepAliveTimeout = Integer.valueOf(getElementText(item, "keepalive_timeout"));
				checkIdleInitialDelay = Integer.valueOf(getElementText(item, "check_idle_initial_delay"));
				checkIdlePeriod = Integer.valueOf(getElementText(item, "check_idle_period"));
				closeIdleTimeout = Integer.valueOf(getElementText(item, "close_idle_timeout"));
				
				feInfo = new FrontendInfo();
				feInfo.setHost(host);
				feInfo.setPort(port);
				feInfo.setReqUrl(pushUrl);
				feInfo.setMaxTotal(maxTotal);
				feInfo.setCharset(charset);
				feInfo.setConnTimeout(connTimeout);
				feInfo.setSocketTimeout(socketTimeout);
				feInfo.setDefaultKeepAliveTimeout(defaultKeepAliveTimeout);
				feInfo.setCheckIdleInitialDelay(checkIdleInitialDelay);
				feInfo.setCheckIdlePeriod(checkIdlePeriod);
				feInfo.setCloseIdleTimeout(closeIdleTimeout);
				frontendList.add(feInfo);
			}
			return frontendList;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at FrontendProvider.getFrontendList throw an error.", e);
			return null;
		}
	}
	
	private String getElementText(Element parent, String nodeName)
	{
		Element element = parent.element(nodeName);
		if(element == null)
			return null;
		
		return element.getText().trim();
	}
}
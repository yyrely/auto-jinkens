package com.chuncongcong;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.ws.WebServiceException;

import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author HU
 * @date 2019/12/11 17:30
 */

public class Main {

	public static void main(String[] args) throws Exception {
		if(args == null || args.length == 0) {
			throw new IllegalArgumentException("参数异常,[0]-projectName");
		}
		String projectName = args[0];

		Properties userInfo = new Properties();
		InputStream fileInputStream = Main.class.getClassLoader().getResourceAsStream("userInfoConfig.properties");
		userInfo.load(fileInputStream);
		String userName = userInfo.getProperty("userName");
		String password = userInfo.getProperty("password");

		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		//登录获取cookie
		HttpPost loginRequest = new HttpPost("http://192.168.100.102:8080/jenkins/j_acegi_security_check");
		StringEntity loginReqEntity = new StringEntity("j_username="+userName+"&j_password="+password+"&from=/jenkins/&Submit=Sign in");
		loginRequest.setEntity(loginReqEntity);
		loginReqEntity.setContentType("application/x-www-form-urlencoded");
		httpClient.execute(loginRequest);
		String JSESSIONID = "";
		for(Cookie cookie : cookieStore.getCookies()) {
			if(cookie.getValue() != null && "JSESSIONID".equals(cookie.getValue())) {
				JSESSIONID = cookie.getValue();
			}
		}

		//请求构建
		HttpPost request = new HttpPost("http://192.168.100.102:8080/jenkins/job/"+ projectName +"/build?delay=0sec");
		String cookie = "JSESSIONID="+JSESSIONID+"; screenResolution=1536x864";
		request.setHeader("Cookie", cookie);
		CloseableHttpResponse response = httpClient.execute(request);
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() == 403) {
			throw new WebServiceException("权限不足,请检查用户名密码,请修改userInfoConfig.properties");
		} else if(statusLine.getStatusCode() == 404) {
			throw new WebServiceException("项目不存在,请检查,[0]-projectName");
		} else if (statusLine.getStatusCode() == 201) {
			System.out.println("构建成功");
		} else {
			throw new WebServiceException("未知异常");
		}
	}
}

package io.mithrilcoin.api.config.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;


/**
 * 인터페이스 통신 rest api configuration class
 *
 */
@Configuration
//@PropertySource("classpath:apiAccess.properties")
public class RestApiConfiguration {
	
	@Autowired
	Environment env;
	
//	@Autowired
//	private AccessInfo accessInfo;
	
	@SuppressWarnings("unused")
	private RestOperations getRestOperation(int readTimeout){
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(1000);
		factory.setReadTimeout(readTimeout);
		RestTemplate restTemplate = new RestTemplate(factory);
		return restTemplate;
	}
	
//	@Bean(name="gatewayAPIRestTemplate", autowire = Autowire.BY_NAME)
//	public IRestTemplate gatewayAPIRestTemplate(){
//		return new APIRestTemplate(
//				getRestOperation(env.getProperty("gatewayApi.timeout	", int.class, 60000))
//				, env.getProperty("gatewayApi.host")
//				, env.getProperty("gatewayApi.port")
//				, accessInfo
//				);
//	}
//	
//	@Bean(name="bizMailAPIRestTemplate", autowire = Autowire.BY_NAME)
//	public IRestTemplate bizMailAPIRestTemplate(){
////		biz.mail.host=http://www.bizmailer.co.kr
////		biz.mail.port=80
////		biz.mail.time=15000
//		return new APIRestTemplate(
//				getRestOperation(env.getProperty("biz.mail.host.timeout	", int.class, 15000))
//				, env.getProperty("biz.mail.host")
//				, env.getProperty("biz.mail.port")
//				, null
//				);
//	}

}

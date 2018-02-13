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
public class RestApiConfiguration {
	
	@SuppressWarnings("unused")
	private RestOperations getRestOperation(int readTimeout){
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(1000);
		factory.setReadTimeout(readTimeout);
		RestTemplate restTemplate = new RestTemplate(factory);
		return restTemplate;
	}

}

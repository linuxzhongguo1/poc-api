package io.mithrilcoin.api.common.fcm;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import io.mithrilcoin.api.biz.message.handler.AsyncRequestInterceptor;
import io.mithrilcoin.api.biz.message.handler.HeaderRequestInterceptor;

public class FirebaseSender {

	private AsyncRestTemplate asyncRestTemplate;
	private Gson gson;
	private String firebase_server_key;
	private String firebase_api_url;
	
	private static Logger logger = LoggerFactory.getLogger(FirebaseSender.class);

	
	public FirebaseSender(String firebase_server_key, String firebase_api_url) {
		this.firebase_server_key = firebase_server_key;
		this.firebase_api_url = firebase_api_url;
		
		ArrayList<AsyncClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new AsyncRequestInterceptor("Authorization", "key=" + firebase_server_key));
		interceptors.add(new AsyncRequestInterceptor("Content-Type", "application/json"));
		
		asyncRestTemplate = new AsyncRestTemplate();
		//asyncRestTemplate.setAsyncRequestFactory(new Netty4ClientHttpRequestFactory());
		
		asyncRestTemplate.setInterceptors(interceptors);
		gson = new Gson();
	}

	public String send(HttpEntity<String> entity) {

		RestTemplate restTemplate = new RestTemplate();

		ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + firebase_server_key));
		interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
		restTemplate.setInterceptors(interceptors);

		String firebaseResponse = restTemplate.postForObject(firebase_api_url, entity, String.class);

		return firebaseResponse;
		// return CompletableFuture.completedFuture(firebaseResponse);
	}

	@Async
	public void asyncSend(HttpEntity<String> request) {
		// 비동기 전달
		ListenableFuture<ResponseEntity<String>> entity = asyncRestTemplate.postForEntity(firebase_api_url, request,
				String.class);
		
		entity.addCallback(result -> {
			
			logger.info("FCM state : "+ result.getStatusCode());
			logger.info("FCM Result : "+ result.getBody());
			
		}, ex -> logger.error(ex.getMessage()));
		
		
	
	}
	
	

}

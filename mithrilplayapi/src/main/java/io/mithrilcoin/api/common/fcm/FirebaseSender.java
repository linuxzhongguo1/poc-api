package io.mithrilcoin.api.common.fcm;

import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import io.mithrilcoin.api.biz.message.handler.HeaderRequestInterceptor;

public class FirebaseSender {

	private String firebase_server_key;
	private String firebase_api_url;

	
	public FirebaseSender(String firebase_server_key, String firebase_api_url) {
		this.firebase_server_key = firebase_server_key;
		this.firebase_api_url = firebase_api_url;
	}

	public String send(HttpEntity<String> entity) {

		RestTemplate restTemplate = new RestTemplate();

		ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + firebase_server_key));
		interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
		restTemplate.setInterceptors(interceptors);

		String firebaseResponse = restTemplate.postForObject(firebase_api_url, entity, String.class);

		return firebaseResponse;
		//return CompletableFuture.completedFuture(firebaseResponse);
	}

}

package io.mithrilcoin.api.config.fcm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

import io.mithrilcoin.api.common.fcm.FirebaseSender;

@Configuration
public class FirebaseConfig {
	
	@Value("${google.firebase.serverkey}")
	private String firebase_server_key;
	@Value("${google.firebase.url}")
	private String firebase_api_url;
	
	
	@Bean
	public FirebaseSender firebaseSender()
	{
		FirebaseSender sender = new FirebaseSender(firebase_server_key, firebase_api_url);
		return sender;
	}
	
}

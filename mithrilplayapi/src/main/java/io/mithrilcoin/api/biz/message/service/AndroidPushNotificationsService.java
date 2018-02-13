package io.mithrilcoin.api.biz.message.service;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.message.mapper.MessageMapper;
import io.mithrilcoin.api.common.fcm.FirebaseSender;
import io.mithrilcoin.api.util.DateUtil;


@Service("pushService")
public class AndroidPushNotificationsService implements MessageService{

	private static Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	@Autowired
	private FirebaseSender sender;
	
	@Autowired
	private MessageMapper messageMapper;
	
	@Autowired
	private DateUtil dateutil;
	
	@Override
	public Message sendMessage(Message message) {
		
		try
		{
			
			logger.info("AndroidPushNotificationsService : sendMessage to " + message.getReceiver());
			insertMessage(message);
			JSONObject body = new JSONObject();
			//body.put("to", "fCDNjliwAXI:APA91bF_CnuoAza5enNA5uZgzOI61S-S2eVHLiw9sTFlxu6evxyJCFK1p41CnCCa3M0CrK62LXJxBZj1jydffCHzDKDyRE6QtcdqgJJulgbFnKHsRUETSDL1yIqoK9pa4touOM-2PVky");
			body.put("to", message.getReceiver());
			/*JSONObject notification = new JSONObject();
			notification.put("title", "This is from server");
			notification.put("body", "Test Message!");*/
			
			JSONObject data = new JSONObject();
			data.put("title", message.getTitle());
			data.put("message", message.getBody());
			data.put("type", message.getTypecode());
			data.put("url", "http://url");
			data.put("query", "select * from user");
			
			//body.put("notification", notification);
			body.put("data", data);
			
			HttpEntity<String> request = new HttpEntity<>(body.toString());

			String pushNotification = sender.send(request);
			
			message.setBody(pushNotification);
			
			message.setState("M002003");
			updateMessage(message);
			return message;
			//pushNotification.thenApply()
			//CompletableFuture.allOf(pushNotification).join();
			
			//String firebaseResponse = pushNotification.get();
		}
		catch(Exception e)
		{
			message.setState("M002004");
			updateMessage(message);
		}
		
		return message;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	private Message insertMessage(Message message) {

		logger.info("AndroidPushNotificationsService : insertMessage " + message.getReceiver());
		message.setState("M002002");
		message.setRegistDate(dateutil.getUTCNow());
		messageMapper.insertMessage(message);
		logger.info("AndroidPushNotificationsService : insertMessage " + message.getIdx());

		return message;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	private Message updateMessage(Message message) {

		messageMapper.updateMessage(message);
		return message;
	}
	

	@Override
	public boolean isValidRequest(Message message) {
		
		return false;
	}

}

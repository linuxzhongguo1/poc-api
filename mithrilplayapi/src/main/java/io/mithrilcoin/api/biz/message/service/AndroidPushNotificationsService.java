package io.mithrilcoin.api.biz.message.service;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;

import io.mithril.vo.member.Device;
import io.mithril.vo.message.FCMMessage;
import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.message.mapper.MessageMapper;
import io.mithrilcoin.api.common.fcm.FirebaseSender;
import io.mithrilcoin.api.util.DateUtil;

@Service("pushService")
public class AndroidPushNotificationsService implements MessageService {

	private static Logger logger = LoggerFactory.getLogger(MessageService.class);

	@Autowired
	private FirebaseSender sender;

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private DateUtil dateutil;

	@Override
	public Message sendMessage(Message message) {

		try {
			logger.info("AndroidPushNotificationsService : sendMessage to " + message.getReceiver());

			JSONObject body = new JSONObject();
			// body.put("to",
			// "fCDNjliwAXI:APA91bF_CnuoAza5enNA5uZgzOI61S-S2eVHLiw9sTFlxu6evxyJCFK1p41CnCCa3M0CrK62LXJxBZj1jydffCHzDKDyRE6QtcdqgJJulgbFnKHsRUETSDL1yIqoK9pa4touOM-2PVky");
			body.put("to", message.getReceiver());
			/*
			 * JSONObject notification = new JSONObject(); notification.put("title",
			 * "This is from server"); notification.put("body", "Test Message!");
			 */

			JSONObject data = new JSONObject();
			data.put("title", message.getTitle());
			data.put("message", message.getBody());
			data.put("type", message.getTypecode());
			data.put("url", message.getUrl());
			data.put("query", message.getQuery());
			// data.put("url", "http://url");
			// data.put("query", "select * from package");

			insertMessage(message);
			data.put("idx", message.getIdx());
			body.put("data", data);

			if (message.getReceiver() == null || message.getReceiver() == "") // 전체 전송
			{
				// 1 목록 추출
				// 2 N 건 씩 끊어서 인서트
				// 3 N 건 비동기 푸쉬 전송
				sendAsyncFcmMessage(body);
			} else // 단건 전송
			{
				insertMessage(message);
				HttpEntity<String> request = new HttpEntity<>(body.toString());

				String pushNotification = sender.send(request);
				Gson gson = new Gson();

				FCMMessage resultMessage = gson.fromJson(pushNotification, FCMMessage.class);
				if (resultMessage.getSuccess() > 0) {
					message.setState("M002003");
				} else {
					message.setBody(pushNotification);
					message.setState("M002004");
				}
				updateMessage(message);
				return message;
			}
			// pushNotification.thenApply()
			// CompletableFuture.allOf(pushNotification).join();

			// String firebaseResponse = pushNotification.get();
		} catch (Exception e) {
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

	@Async
	private void sendMessageAsync(ArrayList<Device> devicelist, JSONObject body) throws JSONException {
		for (Device device : devicelist) {
			if (device.getFcmid() != null && device.getFcmid() != "") {
				body.put("to", device.getFcmid());
				HttpEntity<String> request = new HttpEntity<>(body.toString());
				sender.asyncSend(request);
			}
		}
	}

	private void sendAsyncFcmMessage(JSONObject body) throws Exception {
		int pagecount = 0;
		// 일단 1000건씩
		int size = 1000;
		long lastIndex = 0;
		ArrayList<Device> list = memberMapper.selectDeviceFcmId(pagecount, size, (int) lastIndex);
		if (list.size() == 0) {
			return;
		}
		while (list.size() > 0) {

			sendMessageAsync(list, body);
			pagecount = pagecount + size;
			lastIndex = list.get(list.size() - 1).getIdx();
			list = memberMapper.selectDeviceFcmId(pagecount, size, (int) lastIndex);
		}
	}

	@Override
	public boolean isValidRequest(Message message) {

		return false;
	}

}

package io.mithrilcoin.api.biz.message.service;

import io.mithril.vo.message.Message;

public interface MessageService {

	public Message sendMessage(Message message);
	
	public boolean isValidRequest(Message message);
	
}

package io.mithrilcoin.api.biz.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.message.mapper.MessageMapper;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class ResponseMessageService {

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	private DateUtil dateUtil;
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Message insertResponse(Message message) {
		
		message.setRegistdate(dateUtil.getUTCNow());
		messageMapper.insertMessageResponse(message);

		return message;
	}

}

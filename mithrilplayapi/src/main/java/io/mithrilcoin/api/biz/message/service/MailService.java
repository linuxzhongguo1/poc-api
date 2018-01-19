package io.mithrilcoin.api.biz.message.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.message.mapper.MessageMapper;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service("mailService")
public class MailService implements MessageService {

	private static Logger logger = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	public JavaMailSender emailSender;

	@Override
	public Message sendMessage(Message message) {

		logger.info("MailService : sendMessage to " + message.getReceiver());

		MimeMessage mail = emailSender.createMimeMessage();
		try {
			// 전송 대기중 상태로 인서트
			insertMessage(message);
			mailPacking(message, mail);
			emailSender.send(mail);
			message.setState("M002003");
			updateMessage(message);
			return message;

		} catch (MessagingException | MailException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		message.setState("M002004");
		updateMessage(message);

		return message;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	private Message insertMessage(Message message) {

		logger.info("MailService : insertMessage " + message.getReceiver());
		message.setState("M002002");
		messageMapper.insertMessage(message);
		logger.info("MailService : insertMessage " + message.getIdx());

		return message;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	private Message updateMessage(Message message) {

		messageMapper.updateMessage(message);
		return message;
	}

	private void mailPacking(Message message, MimeMessage mail) throws MessagingException {
		mail.setSubject(message.getTitle());
		MimeMessageHelper helper;
		helper = new MimeMessageHelper(mail, true);
		helper.setFrom(message.getSender());
		helper.setTo(message.getReceiver());
		helper.setText(message.getBody(), true);
	}

}

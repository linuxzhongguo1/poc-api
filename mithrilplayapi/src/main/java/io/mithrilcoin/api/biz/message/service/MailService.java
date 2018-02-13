package io.mithrilcoin.api.biz.message.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.message.mapper.MessageMapper;
import io.mithrilcoin.api.util.DateUtil;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

@Service("mailService")
public class MailService implements MessageService {

	private static Logger logger = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	public JavaMailSender emailSender;
	
	@Autowired
	private DateUtil dateutil;

	@Override
	public Message sendMessage(Message message) {
		
		if( isValidRequest(message))
		{
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
		}
		
		return message;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	private Message insertMessage(Message message) {

		logger.info("MailService : insertMessage " + message.getReceiver());
		message.setState("M002002");
		message.setRegistDate(dateutil.getUTCNow());
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

	@Override
	public boolean isValidRequest(Message message) {

		return 	isAddressValid(message.getReceiver(), message.getSender());
	}

	private ArrayList<String> getMX(String hostName) throws NamingException {
		// Perform a DNS lookup for MX records in the domain
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] { "MX" });
		Attribute attr = attrs.get("MX");
		// if we don't have an MX record, try the machine itself
		if ((attr == null) || (attr.size() == 0)) {
			attrs = ictx.getAttributes(hostName, new String[] { "A" });
			attr = attrs.get("A");
			if (attr == null)
				throw new NamingException("No match for name '" + hostName + "'");
		}
		// Huzzah! we have machines to try. Return them as an array list
		// NOTE: We SHOULD take the preference into account to be absolutely
		// correct. This is left as an exercise for anyone who cares.
		ArrayList<String> res = new ArrayList<String>();
		NamingEnumeration<?> en = attr.getAll();
		while (en.hasMore()) {
			String x = (String) en.next();
			String f[] = x.split(" ");
			if (f[1].endsWith("."))
				f[1] = f[1].substring(0, (f[1].length() - 1));
			res.add(f[1]);
		}
		return res;
	}

	public boolean isAddressValid(String address, String senderAddress) {
		// Find the separator for the domain name
		int pos = address.indexOf('@');
		// If the address does not contain an '@', it's not valid
		if (pos == -1)
			return false;
		// Isolate the domain/machine name and get a list of mail exchangers
		String domain = address.substring(++pos);
		ArrayList<String> mxList = null;
		try {
			mxList = getMX(domain);
		} catch (NamingException ex) {
			return false;
		}
		// Just because we can send mail to the domain, doesn't mean that the
		// address is valid, but if we can't, it's a sure sign that it isn't
		if (mxList.size() == 0)
			return false;
		// Now, do the SMTP validation, try each mail exchanger until we get
		// a positive acceptance. It *MAY* be possible for one MX to allow
		// a message [store and forwarder for example] and another [like
		// the actual mail server] to reject it. This is why we REALLY ought
		// to take the preference into account.
		for (int mx = 0; mx < mxList.size(); mx++) {
			boolean valid = false;
			try {
				int res;
				Socket skt = new Socket((String) mxList.get(mx), 25);
				BufferedReader rdr = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				BufferedWriter wtr = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
				res = hear(rdr);
				if (res != 220) {
					rdr.close();
					wtr.close();
					skt.close();
					return false;
				}
				// throw new Exception("Invalid header");

				say(wtr, "EHLO gmail.com");
				res = hear(rdr);
				if (res != 250) {
					rdr.close();
					wtr.close();
					skt.close();
					return false;
					// throw new Exception("Not ESMTP");
				}
				// validate the sender address
				say(wtr, "MAIL FROM: <" + senderAddress + ">");
				res = hear(rdr);
				if (res != 250) {
					rdr.close();
					wtr.close();
					skt.close();
					return false;
				}
				// throw new Exception("Sender rejected");
				say(wtr, "RCPT TO: <" + address + ">");
				res = hear(rdr);
				// be polite
				say(wtr, "RSET");
				hear(rdr);
				say(wtr, "QUIT");
				hear(rdr);
				if (res != 250) {
					rdr.close();
					wtr.close();
					skt.close();
					return false;
				}
				// throw new Exception("Address is not valid!");
				valid = true;
				rdr.close();
				wtr.close();
				skt.close();
			} catch (Exception ex) {
				// Do nothing but try next host
			} finally {
				if (valid)
					return true;

			}
		}
		return false;
	}

	private int hear(BufferedReader in) throws IOException {
		String line = null;
		int res = 0;
		while ((line = in.readLine()) != null) {
			String pfx = line.substring(0, 3);
			try {
				res = Integer.parseInt(pfx);
			} catch (Exception ex) {
				res = -1;
			}
			if (line.charAt(3) != '-')
				break;
		}
		return res;
	}

	private void say(BufferedWriter wr, String text) throws IOException {
		wr.write(text + "\r\n");
		wr.flush();
		return;
	}

}

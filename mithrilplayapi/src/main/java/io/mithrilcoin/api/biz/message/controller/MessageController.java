package io.mithrilcoin.api.biz.message.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.message.Message;
import io.mithrilcoin.api.biz.message.service.MessageService;
import io.mithrilcoin.api.biz.message.service.ResponseMessageService;

@RestController
@RequestMapping("/message")
public class MessageController {

	@Autowired
	@Qualifier("mailService")
	private MessageService mailService;
	
	@Autowired
	@Qualifier("pushService")
	private MessageService pushService;
	
	@Autowired
	private ResponseMessageService responseService;
	
	
	@PostMapping("/send/{accessPoint}/{idx}")
	public Message sendMessage(@RequestBody Message message,@PathVariable String accessPoint, @PathVariable String idx)
	{
		// 메일  일 경우 메일 서비스 호출 
		if(message.getTypecode().equals("T001001"))
		{
			message = mailService.sendMessage(message);
		}
		else
		{
			message = pushService.sendMessage(message);
		}
		return message;
	}
	
	@PostMapping("/insert/response/{accessPoint}/{idx}")
	public Message insertResponse(@RequestBody Message message,@PathVariable String accessPoint, @PathVariable String idx)
	{
		if( message.getIdx() > 0)
		{
			responseService.insertResponse(message);
		}
		else
		{
			message.setIdx(-1);
			return message;
		}
		
		return message;
	}
	
	
//	
//	@GetMapping("/select/")
//	public ArrayList<Message> selectMessage(Message message)
//	{
//		
//	}
}

package io.mithrilcoin.api.biz.message.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import io.mithril.vo.message.Message;

@Repository
public interface MessageMapper {

	public int insertMessage(Message message);
	
	public int updateMessage(Message message);

	public ArrayList<Message> selectMessage(Message message);

}

package io.mithrilcoin.api.biz.transaction.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.blockchain.Transaction;
import io.mithrilcoin.api.biz.transaction.mapper.TransactionMapper;
import io.mithrilcoin.api.util.DateUtil;


@Service
public class TransactionService {
	
	@Autowired
	private TransactionMapper transactionMapper;
	
	@Autowired
	private DateUtil dateutil;
	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor= {Exception.class})
	public Transaction insertTransaction(Transaction transaction)
	{
		String now = dateutil.getUTCNow();
		transaction.setRegistdate(now);
		if(transactionMapper.insertTransaction(transaction) > 0)
		{
			return transaction;
		}
		transaction.setIdx(-1);
		return transaction;
	}
	
	public ArrayList<Transaction> selectTransactionListNopage(Transaction transaction)
	{
		return transactionMapper.selectTransactionNopage(transaction);
	}
}

package io.mithrilcoin.api.biz.transaction.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.blockchain.Transaction;
import io.mithrilcoin.api.biz.transaction.mapper.TransactionMapper;

public class TransactionService {
	
	@Autowired
	private TransactionMapper transactionMapper;
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor= {Exception.class})
	public Transaction insertTransaction(Transaction transaction)
	{
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

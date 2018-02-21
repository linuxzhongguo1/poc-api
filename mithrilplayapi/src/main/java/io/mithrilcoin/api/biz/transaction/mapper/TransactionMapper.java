package io.mithrilcoin.api.biz.transaction.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import io.mithril.vo.blockchain.Transaction;

@Repository
public interface TransactionMapper {

	public int insertTransaction(Transaction transaction);
	
	public ArrayList<Transaction> selectTransactionNopage(Transaction transaction);
	
}

package io.mithrilcoin.api.biz.transaction.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.blockchain.Transaction;
import io.mithrilcoin.api.biz.transaction.service.TransactionService;

@RestController
@RequestMapping("/trans")
public class TransactionCotroller {

	@Autowired
	private TransactionService transactionService;
	
	@PostMapping("/insert/{accessPoint}/{idx}")
	public Transaction insertTransaction(@RequestBody Transaction transaction, @PathVariable String accessPoint, @PathVariable String idx)
	{
		return transactionService.insertTransaction(transaction);
	}
	
	@GetMapping("/select/listnopage/{accessPoint}/{idx}")
	public ArrayList<Transaction> selectListNopage(@RequestBody Transaction transaction)
	{
		return transactionService.selectTransactionListNopage(transaction);
	}
	
}

package io.mithrilcoin.api.biz.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mithrilcoin.api.biz.test.mapper.DatabaseTestMapper;

@Service
public class DatebaseTestService {

	@Autowired
	private DatabaseTestMapper dbTestMapper;
	
	public String getDbTime()
	{
		return dbTestMapper.getNow();
	}
	
}

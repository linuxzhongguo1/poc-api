package io.mithrilcoin.api.biz.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mithrilcoin.api.biz.test.mapper.DatabaseTestMapper;
import io.mithrilcoin.api.common.redis.RedisDataRepository;

@Service
public class DatebaseTestService {

	@Autowired
	private DatabaseTestMapper dbTestMapper;
	
	@Autowired
	private RedisDataRepository<String, String> redisRepo;
	
	@Cacheable("dbtime")
	public String getDbTime()
	{
		redisRepo.setData("dbTime",  dbTestMapper.getNow());
		return dbTestMapper.getNow();
	}
	
}

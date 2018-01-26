package io.mithrilcoin.api.biz.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.playdata.Rating;
import io.mithrilcoin.api.biz.test.mapper.DatabaseTestMapper;
import io.mithrilcoin.api.biz.test.service.DatebaseTestService;
import io.mithrilcoin.api.common.security.HashingUtil;

@RestController
@RequestMapping("/test")
public class DatebaseTestController {

	private static Logger logger = LoggerFactory.getLogger(DatebaseTestController.class);
	
	@Autowired
	private DatebaseTestService dbTestService;
	
	@Autowired
	private DatabaseTestMapper mapper;
	
	@Autowired
	private HashingUtil hashingUtil;
	
	@GetMapping("/select/dbtime")
	public String getDbTime()
	{
		logger.debug("####### select db time");
		String dbTime = dbTestService.getDbTime();
		
		logger.debug("####### selected db time");
		return dbTime;
	}
	
	@GetMapping("/select/hash/{normalString}")
	public String getHashedString(@PathVariable String normalString)
	{
		return hashingUtil.getHashedString(normalString);
	}
	
	@GetMapping("/select/rate")
	public Rating getRating()
	{
		Rating rate = mapper.selectRating();
		
		return rate;
	}
}

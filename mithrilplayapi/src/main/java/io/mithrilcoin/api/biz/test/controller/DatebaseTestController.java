package io.mithrilcoin.api.biz.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithrilcoin.api.biz.test.service.DatebaseTestService;

@RestController
@RequestMapping("/dbtest")
public class DatebaseTestController {

	private static Logger logger = LoggerFactory.getLogger(DatebaseTestController.class);
	
	@Autowired
	private DatebaseTestService dbTestService;
	
	@GetMapping("/select/dbtime")
	public String getDbTime()
	{
		logger.debug("####### select db time");
		String dbTime = dbTestService.getDbTime();
		
		logger.debug("####### selected db time");
		return dbTime;
	}
}

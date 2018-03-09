package io.mithrilcoin.api.biz.rate.service;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mithril.vo.rate.Rate;
import io.mithrilcoin.api.biz.rate.mapper.RateMapper;
import io.mithrilcoin.api.common.redis.RedisDataRepository;

@Service
public class RateInfoService {

	@Autowired
	private RedisDataRepository<String, Rate> redisDataRepository;

	@Autowired
	private RateMapper rateMapper;

	@PostConstruct
	public void rateInfoInitialize() {
		ArrayList<Rate> ratelist = rateMapper.selectRatelist();

		ratelist.stream().forEach(rate -> {
			redisDataRepository.setData("rate_" + rate.getIdx(), rate);
			redisDataRepository.setData("rate_" + rate.getName(), rate);
		});

		ratelist.stream().forEach(rate -> {
			if (rate.getTypecode().equals("D001002")) {
				if (rate.getUprateidx() > 0) {
					rate.setUprate(redisDataRepository.getData("rate_" + rate.getUprateidx()));
				}
				if (rate.getDownrateidx() > 0) {
					rate.setDownrate(redisDataRepository.getData("rate_" + rate.getDownrateidx()));
				}
				redisDataRepository.setData("rate_" + rate.getIdx(), rate);
				redisDataRepository.setData("rate_" + rate.getName(), rate);
			}
		});
	}

	public Rate getNextRateByName(String rateName) {
		Rate currentRate = redisDataRepository.getData("rate_" + rateName);
		return currentRate.getUprate();
	}

	public Rate getPreviousRateByName(String rateName) {
		Rate currentRate = redisDataRepository.getData("rate_" + rateName);
		return currentRate.getDownrate();
	}

	public Rate getNextRateByIdx(long idx) {
		Rate currentRate = redisDataRepository.getData("rate_" + idx);
		return currentRate.getUprate();
	}

	public Rate getPreviousRateByIdx(long idx) {
		Rate currentRate = redisDataRepository.getData("rate_" + idx);
		return currentRate.getDownrate();
	}

	public Rate getRate(long idx) {
		return redisDataRepository.getData("rate_" + idx);
	}

	public Rate getRate(String name) {
		return redisDataRepository.getData("rate_" + name);
	}

	@Cacheable("RateInfo")
	public ArrayList<Rate> getRateList() {
		return rateMapper.selectRatelist();
	}

}

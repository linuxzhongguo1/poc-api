package io.mithrilcoin.api.biz.rate.service;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mithril.vo.rate.Rate;
import io.mithril.vo.rate.Rateproperty;
import io.mithril.vo.rate.Ratevariable;
import io.mithrilcoin.api.biz.rate.mapper.RateMapper;
import io.mithrilcoin.api.common.redis.RedisDataRepository;

@Service
public class RateInfoService {

	@Autowired
	private RedisDataRepository<String, Rate> redisDataRepository;

	@Autowired
	private RedisDataRepository<String, Rateproperty> redisRatePropertyDataRepository;

	@Autowired
	private RedisDataRepository<String, ArrayList<Ratevariable>> redisRateVariableDataRepository;

	@Autowired
	private RateMapper rateMapper;

	private static final String PROPERTY_PREFIX = "rateproperty_";
	private static final String VARIABLE_PREFIX = "ratevariable_";

	@PostConstruct
	public void rateInfoInitialize() {
		ArrayList<Rate> ratelist = rateMapper.selectRatelist();
		rateInitialize(ratelist);
		ArrayList<Rateproperty> propertylist = rateMapper.selectRateproperty();
		ArrayList<Ratevariable> variablelist = rateMapper.selectRatevariable();
		rateVaribaleInitialize(propertylist, variablelist);
	}

	private void rateInitialize(ArrayList<Rate> ratelist) {
		ratelist.stream().forEach(rate -> {
			redisDataRepository.setData("rate_" + rate.getIdx(), rate);
			redisDataRepository.setData("rate_" + rate.getName(), rate);
		});

		ratelist.stream().forEach(rate -> {
			if (rate.getTypecode().equals("R001001")) {
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

	private void rateVaribaleInitialize(ArrayList<Rateproperty> propertylist, ArrayList<Ratevariable> variablelist) {

		propertylist.stream().forEach(rateproperty -> {
			String key = PROPERTY_PREFIX + rateproperty.getRatetype() + rateproperty.getName();
			redisRatePropertyDataRepository.setData(key, rateproperty);
		});

		variablelist.stream().forEach(ratevariable -> {
			String key = VARIABLE_PREFIX + ratevariable.getRatetype() + ratevariable.getCategory();
			if (redisRatePropertyDataRepository.hasContainKey(key)) {
				redisRateVariableDataRepository.getData(key).add(ratevariable);
			} else {
				ArrayList<Ratevariable> list = new ArrayList<>();
				list.add(ratevariable);
				redisRateVariableDataRepository.setData(key, list);
			}
		});
	}
	/**
	 *  null 이 반환될 수 있음.
	 * @param rateType R003001 - 장르 R003002 - 등급 R003003 - 추천
	 * @param name
	 * @return
	 */
	public Rateproperty getRatePropertiesByTypeAndName(String rateType, String name)
	{
		String key = PROPERTY_PREFIX + rateType + name;
		return redisRatePropertyDataRepository.getData(key);
	}
	/**
	 * null 이 반환될 수 있음.
	 * @param rateType R003001 - 장르 R003002 - 등급 R003003 - 추천
	 * @param categoryName DT, Login, APM, Install, GoogleLogin, Other
	 * @return Ratevariable list
	 */
	public ArrayList<Ratevariable> getRateVariablelist(String rateType, String categoryName)
	{
		String key = VARIABLE_PREFIX + rateType + categoryName;
		return redisRateVariableDataRepository.getData(key);
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

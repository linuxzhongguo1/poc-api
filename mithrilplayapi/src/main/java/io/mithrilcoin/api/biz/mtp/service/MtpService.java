package io.mithrilcoin.api.biz.mtp.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.mtp.MtpHistory;
import io.mithril.vo.mtp.MtpSource;
import io.mithril.vo.mtp.MtpTotal;
import io.mithrilcoin.api.biz.mtp.mapper.MtpMapper;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class MtpService {
	@Autowired
	private MtpMapper mtpMapper;
	
	@Autowired
	private DateUtil datetutil;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MtpHistory insertMtp(MtpHistory mtpHistory) {
		mtpMapper.insertMtp(mtpHistory);
		return mtpHistory;
	}

	public ArrayList<MtpSource> selectMtpSource(MtpSource source) {
		
		return mtpMapper.selectMtpSource(source);
	}
	
	/**
	 * 가입 보상 지급 
	 * @param member_idx
	 */
	@CacheEvict(value="MTPCache", key="#member_idx")
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void insertInviteReward(long member_idx)
	{
		//가입 보상 100포인트 지급 
		MtpSource source = new MtpSource();
		// 가입 보상 기준 
		source.setTypecode("T003002");
		ArrayList<MtpSource> sourceList = mtpMapper.selectMtpSource(source);
		
		source = sourceList.get(0);
		
		
		MtpHistory mtphistory = new MtpHistory();
		mtphistory.setMember_idx(member_idx);
		mtphistory.setAmount(source.getAmount());
		mtphistory.setMtpsource_idx(source.getIdx());
		mtphistory.setTypecode("T002001");
		mtphistory.setRegistdate(datetutil.getUTCNow());
		mtpMapper.insertMtp(mtphistory);
	}
	/**
	 * 등급 업 보상 지급 
	 * @param member_idx
	 */
	@CacheEvict(value="MTPCache", key="#member_idx")
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void insertUpgradeReward(long member_idx, long amount)
	{
		//가입 보상 100포인트 지급 
		MtpSource source = new MtpSource();
		// 가입 보상 기준 
		source.setTypecode("T003004");
		ArrayList<MtpSource> sourceList = mtpMapper.selectMtpSource(source);
		
		source = sourceList.get(0);
		
		
		MtpHistory mtphistory = new MtpHistory();
		mtphistory.setMember_idx(member_idx);
		mtphistory.setAmount(amount);
		mtphistory.setMtpsource_idx(source.getIdx());
		mtphistory.setTypecode("T002001");
		mtphistory.setRegistdate(datetutil.getUTCNow());
		mtpMapper.insertMtp(mtphistory);
	}
	
	
	/***
	 * 게임 데이터 보상 지급 
	 * @param member_idx
	 */
	@CacheEvict(value="MTPCache", key="#member_idx")
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MtpHistory insertDataReward(long member_idx, long playdata_idx)
	{
		//가입 보상 100포인트 지급 
		MtpSource source = new MtpSource();
		// 가입 보상 기준 
		source.setTypecode("T003003");
		ArrayList<MtpSource> sourceList = mtpMapper.selectMtpSource(source);
		
		source = sourceList.get(0);
		
		MtpHistory mtphistory = new MtpHistory();
		mtphistory.setMember_idx(member_idx);
		mtphistory.setAmount(source.getAmount());
		mtphistory.setMtpsource_idx(source.getIdx());
		mtphistory.setTypecode("T002001");
		mtphistory.setPlaydata_idx(playdata_idx);
		mtphistory.setRegistdate(datetutil.getUTCNow());
		mtpMapper.insertMtp(mtphistory);
		
		return mtphistory;
	}
	
	@Cacheable(value="MTPCache",key="#member_idx")
	public MtpTotal selectMtpTotal(long member_idx) {
		MtpTotal mtpTotal = mtpMapper.selectMtpTotalByMember(member_idx);
		mtpTotal.setUsableamount(mtpTotal.getIncomeamount() - mtpTotal.getSpentamount() - mtpTotal.getExpireamount());
		return mtpTotal;
	}
	
	
}

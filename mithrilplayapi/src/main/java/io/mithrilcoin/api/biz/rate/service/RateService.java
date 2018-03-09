package io.mithrilcoin.api.biz.rate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.member.Member;
import io.mithril.vo.rate.MemberRating;
import io.mithril.vo.rate.Memberrate;
import io.mithril.vo.rate.Rate;
import io.mithril.vo.rate.Ratehistory;
import io.mithrilcoin.api.biz.mtp.mapper.MtpMapper;
import io.mithrilcoin.api.biz.rate.mapper.RateMapper;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class RateService {

	@Autowired
	private RateInfoService rateInfoService;

	@Autowired
	private RateMapper rateMapper;
	
	@Autowired
	private MtpMapper mtpMapper;

	@Autowired
	private DateUtil dateutil;

	private static final String FIRST_RATE = "Stone";

	// 1. 등급 조정 함수
	// 2. 레이팅 부여 함수
	// 3. 레이팅 차감 함수
	// 4. 등급 조건 검사.
	// 5. 레이팅 통계 관련 함수.
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MemberRating rateTune(Member member) {

		Ratehistory currentRateHistory = rateMapper.selectLastRatehistoryByMemberIdx(member.getIdx());
		Memberrate currentMemberRate = rateMapper.selectLastMemberrateByMemberIdx(member.getIdx());

		MemberRating mrate = new MemberRating();
		
		Rate assignRate = null;
		// 최초 등급 할당 여부 확인
		if (currentMemberRate == null) {
			assignRate = rateInfoService.getRate(FIRST_RATE);
			currentMemberRate = insertMemberrate(assignRate, member.getIdx());
			//int rpPoint, String statecode, int oldAmount, long member_idx
			currentRateHistory = insertRateHistory(assignRate.getRpreward(), "R002001", 0, member.getIdx());
			mrate.setName(assignRate.getName());
			mrate.setRatepoint(currentRateHistory.getCurrentamount());
		}
		
		
		Rate currentRate = rateInfoService.getRate(currentMemberRate.getRate_idx());
		// validation 
		

		return null;
	}

	private Memberrate insertMemberrate(Rate rate, long member_idx) {
		Memberrate memberRate = new Memberrate();
		memberRate.setMember_idx(member_idx);
		memberRate.setRate_idx(rate.getIdx());
		memberRate.setRegistedate(dateutil.getUTCNow());
		rateMapper.insertMemberrate(memberRate);

		return memberRate;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Ratehistory insertRateHistory(int rpPoint, String statecode, int oldAmount, long member_idx) {
		Ratehistory rh = new Ratehistory();
		rh.setAmount(rpPoint);
		rh.setMember_idx(member_idx);
		if (statecode.equals("R002001")) {
			rh.setCurrentamount(oldAmount + rpPoint);
		} else {
			rh.setCurrentamount(oldAmount - rpPoint);
		}
		rh.setRegistdate(dateutil.getUTCNow());
		rh.setStatecode(statecode);

		rateMapper.insertRatehistory(rh);
		return rh;
	}
	/**
	 * 
	 * @return
	 */
	private int checkUpDownRate(Rate currentRate, long member_idx )
	{
		return 0;
	}

}

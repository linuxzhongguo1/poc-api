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
import io.mithrilcoin.api.biz.gamedata.service.GamedataService;
import io.mithrilcoin.api.biz.member.service.MemberService;
import io.mithrilcoin.api.biz.mtp.service.MtpService;
import io.mithrilcoin.api.biz.rate.mapper.RateMapper;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class RateService {

	@Autowired
	private RateInfoService rateInfoService;

	@Autowired
	private RateMapper rateMapper;

	@Autowired
	private MtpService mtpservice;

	@Autowired
	private MemberService memberService;

	@Autowired
	private GamedataService gamedataService;

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
			currentRateHistory = insertRateHistory(assignRate.getRpreward(), "R002001", 0, member.getIdx());
		}
		Rate currentRate = rateInfoService.getRate(currentMemberRate.getRate_idx());
		// 전체에서 내 등위
		long grade = rateMapper.selectRankgrade(currentRateHistory.getCurrentamount());
		long totalMember = memberService.selectTotalMemberCount();
		// 전체에서 내 백분율
		long percent = getPercent(grade, totalMember);
		if (assignRate == null) {
			assignRate = checkUpDownRate(currentRate, member, percent);
			// 승급이라면
			if (assignRate.getIdx() == currentRate.getUprateidx() && currentRate.getUprateidx() > 0) {
				// 한번도 승급된적 없는 등급이고 해당 승급 보상이 있는 경우.
				if (rateMapper.selectMemberrate(member.getIdx(), currentRate.getUprateidx()).size() == 0
						&& assignRate.getReward() > 0) {
					// 보너스 MTP포인트 지급
					mtpservice.insertUpgradeReward(member.getIdx(), assignRate.getReward());
				}
				currentMemberRate = insertMemberrate(assignRate, member.getIdx());
				// rating 포인트 부여
				currentRateHistory = insertRateHistory(assignRate.getRpreward(), "R002001", 0, member.getIdx());
			}
		}
		mrate.setRank(grade);
		mrate.setName(assignRate.getName());
		mrate.setRatepoint(currentRateHistory.getCurrentamount());
		mrate.setGraderank(
				rateMapper.selectRankgradeByGroup(assignRate.getIdx(), currentRateHistory.getCurrentamount()) + 1);
		return mrate;
	}

	private Memberrate insertMemberrate(Rate rate, long member_idx) {
		Memberrate memberRate = new Memberrate();
		memberRate.setMember_idx(member_idx);
		memberRate.setRate_idx(rate.getIdx());
		memberRate.setRegistdate(dateutil.getUTCNow());
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
	private Rate checkUpDownRate(Rate currentRate, Member member, long rate) {

		// 등급 조정 대상 인지 확인
		if (!currentRate.getTypecode().equals("R001001")) {
			return currentRate;
		}
		// 1) 레이팅 퍼센트 2) MTP + During Time + Install game 수
		// 현재 상태 확인
		if (currentRate.getRating() > 0) // 필요 레이팅 유지 RP 조건이 있는가.
		{
			// 현재 레이트 확인
			// 레이팅 유지 조건이 우선 순위 1
			if (currentRate.getRating() > rate) // 레이팅이 유지 가능한 상태.
			{
				if (currentRate.getUprateidx() > 0) // 다음 승급 등급이 있는 지
				{
					Rate uprate = currentRate.getUprate();
					if (uprate.getRating() > 0 && uprate.getRating() > rate) // 승급 범위 안에 들어 왓는지 확인.
					{
						if (rateRequirementCheck(uprate, member)) // 디테일한 기본 조건을 확인
						{
							return uprate;
						}
					}
				}
				// 별거 없으면 그냥 현재 레이트 반환
				return currentRate;
			} else // 레이트 벗어남 하락 객잔.
			{
				if (currentRate.getDownrateidx() > 0) // 하락할 레이트 가 있으면
				{
					return currentRate.getDownrate();
				}
				return currentRate;
			}
		}

		// 이 후 처리되는 레이팅은 승급만 존재
		if (currentRate.getUprateidx() > 0) // 레이팅과 무관하게 다음 승급으로 가는 승급 조건이 있는지 확인.
		{
			Rate uprate = currentRate.getUprate();
			rateRequirementCheck(uprate, member);
		}

		return currentRate;
	}

	/**
	 * 레이팅 조건 외에 레이트를 획득하기 위한 기본 조건을 체크
	 * 
	 * @param uprate
	 * @param member_idx
	 * @return
	 */
	private boolean rateRequirementCheck(Rate uprate, Member member) {

		long member_idx = member.getIdx();
		// MTP 보유량 체크
		if (uprate.getMinmtp() > 0) {
			if (uprate.getMinmtp() > mtpservice.selectMtpTotal(member_idx).getUsableamount()) {
				return false;
			}
		}
		// 게임 인스톨 갯수 체크
		if (uprate.getMingame() > 0) {
			if (uprate.getMingame() > gamedataService.selectInstalledGame(member_idx)) {
				return false;
			}
		}
		// 전체 게임 플레이 시간 체크
		if (uprate.getMinplaytime() > 0) {
			if (uprate.getMinplaytime() > gamedataService.selectTotalPlaytime(member_idx)) {
				return false;
			}
		}
		// 최근 30일 게임 평균 플레이 시간 체크
		if (uprate.getMinavgplaytime() > 0) {
			if (uprate.getMinavgplaytime() > gamedataService.selectAvgPlaytime(member_idx)) {
				return false;
			}
		}
		// 위잿 체크 ???
		// SNS 연동체크
		// 추가 회원 인증 여부 체크
		if (uprate.getAuthyn().equals("Y")) {
			// 이메일 인증 + 소셜 아이디 (google or else)가 있는 경우
			if (!member.getState().equals("M001002") || memberService.selectMemberSocial(member_idx) == null) {
				return false;
			}
		}
		return true;
	}

	private long getPercent(long value, long total) {

		double rate = (double) ((double) value / (double) total) * 100;
		long longper = (long) rate;

		return longper;
	}

}

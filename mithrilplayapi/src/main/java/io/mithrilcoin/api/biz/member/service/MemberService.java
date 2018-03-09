package io.mithrilcoin.api.biz.member.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.exception.MithrilPlayExceptionCode;
import io.mithril.vo.member.Device;
import io.mithril.vo.member.Member;
import io.mithril.vo.member.MemberDetail;
import io.mithril.vo.member.MemberInfo;
import io.mithril.vo.member.MemberListVo;
import io.mithril.vo.member.MemberSocial;
import io.mithril.vo.member.UserInfo;
import io.mithrilcoin.api.biz.gamedata.service.GamedataService;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.mtp.service.MtpService;
import io.mithrilcoin.api.common.security.HashingUtil;
import io.mithrilcoin.api.exception.MithrilPlayException;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class MemberService {

	private static Logger logger = LoggerFactory.getLogger(MemberService.class);

	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private MtpService mtpService;

	@Autowired
	private HashingUtil hashUtil;

	@Autowired
	private DateUtil dateutil;

	// 회원 가입
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@CacheEvict("TotalMember")
	public MemberInfo insertMember(MemberInfo signUpMember) throws MithrilPlayException {
		logger.info("io.mithrilcoin.api.biz.member.service insertMember ");
	
		//SNS 회원가입이여도 패스워드는 무조건 존재해야 함. 
		if( signUpMember.getPassword() == null || signUpMember.getPassword().equals(""))
		{
			logger.error("insertMember : " + MithrilPlayExceptionCode.INVALID_PARAMETER.getMessage());
			signUpMember.setIdx(0);
			return signUpMember;
		}
		signUpMember.setPassword(hashUtil.getHashedString(signUpMember.getPassword()));
		ArrayList<Member> memberlist = memberMapper.selectMember(signUpMember);
		// 이미 회원가입된 사용자
		if (memberlist.size() > 0) {
			// SNS 정보 있음
			if(signUpMember.getMembersocial() != null)
			{
				MemberSocial sns = signUpMember.getMembersocial();
				Member findMember = memberlist.get(0);
				sns.setMember_idx(findMember.getIdx());
				MemberSocial result = memberMapper.selectMembersocial(sns);
				//SNS 가입 이력이 있는 경우
				if( result != null)
				{
					logger.error("insertMember : " + MithrilPlayExceptionCode.RESULT_ALREADY_MEMBER.getMessage());
					signUpMember.setIdx(-1);
					return signUpMember;
				}
				else
				{
					//SNS 가입 정보만 처리.
					memberMapper.insertMemberSocial(sns);
					signUpMember.setState(findMember.getState());
					signUpMember.setRatio(findMember.getRatio());
				
					String now = dateutil.getUTCNow();
					signUpMember.setRegistdate(findMember.getRegistdate());
					signUpMember.setModifydate(findMember.getModifydate());
					signUpMember.setRecentlogindate(now);
					signUpMember.setMembersocial(sns);
					return signUpMember;
				}
			}
			else
			{
				logger.error("insertMember : " + MithrilPlayExceptionCode.RESULT_ALREADY_MEMBER.getMessage());
				signUpMember.setIdx(-1);
				return signUpMember;
			}
		}
		else
		{
			// 미인증 회원으로 최초 등록
			signUpMember.setState("M001001");
			signUpMember.setRatio(1.0f);
			logger.info("insertMember : memberMapper.insertMember");
			String now = dateutil.getUTCNow();
			signUpMember.setRegistdate(now);
			signUpMember.setModifydate(now);
			signUpMember.setRecentlogindate(now);
			
			memberMapper.insertMember(signUpMember);
			if (signUpMember.getIdx() > 0) {
				logger.info("insertMember : memberMapper.insertMember end");
				Device device = new Device();
				device.setBrand(signUpMember.getBrand());
				device.setDeviceid(signUpMember.getDeviceid());
				device.setMember_idx(signUpMember.getIdx());
				device.setModel(signUpMember.getModel());
				device.setRegistdate(now);
				device.setFcmid(signUpMember.getFcmid());
				device.setOsversion(signUpMember.getOsversion());
				device.setUseyn("Y");
				memberMapper.insertDevice(device);
				memberMapper.updateNewDevice(device);
				// SNS 정보로 회원가입이라면 해당 정보 같이 입력 
				if(signUpMember.getMembersocial() != null)
				{
					MemberSocial sns =  signUpMember.getMembersocial();
					sns.setMember_idx(signUpMember.getIdx());
					memberMapper.insertMemberSocial(sns);
					signUpMember.setMembersocial(sns);
				}
				
				logger.info("insertMember : " + MithrilPlayExceptionCode.SUCCESS.getMessage());
				return signUpMember;
			}
		}
		logger.error("insertMember : " + MithrilPlayExceptionCode.FAILED_TO_INSERT.getMessage());

		throw new MithrilPlayException(MithrilPlayExceptionCode.FAILED_TO_INSERT);
	}

	public Member signIn(MemberInfo member) {
		logger.info("io.mithrilcoin.api.biz.member.service signIn ");
		ArrayList<Member> memberlist = memberMapper.selectMember(member);
		if (memberlist.size() > 0) {
			Member findMember = memberlist.get(0);
			logger.info("io.mithrilcoin.api.biz.member.service signIn find member : " + findMember.getEmail());
			
			//SNS로그인 요청이면 
			if(member.getMembersocial() != null)
			{
				MemberSocial sns = member.getMembersocial();
				sns.setMember_idx(findMember.getIdx());
				MemberSocial result = memberMapper.selectMembersocial(sns);
				if( result != null && result.getTokenid().equals(sns.getTokenid()))
				{
					return findMember;
					
				}else {
					// SNS 아이디 틀림
					member.setIdx(-1);
					return member;
				}
			}
			else
			{
				String sourcePass = hashUtil.getHashedString(member.getPassword());
				String targetPass = findMember.getPassword();
				if (sourcePass.equals(targetPass)) {
					return findMember;
				} else {
					// 비번 틀림
					member.setIdx(-1);
					return member;
				}
			}

		}
		return null;
	}

	public Member selectMember(MemberInfo signUpMember) {
		logger.info("io.mithrilcoin.api.biz.member.service selectMember ");
		ArrayList<Member> memberlist = memberMapper.selectMember(signUpMember);
		if (memberlist.size() > 0) {
			Member findMember = memberlist.get(0);
			logger.info("io.mithrilcoin.api.biz.member.service selectMember find member : " + findMember.getEmail());
			return findMember;
		}
		return null;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Member authorizeMember(Member member) {

		ArrayList<Member> findMembers = memberMapper.selectMember(member);
		if (findMembers.size() > 0) {
			Member findMember = findMembers.get(0);

			if ("M001001".equals(findMember.getState())) {
				// 정상 인증회원으로 처리
				findMember.setState("M001002");
				String now = dateutil.getUTCNow();
				findMember.setAuthdate(dateutil.getUTCNow());
				findMember.setModifydate(now);
				memberMapper.updateMember(findMember);
				findMember = memberMapper.selectMember(member).get(0);
				
				return findMember;
			}
			member.setState("");
		}
		return member;
	}

	public UserInfo selectUserInfo(String idx) {
		long idxlong = Long.parseLong(idx);

		UserInfo info = new UserInfo();
		Device device = new Device();
		device.setMember_idx(idxlong);
		device.setUseyn("Y");
		ArrayList<Device> deviceList = memberMapper.selectDevice(device);
		if (deviceList.size() > 0) {
			info.setDeviceid(deviceList.get(deviceList.size() - 1).getDeviceid());
			info.setFcmid(deviceList.get(deviceList.size() - 1).getFcmid());
		}
		MemberDetail detail = new MemberDetail();
		detail.setMember_idx(idxlong);
		detail = memberMapper.selectMemberDetail(detail);
		info.setMemberDetail(detail);
		info.setValidtime(GamedataService.VALID_PLAY_TIME);
		
		Member member = new Member();
		member.setIdx(idxlong);
		ArrayList<Member> memlist = memberMapper.selectMember(member);
		if(memlist.size() > 0 )
		{
			info.setRegistdate(memlist.get(0).getRegistdate());
		}
		return info;

	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MemberDetail updateMemberDetail(MemberDetail memberdetail) {

		MemberDetail findMemberdetail = memberMapper.selectMemberDetail(memberdetail);
		String now = dateutil.getUTCNow();
		memberdetail.setModifydate(now);
	
		if( findMemberdetail  != null)
		{
			memberMapper.updateMemberdetail(memberdetail);
		}
		else
		{
			memberdetail.setRegistdate(now);
			memberMapper.insertMemberDetail(memberdetail);
			Member member = new Member();
			member.setIdx(memberdetail.getMember_idx());
			member.setState("M001003");
			member.setModifydate(now);
			memberMapper.updateMember(member);
			mtpService.insertInviteReward(memberdetail.getMember_idx());
			
//			//가입 보상 100포인트 지급 
//			MtpSource source = new MtpSource();
//			// 가입 보상 기준 
//			source.setTypecode("T003002");
//			ArrayList<MtpSource> sourceList = mtpService.selectMtpSource(source);
//			
//			source = sourceList.get(0);
//			
//			MtpHistory mtphistory = new MtpHistory();
//			mtphistory.setMember_idx(member.getIdx());
//			mtphistory.setAmount(source.getAmount());
//			mtphistory.setMtpsource_idx(source.getIdx());
//			mtphistory.setTypecode("T002001");
//			mtpService.insertMtp(mtphistory);
			
		}
		findMemberdetail = memberMapper.selectMemberDetail(memberdetail);
		return findMemberdetail;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Device updateDevice(Device device) {
		
		ArrayList<Device> findDevices = memberMapper.selectDevice(device);
		String now = dateutil.getUTCNow();
		String fcmid = device.getFcmid();
		String osversion = device.getOsversion();
		if( findDevices.size() > 0)
		{
		
			device = findDevices.get(0);
			device.setFcmid(fcmid);
			device.setOsversion(osversion);
			device.setUseyn("Y");
		}
		else
		{
			device.setUseyn("Y");
			device.setRegistdate(now);
			device.setFcmid(fcmid);
			device.setOsversion(osversion);
			memberMapper.insertDevice(device);
		}
		
		memberMapper.updateNewDevice(device);
		memberMapper.updateActiveDevice(device);
		
		return device;
	}
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Member updateMemberLogInTime(Member member) {
		
		memberMapper.updateMember(member);
		
		return member;
	}
	
	public ArrayList<MemberListVo> selectMemberList(MemberListVo vo)
	{
		int totalcount = memberMapper.selectMemberTotalCount();
		int totalsubcount = memberMapper.selectMemberSubCount(vo);
		ArrayList<MemberListVo> list = memberMapper.selectMemberList(vo);
		
		if(list.size() > 0)
		{
			list.get(0).setTotalcount(String.valueOf(totalcount));
			list.get(0).setSearchcount(String.valueOf(totalsubcount));
		}
		
		return list;
	}
	@Cacheable("TotalMember")
	public long selectTotalMemberCount()
	{
		return memberMapper.selectTotalMemberCount();
	}
	

}

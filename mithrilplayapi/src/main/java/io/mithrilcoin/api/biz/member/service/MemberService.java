package io.mithrilcoin.api.biz.member.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.exception.MithrilPlayExceptionCode;

import io.mithril.vo.member.Device;
import io.mithril.vo.member.Member;
import io.mithril.vo.member.MemberDetail;
import io.mithril.vo.member.MemberInfo;
import io.mithril.vo.member.UserInfo;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.common.security.HashingUtil;
import io.mithrilcoin.api.exception.MithrilPlayException;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class MemberService {

	private static Logger logger = LoggerFactory.getLogger(MemberService.class);

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private HashingUtil hashUtil;

	@Autowired
	private DateUtil dateutil;

	// 회원 가입
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public MemberInfo insertMember(MemberInfo signUpMember) throws MithrilPlayException {
		logger.info("io.mithrilcoin.api.biz.member.service insertMember ");
		signUpMember.setPassword(hashUtil.getHashedString(signUpMember.getPassword()));
		ArrayList<Member> memberlist = memberMapper.selectMember(signUpMember);
		// 이미 회원가입된 사용자
		if (memberlist.size() > 0) {
			logger.error("insertMember : " + MithrilPlayExceptionCode.RESULT_ALREADY_MEMBER.getMessage());
			// throw new
			// MithrilPlayException(MithrilPlayExceptionCode.RESULT_ALREADY_MEMBER);
			signUpMember.setIdx(-1);
			return signUpMember;

		}
		// 미인증 회원으로 최초 등록
		signUpMember.setState("M001001");
		signUpMember.setRatio(1.0f);
		logger.info("insertMember : memberMapper.insertMember");
		memberMapper.insertMember(signUpMember);
		if (signUpMember.getIdx() > 0) {
			logger.info("insertMember : memberMapper.insertMember end");
			Device device = new Device();
			device.setBrand(signUpMember.getBrand());
			device.setDeviceid(signUpMember.getDeviceid());
			device.setMember_idx(signUpMember.getIdx());
			device.setModel(signUpMember.getModel());
			device.setUseyn("Y");
			memberMapper.insertDevice(device);
			memberMapper.updateNewDevice(device);

			logger.info("insertMember : " + MithrilPlayExceptionCode.SUCCESS.getMessage());
			return signUpMember;
		}
		logger.error("insertMember : " + MithrilPlayExceptionCode.FAILED_TO_INSERT.getMessage());

		throw new MithrilPlayException(MithrilPlayExceptionCode.FAILED_TO_INSERT);
	}

	public Member signIn(Member member) {
		logger.info("io.mithrilcoin.api.biz.member.service signIn ");
		ArrayList<Member> memberlist = memberMapper.selectMember(member);
		if (memberlist.size() > 0) {
			Member findMember = memberlist.get(0);
			logger.info("io.mithrilcoin.api.biz.member.service signIn find member : " + findMember.getEmail());

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
				memberMapper.updateMember(findMember);
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
		}
		MemberDetail detail = new MemberDetail();
		detail.setMember_idx(idxlong);
		detail = memberMapper.selectMemberDetail(detail);
		info.setMemberDetail(detail);

		return info;

	}

}

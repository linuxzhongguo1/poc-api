package io.mithrilcoin.api.biz.member.service;

import java.util.ArrayList;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.exception.MithrilPlayExceptionCode;
import io.mithril.vo.member.Account;
import io.mithril.vo.member.Device;
import io.mithril.vo.member.Member;
import io.mithril.vo.member.SignupMember;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.common.security.HashingUtil;
import io.mithrilcoin.api.exception.MithrilPlayException;

@Service
public class MemberService {
	
	private static Logger logger = LoggerFactory.getLogger(MemberService.class);
	
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private HashingUtil hashUtil;
	
	//회원 가입 
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor= {Exception.class})
	public SignupMember insertMember(SignupMember signUpMember) throws MithrilPlayException
	{
		signUpMember.setPassword(hashUtil.getHashedString(signUpMember.getPassword()));
		ArrayList<Member> memberlist = memberMapper.selectMember(signUpMember);
		// 이미 회원가입된 사용자
		if( memberlist.size() > 0)
		{
			throw new MithrilPlayException(MithrilPlayExceptionCode.RESULT_ALREADY_MEMBER);
		}
		// 미인증 회원으로 최초 등록
		signUpMember.setState("M001001");
		signUpMember.setRatio(1.0f);
		int memberIdx = memberMapper.insertMember(signUpMember);
		if( memberIdx > 0)
		{
			Device device = new Device();
			device.setBrand(signUpMember.getBrand());
			device.setDeviceid(signUpMember.getDeviceid());
			device.setMember_idx(memberIdx);
			device.setModel(signUpMember.getModel());
			memberMapper.insertDevice(device);
			signUpMember.setIdx(memberIdx);
			
			return signUpMember;
		}
		throw new MithrilPlayException(MithrilPlayExceptionCode.FAILED_TO_INSERT);
	}
}

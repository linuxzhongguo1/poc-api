package io.mithrilcoin.api.biz.member.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.exception.MithrilPlayExceptionCode;
import io.mithril.vo.member.Device;
import io.mithril.vo.member.Member;
import io.mithril.vo.member.MemberDetail;
import io.mithril.vo.member.MemberInfo;
import io.mithril.vo.member.UserInfo;
import io.mithrilcoin.api.biz.member.service.MemberService;
import io.mithrilcoin.api.exception.MithrilPlayException;

@RestController
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@PostMapping("/signup/{accessPoint}/{idx}")
	public MemberInfo memberSignUp(@RequestBody @Valid MemberInfo member, @PathVariable String accessPoint,
			@PathVariable String idx, BindingResult result) throws MithrilPlayException {

		// 필수값이 누락일 경우 예외처리
		if (result.hasErrors()) {
			throw new MithrilPlayException(MithrilPlayExceptionCode.INVALID_PARAMETER);
		}

		MemberInfo validMember = memberService.insertMember(member);

		return validMember;
	}

	@PostMapping("/signin/{accessPoint}/{idx}")
	public Member signin(@RequestBody @Valid Member member, @PathVariable String accessPoint, @PathVariable String idx,
			BindingResult result) {
		return memberService.signIn(member);
	}

	@PostMapping("/authorize/{accessPoint}/{idx}")
	public Member authrizeMail(@RequestBody Member member, @PathVariable String accessPoint,
			@PathVariable String idx) {
		return memberService.authorizeMember(member);
	}

	@GetMapping("/select/userInfo/{mberIdx}/{accessPoint}/{idx}")
	public UserInfo selectUserinfo(@PathVariable String mberIdx, @PathVariable String accessPoint,
			@PathVariable String idx) {
		return memberService.selectUserInfo(mberIdx);
	}

	@GetMapping("/select/{accessPoint}/{idx}")
	public Member selectMember(MemberInfo member, @PathVariable String accessPoint, @PathVariable String idx,
			BindingResult result) {
		try {
			String email = URLDecoder.decode( member.getEmail(), "UTF-8");
			member.setEmail(email);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Member findmember = memberService.selectMember(member);
		if (findmember != null) {
			return findmember;
		}
		return null;

	}
	
	@PostMapping("/update/memberdetail/{accessPoint}/{idx}")
	public MemberDetail updateMemberDetail(@RequestBody MemberDetail memberdetail, @PathVariable String accessPoint, @PathVariable String idx)
	{
		//memberService.updateMemberDetail(memberdetail);
		return memberService.updateMemberDetail(memberdetail);
	}
	@PostMapping("/update/device/{accessPoint}/{idx}")
	public Device updateDevice(@RequestBody Device device, @PathVariable String accessPoint, @PathVariable String idx)
	{
		return memberService.updateDevice(device);
	}
	@PostMapping("/update/loginTime/{accessPoint}/{idx}")
	public Member updateDevice(@RequestBody Member member, @PathVariable String accessPoint, @PathVariable String idx)
	{
		return memberService.updateMemberLogInTime(member);
	}
}

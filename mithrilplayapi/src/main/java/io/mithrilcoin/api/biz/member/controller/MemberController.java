package io.mithrilcoin.api.biz.member.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.exception.MithrilPlayExceptionCode;
import io.mithril.vo.member.MemberInfo;
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
}

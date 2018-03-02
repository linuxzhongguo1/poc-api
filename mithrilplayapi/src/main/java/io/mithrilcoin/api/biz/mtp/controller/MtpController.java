package io.mithrilcoin.api.biz.mtp.controller;

import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.member.Member;
import io.mithril.vo.mtp.MtpTotal;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.mtp.service.MtpService;

@RestController
@RequestMapping("/mtp")
public class MtpController {

	@Autowired
	private MtpService mtpService;

	@Autowired
	private MemberMapper memberMapper;

	@RequestMapping("/select/{email}/{accessPoint}/{idx}")
	public MtpTotal selectMtpTotal(@PathVariable String email, @PathVariable String accessPoint,
			@PathVariable String idx) throws Exception {

		String decodeMail = URLDecoder.decode(email, "UTF-8");
		Member member = new Member();
		member.setEmail(decodeMail);

		Member findMember = memberMapper.selectMember(member).get(0);

		return mtpService.selectMtpTotal(findMember.getIdx());
	}
}

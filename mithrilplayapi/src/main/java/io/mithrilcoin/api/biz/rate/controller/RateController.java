package io.mithrilcoin.api.biz.rate.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.member.Member;
import io.mithril.vo.rate.MemberRating;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.member.service.MemberService;
import io.mithrilcoin.api.biz.rate.service.RateService;

@RestController
@RequestMapping("/rate")
public class RateController {

	@Autowired
	private RateService rateService;

	@Autowired
	private MemberMapper memberMapper;

	@GetMapping("/select/{email}/{accessPoint}/{idx}")
	public MemberRating selectCurrentMemberRating(@PathVariable String email, @PathVariable String accessPoint,
			@PathVariable String idx) throws UnsupportedEncodingException {

		Member member = new Member();
		String decodeEmail = URLDecoder.decode(email, "UTF-8");
		
		member.setEmail(decodeEmail);
		ArrayList<Member> memberlist = memberMapper.selectMember(member);
		if (memberlist.size() > 0) {
			return rateService.rateTune(memberlist.get(0));
		}
		return null;
	}
}

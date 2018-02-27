package io.mithrilcoin.api.biz.member.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import io.mithril.vo.member.Account;
import io.mithril.vo.member.Device;
import io.mithril.vo.member.Member;
import io.mithril.vo.member.MemberDetail;
import io.mithril.vo.member.MemberListVo;
import io.mithril.vo.member.MemberSocial;

/**
 * 회원 가입, 변경, 기타 정보 처리 DAO 클래스 
 * @author Kei
 *
 */
@Repository
public interface MemberMapper {
	
	public int insertMember(Member member);
	
	public int updateMember(Member member);
	
	public ArrayList<Member> selectMember(Member member);
	
	public int insertMemberDetail(MemberDetail memberDetail);
	
	public int insertDevice(Device device);
	
	public int insertAccount(Account account);
	
	public int updateAccount(Account account);
	
	public int updateMemberdetail(MemberDetail memberdetail);

	public int updateNewDevice(Device device);
	
	public int updateActiveDevice(Device device);
	
	public ArrayList<Device> selectDevice(Device device);
	
	public MemberDetail selectMemberDetail(MemberDetail detail);
	
	public ArrayList<MemberListVo> selectMemberList(MemberListVo vo);
	
	public int selectMemberSubCount(MemberListVo vo);
	
	public int selectMemberTotalCount();
	
	public MemberSocial selectMembersocial(MemberSocial social);
	
	public int insertMemberSocial(MemberSocial social);
	
}

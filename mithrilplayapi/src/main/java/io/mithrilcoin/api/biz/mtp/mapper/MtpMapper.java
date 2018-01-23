package io.mithrilcoin.api.biz.mtp.mapper;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import io.mithril.vo.mtp.MtpHistory;
import io.mithril.vo.mtp.MtpSource;
import io.mithril.vo.mtp.MtpTotal;

@Repository
public interface MtpMapper {

	public int insertMtp(MtpHistory mtphistory);

	public int insertMtpSource(MtpSource mtpSource);

	public int updateMtpSource(MtpSource mtpSource);

	public ArrayList<MtpHistory> selectMtpHistoryByMember(long memberIdx);

	public long selectMtpTotalByMember(@Param("memberidx") long memberIdx, @Param("typecode") String typecode);
	
	public ArrayList<MtpSource> selectMtpSource(MtpSource source);
}

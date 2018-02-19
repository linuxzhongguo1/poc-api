package io.mithrilcoin.api.biz.gamedata.mapper;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import io.mithril.vo.playdata.PlayData;
import io.mithril.vo.playdata.Playhistory;
import io.mithril.vo.playdata.Playstoreappinfo;
import io.mithril.vo.playdata.TemporalPlayData;

@Repository
public interface GamedataMapper {

	public ArrayList<Playstoreappinfo> selectPlaystoreappinfo(Playstoreappinfo storeInfo);

	public ArrayList<Playstoreappinfo> selectMassPlaystoreappinfo(@Param("pagecount") int pagecount,
			@Param("size") int size, @Param("startidx") int idx);
	
	public ArrayList<TemporalPlayData> selectTodayPlayData(@Param("member_idx") long mber_idx);
	
	public int insertPlayData(PlayData playdata);

	public int updatePlaydata(PlayData playdata);

	public ArrayList<PlayData> selectTotalPlaydataNopage(@Param("member_idx") long member_idx);

	public ArrayList<TemporalPlayData> selectTodayPlayDataHistory(@Param("member_idx") long member_idx);
	
	public int insertPlayhistory(Playhistory playhistory);
}

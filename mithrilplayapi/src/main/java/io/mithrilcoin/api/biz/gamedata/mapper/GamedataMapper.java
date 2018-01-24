package io.mithrilcoin.api.biz.gamedata.mapper;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import io.mithril.vo.playdata.Playstoreappinfo;

@Repository
public interface GamedataMapper {

	public ArrayList<Playstoreappinfo> selectPlaystoreappinfo(Playstoreappinfo storeInfo);
	
	public ArrayList<Playstoreappinfo> selectMassPlaystoreappinfo(@Param("pagecount") int pagecount, @Param("size") int size);
}

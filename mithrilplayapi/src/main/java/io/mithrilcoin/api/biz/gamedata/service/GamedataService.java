package io.mithrilcoin.api.biz.gamedata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mithril.vo.playdata.Playstoreappinfo;
import io.mithrilcoin.api.biz.gamedata.mapper.GamedataMapper;
import io.mithrilcoin.api.common.redis.RedisDataRepository;

@Service
public class GamedataService {

	@Autowired
	private RedisDataRepository<String, Playstoreappinfo> playstoreRepo;
	
	@Autowired
	private GamedataMapper gamedatamapper;
	
	@PostConstruct
	public void init()
	{
		updatePlaystoreData(0);
	}

	public long updatePlaystoreData(int idx) {
		int pagecount = 0;
		int size = 10000;
		long lastIndex = 0;
		ArrayList<Playstoreappinfo> list = 	gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		
		while(list.size() > 0)
		{
			for(Playstoreappinfo appInfo : list)
			{
				if(!playstoreRepo.hasContainKey(appInfo.getPackagename()))
				{
					playstoreRepo.setData(appInfo.getPackagename(), appInfo);
					lastIndex = appInfo.getIdx();
				}
			}
			pagecount = pagecount + size;
			list = 	gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		}
		return lastIndex;
	}
	
	public ArrayList<Playstoreappinfo> selectPlayappInfo(ArrayList<Playstoreappinfo> applist)
	{
		ArrayList<String> keys = new ArrayList<>();
		for(Playstoreappinfo appinfo : applist)
		{
			keys.add(appinfo.getPackagename());
		}
		HashMap<String, Playstoreappinfo> resultMaps = playstoreRepo.getMultiData(keys);
		ArrayList<Playstoreappinfo> list = new ArrayList<>();
		
		Iterator<String> keyitor  = resultMaps.keySet().iterator();
		while(keyitor.hasNext())
		{
			Playstoreappinfo result = resultMaps.get(keyitor.next());
			if( result != null)
			{
				list.add(result);
			}
		}
		return list;
		
	}
}

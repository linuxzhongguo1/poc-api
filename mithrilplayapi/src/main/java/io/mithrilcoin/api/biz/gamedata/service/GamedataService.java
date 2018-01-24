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
		int pagecount = 0;
		int size = 10000;
		ArrayList<Playstoreappinfo> list = 	gamedatamapper.selectMassPlaystoreappinfo(pagecount, size);
		
		while(list.size() > 0)
		{
			for(Playstoreappinfo appInfo : list)
			{
				if(!playstoreRepo.hasContainKey(appInfo.getPackagename()))
				{
					playstoreRepo.setData(appInfo.getPackagename(), appInfo);
				}
			}
			pagecount = pagecount + size;
			list = 	gamedatamapper.selectMassPlaystoreappinfo(pagecount, size);
		}
		
		//playstoreRepo.getMultiData(keys)
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

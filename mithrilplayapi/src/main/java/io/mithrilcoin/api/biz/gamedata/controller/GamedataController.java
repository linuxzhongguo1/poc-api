package io.mithrilcoin.api.biz.gamedata.controller;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.playdata.Playstoreappinfo;
import io.mithril.vo.playdata.TemporalPlayData;
import io.mithrilcoin.api.biz.gamedata.service.GamedataService;

@RestController
@RequestMapping("/gamedata")
public class GamedataController {

	@Autowired
	private GamedataService gamedataService;

	@PostMapping("/validate/{accessPoint}/{idx}")
	public ArrayList<Playstoreappinfo> selectTemporalPlayData(@RequestBody ArrayList<Playstoreappinfo> gameList,
			@PathVariable String accessPoint, @PathVariable String idx) {
		return gamedataService.selectPlayappInfo(gameList);
	}
	
	@GetMapping("/update/appcash/{idx}")
	public long updateAppcash(@PathVariable int idx)
	{
		return gamedataService.updatePlaystoreData(idx);
	}
	
	@PostMapping("/insert/{email}/{accessPoint}/{idx}")
	public ArrayList<TemporalPlayData> insertPlaydata(@RequestBody ArrayList<TemporalPlayData> dataList, @PathVariable String email
			,@PathVariable String accessPoint, @PathVariable String idx) throws Exception
	{
		String userEmail = URLDecoder.decode(email,"UTF-8");
		return gamedataService.insertPlayData(dataList, userEmail);	
	}
	@PostMapping("/insert/reward/{email}/{accessPoint}/{idx}")
	public TemporalPlayData insertPlaydataReward(@RequestBody TemporalPlayData playdata, @PathVariable String email
			,@PathVariable String accessPoint, @PathVariable String idx) throws Exception
	{
		String userEmail = URLDecoder.decode(email,"UTF-8");
		return gamedataService.insertRewardData(playdata, userEmail);

	}
	
	
}

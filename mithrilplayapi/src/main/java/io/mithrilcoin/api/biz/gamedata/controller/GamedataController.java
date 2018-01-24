package io.mithrilcoin.api.biz.gamedata.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.playdata.Playstoreappinfo;
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
	
	@GetMapping("/update/appcash")
	public long updateAppcash()
	{
		return gamedataService.updatePlaystoreData();
	}
}
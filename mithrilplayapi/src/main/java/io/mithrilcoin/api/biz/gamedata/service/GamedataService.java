package io.mithrilcoin.api.biz.gamedata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.member.Member;
import io.mithril.vo.mtp.MtpHistory;
import io.mithril.vo.playdata.PlayData;
import io.mithril.vo.playdata.Playstoreappinfo;
import io.mithril.vo.playdata.TemporalPlayData;
import io.mithrilcoin.api.biz.gamedata.mapper.GamedataMapper;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.mtp.service.MtpService;
import io.mithrilcoin.api.common.redis.RedisDataRepository;

@Service
public class GamedataService {

	private static final long VALID_PLAY_TIME = 60000;
	@Autowired
	private RedisDataRepository<String, Playstoreappinfo> playstoreRepo;

	@Autowired
	private GamedataMapper gamedatamapper;

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private MtpService mtpService;

	@PostConstruct
	public void init() {
		updatePlaystoreData(0);
	}

	public long updatePlaystoreData(int idx) {
		int pagecount = 0;
		int size = 10000;
		long lastIndex = 0;
		ArrayList<Playstoreappinfo> list = gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		if(list.size() == 0)
		{
			return idx;
		}
		while (list.size() > 0) {
			for (Playstoreappinfo appInfo : list) {
//				if (!playstoreRepo.hasContainKey(appInfo.getPackagename())) {
					playstoreRepo.setData(appInfo.getPackagename(), appInfo);
					lastIndex = appInfo.getIdx();
//				}
			}
			pagecount = pagecount + size;
			list = gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		}
		
		return lastIndex;
	}
	
	public ArrayList<Long> deletePlaystoreData(ArrayList<String> pacakagelist)
	{
		HashMap<String, Playstoreappinfo> resultMaps = playstoreRepo.getMultiData(pacakagelist);
		ArrayList<Long> list = new ArrayList<>();

		Iterator<String> keyitor = resultMaps.keySet().iterator();
		while (keyitor.hasNext()) {
			Playstoreappinfo result = resultMaps.get(keyitor.next());
			if (result != null) {
				list.add(result.getIdx());
			}
		}
		
		playstoreRepo.deleteData(pacakagelist);
		
		return list;
	}
	

	public ArrayList<Playstoreappinfo> selectPlayappInfo(ArrayList<Playstoreappinfo> applist) {
		ArrayList<String> keys = new ArrayList<>();
		for (Playstoreappinfo appinfo : applist) {
			keys.add(appinfo.getPackagename());
		}
		HashMap<String, Playstoreappinfo> resultMaps = playstoreRepo.getMultiData(keys);
		ArrayList<Playstoreappinfo> list = new ArrayList<>();

		Iterator<String> keyitor = resultMaps.keySet().iterator();
		while (keyitor.hasNext()) {
			Playstoreappinfo result = resultMaps.get(keyitor.next());
			if (result != null) {
				list.add(result);
			}
		}
		return list;

	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public ArrayList<TemporalPlayData> insertPlayData(ArrayList<TemporalPlayData> dataList, String email) {

		// 1 게임 인 것들만 분리
		ArrayList<TemporalPlayData> gamePlaydatalist = getGameAppData(dataList);
		// 2 분리된 것들 중에서 select 후 없는 애들은 insert
		Member meber = new Member();
		meber.setEmail(email);
		ArrayList<Member> memberlist = memberMapper.selectMember(meber);
		ArrayList<TemporalPlayData> rewardedList = new ArrayList<>();
		ArrayList<TemporalPlayData> newList = new ArrayList<>();
		ArrayList<TemporalPlayData> exceptList = new ArrayList<>(); 
		ArrayList<TemporalPlayData> noneList = new ArrayList<>(); 
		if (memberlist.size() > 0) {
			Member findmember = memberlist.get(0);
			long member_idx = findmember.getIdx();
			ArrayList<TemporalPlayData> todayList = gamedatamapper.selectTodayPlayData(member_idx);
			
			for (TemporalPlayData gamdata : gamePlaydatalist) // 화면에서 올라온 데이터
			{
				boolean findFlag = false;
				for (TemporalPlayData data : todayList) // 오늘 저장된 데이터
				{
					if (data.getPackagename().equals(gamdata.getPackagename())) {
						findFlag = true;
						// 보상이 완료된 상태라면
						if ("P001001".equals(data.getState())) {
							gamdata.setValid("true");
							exceptList.add(gamdata);
						} else {
							gamdata.setValid("false");
							rewardedList.add(gamdata);
						}
						gamdata.setReward(data.getReward());
						gamdata.setIdx(data.getIdx());
						gamdata.setState(data.getState());
						
					}
				}
				if (!findFlag) {
					if (gamdata.getPlaytime() >= VALID_PLAY_TIME) {
						gamdata.setValid("true");
						insertPlayData(gamdata, member_idx, email);
						newList.add(gamdata);
					} else {
						gamdata.setValid("false");
						noneList.add(gamdata);
					}
					
				}
				
			}
			rewardedList.addAll(exceptList);
			rewardedList.addAll(newList);
			rewardedList.addAll(noneList);
		} else {
			return null;
		}
		return rewardedList;
	}

	private TemporalPlayData insertPlayData(TemporalPlayData data, long member_idx, String email) {
		PlayData playdata = new PlayData();
		playdata.setMember_idx(member_idx);
		playdata.setModify_member_id(email);
		playdata.setPackagename(data.getPackagename());
		playdata.setTitle(data.getTitle());
		playdata.setPlaytime(data.getPlaytime());
		// 보상가능
		playdata.setState("P001001");
		// 플레이 시간 타입 현재는 이거밖에 없음.
		playdata.setTypecode("T004001");
		gamedatamapper.insertPlayData(playdata);
		data.setIdx(playdata.getIdx());
		data.setState(playdata.getState());

		return data;
	}

	private ArrayList<TemporalPlayData> getGameAppData(ArrayList<TemporalPlayData> dataList) {
		ArrayList<Playstoreappinfo> applist = new ArrayList<>();
		ArrayList<TemporalPlayData> gamedatalist = new ArrayList<>();
		for (TemporalPlayData playdata : dataList) {
			Playstoreappinfo info = new Playstoreappinfo();
			info.setPackagename(playdata.getPackagename());
			applist.add(info);
		}
		applist = selectPlayappInfo(applist);

		for (Playstoreappinfo appdata : applist) {
			for (TemporalPlayData data : dataList) {
				if (appdata.getPackagename().equals(data.getPackagename())) {
					data.setTitle(appdata.getTitle());
					gamedatalist.add(data);
				}
			}
		}
		return gamedatalist;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public TemporalPlayData insertRewardData(TemporalPlayData playdata, String userEmail) {

		Member meber = new Member();
		meber.setEmail(userEmail);
		ArrayList<Member> memberlist = memberMapper.selectMember(meber);
		if (memberlist.size() > 0) {
			Member findmember = memberlist.get(0);
			long member_idx = findmember.getIdx();
			ArrayList<TemporalPlayData> todayList = gamedatamapper.selectTodayPlayData(member_idx);
			for (TemporalPlayData data : todayList) {
				// 패키지 이름 같고 idx도 같고 상태가 보상가능 상태여야만 보상을 쥐어줌
				if (data.getPackagename().equals(playdata.getPackagename()) && data.getIdx() == playdata.getIdx()
						&& "P001001".equals(data.getState())) {
					// 리워드 주고
					MtpHistory history = mtpService.insertDataReward(member_idx, data.getIdx());
					updatePlayData(data, member_idx, userEmail, history);
					playdata.setReward(history.getAmount());
					playdata.setState("P001002");

				} else {
					playdata.setState(data.getState());
					playdata.setReward(data.getReward());
				}
				playdata.setValid("false");
			}
		}

		return playdata;
	}

	private void updatePlayData(TemporalPlayData playdata, long member_idx, String email, MtpHistory mtphistory) {
		PlayData mydata = new PlayData();
		mydata.setIdx(playdata.getIdx());
		mydata.setMember_idx(member_idx);
		mydata.setModify_member_id(email);
		mydata.setPlaytime(playdata.getPlaytime());
		mydata.setMtp_idx(mtphistory.getIdx());
		mydata.setReward(mtphistory.getAmount());
		// 보상완료
		mydata.setState("P001002");

		gamedatamapper.updatePlaydata(mydata);

	}

	public ArrayList<PlayData> selectNopagePlaydata(String email) {
		Member meber = new Member();
		meber.setEmail(email);
		ArrayList<Member> memberlist = memberMapper.selectMember(meber);
		long member_idx = memberlist.get(0).getIdx();
		return gamedatamapper.selectTotalPlaydataNopage(member_idx);

	}

}

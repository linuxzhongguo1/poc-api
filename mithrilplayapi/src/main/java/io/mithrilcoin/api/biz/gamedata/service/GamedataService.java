package io.mithrilcoin.api.biz.gamedata.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.member.Member;
import io.mithril.vo.mtp.MtpHistory;
import io.mithril.vo.playdata.PlayData;
import io.mithril.vo.playdata.Playhistory;
import io.mithril.vo.playdata.Playstoreappinfo;
import io.mithril.vo.playdata.TemporalPlayData;
import io.mithrilcoin.api.biz.gamedata.mapper.GamedataMapper;
import io.mithrilcoin.api.biz.member.mapper.MemberMapper;
import io.mithrilcoin.api.biz.mtp.service.MtpService;
import io.mithrilcoin.api.common.redis.RedisDataRepository;
import io.mithrilcoin.api.util.CollectionUtil;
import io.mithrilcoin.api.util.DateUtil;

@Service
public class GamedataService {

	public static final long VALID_PLAY_TIME = 60000;
	private static final int ONEDAY_MAX_REWARD = 3;

	@Autowired
	private RedisDataRepository<String, Playstoreappinfo> playstoreRepo;

	@Autowired
	private GamedataMapper gamedatamapper;

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private MtpService mtpService;

	@Autowired
	private DateUtil dateUtil;

	@Autowired
	private CollectionUtil collectionUtil;

	@PostConstruct
	public void init() {
		// updatePlaystoreData(0);
	}

	public long updatePlaystoreData(int idx) {
		int pagecount = 0;
		int size = 10000;
		long lastIndex = 0;
		ArrayList<Playstoreappinfo> list = gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		if (list.size() == 0) {
			return idx;
		}
		while (list.size() > 0) {
			for (Playstoreappinfo appInfo : list) {
				// if (!playstoreRepo.hasContainKey(appInfo.getPackagename())) {
				playstoreRepo.setData(appInfo.getPackagename(), appInfo);
				lastIndex = appInfo.getIdx();
				// }
			}
			pagecount = pagecount + size;
			list = gamedatamapper.selectMassPlaystoreappinfo(pagecount, size, idx);
		}

		return lastIndex;
	}

	public ArrayList<Long> deletePlaystoreData(ArrayList<String> pacakagelist) {
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
		ArrayList<TemporalPlayData> todayPlaydatalist = new ArrayList<>();
		// 2 분리된 것들 중에서 select 후 없는 애들은 insert
		Member meber = new Member();
		meber.setEmail(email);
		ArrayList<Member> memberlist = memberMapper.selectMember(meber);
		long member_idx = 0;
		if (memberlist.size() > 0) {
			Member findmember = memberlist.get(0);
			member_idx = findmember.getIdx();
		}
		if (member_idx > 0) {
			todayPlaydatalist = gamedatamapper.selectTodayPlayData(member_idx);

			int rewardCount = 0;
			boolean todayrewardEnd = false;
			for (TemporalPlayData data : todayPlaydatalist) {
				if (data.getReward() > 0) {
					rewardCount++;
				}
			}
			if (rewardCount >= ONEDAY_MAX_REWARD) {
				todayrewardEnd = true;
			}

			ArrayList<TemporalPlayData> todayHistoryList = gamedatamapper.selectTodayPlayDataHistory(member_idx);
			// get today data list
			HashMap<String, ArrayList<TemporalPlayData>> todayHistoryMap = collectionUtil
					.list2MapArrayValue(todayHistoryList, "packagename");
			HashMap<String, TemporalPlayData> todayPlayMap = collectionUtil.list2MapSingleValue(todayPlaydatalist,
					"packagename");
			LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
			Date midnight = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			long todaylong = midnight.getTime();
			// 24 * 60 * 60 = 86400
			long tommorowlong = todaylong + 86400000;
			
			// 오늘 플레이한 전체 내역
			for (TemporalPlayData gamedata : gamePlaydatalist) {
				// 시작 시간과 끝시간이 모두 숫자로 여야 함미다.
				// 게임 종료 시간을 기준으로 오늘 00:00 부터 익일 00:00:00 전까지  
				if (!Pattern.matches("^[0-9]+$", gamedata.getStarttime())
						|| !Pattern.matches("^[0-9]+$", gamedata.getEndtime())
						|| todaylong >= Long.parseLong(gamedata.getEndtime())
						|| tommorowlong <= Long.parseLong(gamedata.getEndtime())) {
					continue;
				}

				String packagename = gamedata.getPackagename();
				// 오늘 게임한 내역에 이미 있는 패키지라면.
				if (todayHistoryMap.containsKey(packagename)) {
					ArrayList<TemporalPlayData> list = todayHistoryMap.get(packagename);
					boolean flag = false;
					for (TemporalPlayData data : list) {
						if (data.getStarttime().equals(gamedata.getStarttime())
								|| data.getEndtime().equals(gamedata.getEndtime())) {
							flag = true;
						}
					}
					if( !flag )
					{
						gamedata.setIdx(todayPlayMap.get(packagename).getIdx());
						insertPlayhistory(gamedata);
					}
				} else {
					gamedata = insertPlayData(gamedata, member_idx, email);
					insertPlayhistory(gamedata);
					todayPlayMap.put(packagename, gamedata);
					ArrayList<TemporalPlayData> historyList = new ArrayList<>();
					historyList.add(gamedata);
					todayHistoryMap.put(packagename, historyList);
				}
				TemporalPlayData playdata = todayPlayMap.get(gamedata.getPackagename());
				long playtime = Long.parseLong(gamedata.getEndtime()) - Long.parseLong(gamedata.getStarttime());
				long totalPlaytime = playdata.getPlaytime() + playtime;

				playdata.setPlaytime(totalPlaytime);

			} // end of for

			return getRewardedPlaydata(todayPlayMap, todayrewardEnd);
		}
		return todayPlaydatalist;
	}

	private ArrayList<TemporalPlayData> getRewardedPlaydata(HashMap<String, TemporalPlayData> todayPlayMap,
			boolean rewardEnd) {

		Iterator<String> keys = todayPlayMap.keySet().iterator();
		ArrayList<TemporalPlayData> resultList = new ArrayList<>();
		while (keys.hasNext()) {
			TemporalPlayData playData = todayPlayMap.get(keys.next());
			if (!rewardEnd) {
				if (playData.getReward() == 0 && playData.getPlaytime() >= VALID_PLAY_TIME) {
					playData.setValid("true");
				} else {
					playData.setValid("false");
				}
			} else {
				playData.setValid("false");
			}
			resultList.add(playData);
		}
		PlaydataComparer compareror = new PlaydataComparer();
		Collections.sort(resultList, compareror);

		return resultList;
	}

	private TemporalPlayData insertPlayData(TemporalPlayData data, long member_idx, String email) {
		PlayData playdata = new PlayData();
		playdata.setMember_idx(member_idx);
		playdata.setModify_member_id(email);
		playdata.setPackagename(data.getPackagename());
		playdata.setTitle(data.getTitle());
		playdata.setAlttitle(data.getAlttitle());
		playdata.setPlaytime(data.getPlaytime());
		playdata.setVersion(data.getVersion());
		// 보상가능
		playdata.setState("P001001");
		// 플레이 시간 타입 현재는 이거밖에 없음.
		playdata.setTypecode("T004001");
		String now = dateUtil.getUTCNow();
		playdata.setModifydate(now);
		playdata.setRegistdate(now);
		gamedatamapper.insertPlayData(playdata);
		data.setIdx(playdata.getIdx());
		data.setState(playdata.getState());
		data.setPlaydate(now);
		return data;
	}

	private Playhistory insertPlayhistory(TemporalPlayData playdata) {
		Playhistory history = new Playhistory();
		history.setPlaydata_idx(playdata.getIdx());
		history.setStarttime(playdata.getStarttime());
		history.setEndtime(playdata.getEndtime());
		String now = dateUtil.getUTCNow();
		history.setRegistdate(now);
		gamedatamapper.insertPlayhistory(history);

		return history;
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
			
			ArrayList<TemporalPlayData> todayHistoryList = gamedatamapper.selectPlayDataHistory(member_idx , playdata.getIdx());
			long rewardtime = 0;
			for(TemporalPlayData historyData : todayHistoryList)
			{
				if(dateUtil.isToday(historyData.getEndtime()))
				{
					rewardtime += Long.parseLong(historyData.getEndtime()) - Long.parseLong(historyData.getStarttime());
				}
			}
			if( rewardtime < VALID_PLAY_TIME || playdata.getPlaytime() != rewardtime)
			{
				playdata.setValid("false");
				return playdata;
			}
			
			ArrayList<TemporalPlayData> todayList = gamedatamapper.selectTodayPlayData(member_idx);
			int count = 0;
			for (TemporalPlayData data : todayList) {
				if (data.getState().equals("P001002")) {
					count++;
				}
				if (count == ONEDAY_MAX_REWARD) {
					playdata.setValid("false");
					return playdata;
				}
			}

			for (TemporalPlayData data : todayList) {

				// 패키지 이름 같고 idx도 같고 상태가 보상가능 상태여야만 보상을 쥐어줌
				if (data.getPackagename().equals(playdata.getPackagename()) && data.getIdx() == playdata.getIdx()
						&& "P001001".equals(data.getState())) {
					// 리워드 주고
					MtpHistory history = mtpService.insertDataReward(member_idx, data.getIdx());
					data.setAlttitle(playdata.getAlttitle());
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
		mydata.setAlttitle(playdata.getAlttitle());
		// 보상완료
		mydata.setState("P001002");
		String now = dateUtil.getUTCNow();
		mydata.setModifydate(now);
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

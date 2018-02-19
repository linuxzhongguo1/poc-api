package io.mithrilcoin.api.biz.gamedata.service;

import java.util.Comparator;

import io.mithril.vo.playdata.TemporalPlayData;

public class PlaydataComparer implements Comparator<TemporalPlayData>{

	@Override
	public int compare(TemporalPlayData p1, TemporalPlayData p2) {
		
		int gap = (int)(p1.getReward() - p2.getReward());
		
		if(gap == 0)
		{
			if(p1.getReward() > 0)
			{
				long time = Long.parseLong(p1.getStarttime()) -  Long.parseLong(p2.getStarttime());
				return (int) time;
			}
			else
			{
				int p1ValidGap = 0;
				int p2ValidGap = 0;
				if(p1.getValid().equals("true"))
				{
					p1ValidGap = 1;
				}
				else
				{
					p1ValidGap = 0;
				}
				if(p2.getValid().equals("true"))
				{
					p2ValidGap = 1;
				}
				else
				{
					p2ValidGap = 0;
				}
				return p1ValidGap - p2ValidGap;
			}
		}
		return gap;
	}

	

}

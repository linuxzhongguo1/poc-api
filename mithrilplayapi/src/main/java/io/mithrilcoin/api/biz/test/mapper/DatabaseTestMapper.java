package io.mithrilcoin.api.biz.test.mapper;

import org.springframework.stereotype.Repository;

import io.mithril.vo.playdata.Rating;

@Repository
public interface DatabaseTestMapper {

	public String getNow();
	
	public Rating selectRating();
}

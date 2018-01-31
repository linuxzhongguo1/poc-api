package io.mithrilcoin.api.admin.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import io.mithril.vo.admin.Admin;

@Repository
public interface AdminMapper {

	public ArrayList<Admin> selectAdmin(Admin admin);
	
	public ArrayList<Admin> selectAdminList(Admin admin);

	public int insertAdmin(Admin admin);

	public int updateAdmin(Admin admin);

}

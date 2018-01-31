package io.mithrilcoin.api.admin.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mithril.vo.admin.Admin;
import io.mithrilcoin.api.admin.mapper.AdminMapper;
import io.mithrilcoin.api.common.security.HashingUtil;


@Service
public class AdminService {

	private static Logger logger = LoggerFactory.getLogger(AdminService.class);

	@Autowired
	private AdminMapper adminMapper;

	@Autowired
	private HashingUtil hashUtil;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Admin signupAdmin(Admin admin) {
		logger.info("AdminService signupAdmin ");
		if (admin.getId() != null && !admin.getId().equals("")) {

			ArrayList<Admin> adminlist = adminMapper.selectAdmin(admin);
			if (adminlist.size() == 0) {
				// 비밀번호 Hashing
				admin.setPassword(hashUtil.getHashedString(admin.getPassword()));
				admin.setState("A001001");
				admin.setLevel("1");
				adminMapper.insertAdmin(admin);
			}
		} else {
			admin.setIdx(-1);
		}
		return admin;
	}

	public Admin signin(Admin admin) {
		logger.info("AdminService signin ");
		if (admin.getId() != null && !admin.getId().equals("")) {
			ArrayList<Admin> adminlist = adminMapper.selectAdmin(admin);
			if (adminlist.size() > 0) {
				Admin findAdmin = adminlist.get(0);
				if (findAdmin.getPassword().equals(hashUtil.getHashedString(admin.getPassword()))) {
					findAdmin.setPassword("");
					return findAdmin;
				}
				else 
				{
					// 비번 틀림.
					admin.setIdx(-1);
				}
			}
			else
			{
				admin.setIdx(-1);
			}
		}
		return admin;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Admin updateAdmin(Admin admin) {
		logger.info("AdminService updateAdmin ");
		if (adminMapper.updateAdmin(admin) > 0) {
			return admin;
		}
		return null;
	}

	public Admin selectAdminById(String id) {
		logger.info("AdminService selectAdminById " + id);
		Admin admin = new Admin();
		admin.setId(id);
		ArrayList<Admin> adminlist = adminMapper.selectAdmin(admin);
		if (adminlist.size() > 0) {
			return adminlist.get(0);
		}
		return null;
	}

	public ArrayList<Admin> selectAdminlist(Admin admin) {
		logger.info("AdminService selectAdminlist " + admin.toString());
		ArrayList<Admin> adminlist = adminMapper.selectAdminList(admin);
		return adminlist;
	}

}

package io.mithrilcoin.api.admin.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mithril.vo.admin.Admin;
import io.mithrilcoin.api.admin.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService adminservice;

	@PostMapping("/signup/{accesspoint}/{idx}")
	public Admin signupAdmin(@RequestBody Admin admin, @PathVariable String accesspoint, @PathVariable String idx) {
		return adminservice.signupAdmin(admin);
	}

	@PostMapping("/signin/{accesspoint}/{idx}")
	public Admin signinAdmin(@RequestBody Admin admin, @PathVariable String accesspoint, @PathVariable String idx) {
		return adminservice.signin(admin);
	}

	@PostMapping("/update/{accesspoint}/{idx}")
	public Admin updateAdmin(@RequestBody Admin admin, @PathVariable String accesspoint, @PathVariable String idx) {
		return adminservice.updateAdmin(admin);
	}

	@GetMapping("/select/{id}/{accesspoint}/{idx}")
	public Admin selectAdmin(@PathVariable String id, @PathVariable String accesspoint, @PathVariable String idx) {
		return adminservice.selectAdminById(id);
	}

	@GetMapping("/selectList/{accesspoint}/{idx}")
	public ArrayList<Admin> selectAdmin(Admin admin, @PathVariable String accesspoint, @PathVariable String idx) {
		return adminservice.selectAdminlist(admin);
	}

}

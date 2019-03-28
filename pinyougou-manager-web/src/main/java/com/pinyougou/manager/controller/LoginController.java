package com.pinyougou.manager.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营商登陆
 * @author lenovo
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {
	
	@RequestMapping("/getName")
	public Map getName() {
		//从security上下文中获取登陆者姓名
		String username=SecurityContextHolder.getContext().getAuthentication().getName(); 
		Map map = new HashMap();
		map.put("username", username);
		return map;
		
	}

}

package com.pinyougou.shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登陆
 * @author lenovo
 *
 */
@RestController
@RequestMapping("/seller")
public class LoginController {

	@RequestMapping("/getName")
	public Map getName(){
		Map map = new HashMap();
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		map.put("username", username);
		return map;
	}
	
}

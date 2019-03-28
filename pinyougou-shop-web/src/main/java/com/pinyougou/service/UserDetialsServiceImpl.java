package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

/**
 * 安全认证实现类
 * @author lenovo
 *
 */
public class UserDetialsServiceImpl implements UserDetailsService {
	
	private SellerService sellerService;//dubbo服务引用 知道 com.pinyougou.shop.controller  没扫con.pinyougou.service包
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//权限集合
		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		TbSeller tbSeller = sellerService.findOne(username);
		if(tbSeller != null) {
			//审核通过的商家
			if(!"1".equals(tbSeller.getStatus())) {
				return null;
			}
			return new User(username, tbSeller.getPassword(), grantedAuths);
		}
		return null;
	}

}

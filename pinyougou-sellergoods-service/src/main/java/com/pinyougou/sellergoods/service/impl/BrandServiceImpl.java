package com.pinyougou.sellergoods.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

/**
 * 品牌接口实现类
 * @author lenovo
 *
 */

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample brandExample = new TbBrandExample();
		Criteria criteria = brandExample.createCriteria(); //创建查询条件类
		if(brand != null) {
			//添加 ‘品牌名称’查询条件
			if(brand.getName() != null && brand.getName().length() > 0) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			//添加 ‘品牌首字母’查询条件
			if(brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(brandExample);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Override
	public void add(TbBrand tbBrand) {
		brandMapper.insert(tbBrand);	
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}	
	}

	@Override
	public List<Map> selectOptionList() {
		System.out.println(brandMapper.selectOptionList());
		return brandMapper.selectOptionList();
	}

}

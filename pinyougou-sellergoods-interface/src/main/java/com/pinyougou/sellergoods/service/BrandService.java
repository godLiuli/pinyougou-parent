package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌服务层接口
 * @author lenovo
 *
 */
public interface BrandService {
	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbBrand> findAll();
	
	/**
	 * 分页
	 * @param pageNum 当前页码
	 * @param pageSize 每页数量
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	/**
	 * 分页条件查询
	 * @param brand 查询条件
	 * @param pageNum 当前页
	 * @param pageSize 每页数量
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
	
	/**
	 * 添加品牌
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
		
	/**
	 * 根据id查找品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改品牌
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	/**
	 * 批量删除
	 * @param id
	 */
	public void delete(Long[] ids);
	
	/**
	 * 获取品牌列表
	 * @return
	 */
	List<Map> selectOptionList();
	
	
}

package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);	
		
		saveToRedis(); //把品牌和规格列表存入缓存中     {"itemCat":[],"brandList":[],specList:[]}
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public List<Map> selectOptionTemp() {
			return typeTemplateMapper.selectOptionTemp();
		}

		@Override
		public List<Map> findSpecList(Long id) {
			TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);//获取指定id的模板对象
			//[{"id":1,"text":"联想"},{"id":3,"text":"三星"}]
			//String brandIds = template.getBrandIds();
			//List<Map> list = JSON.parseArray(brandIds, Map.class) ;
			List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
			for(Map map:list) {
				//规格id
				Long specId = new Long((Integer)map.get("id"));
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
				criteria.andSpecIdEqualTo(specId);
				//获取对应id规格的 规格列表
				List<TbSpecificationOption> option = specificationOptionMapper.selectByExample(example);
				map.put("options", option);	
			}
			return list;
		}
		
		/**
		 * 把品牌和规格列表存入缓存中     {"itemCat":[],"brandList":[],specList:[]}
		 */
		private void saveToRedis() {  
			//获取模板集合
			List<TbTypeTemplate> list = findAll();
			for (TbTypeTemplate typeTemplate : list) {
				//1.品牌-存入缓存中
				List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
				redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
				//2.规格列表-存入缓存中
				List<Map> specList = findSpecList(typeTemplate.getId());
				redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
				System.out.println("更新品牌和规格列表缓存。。。");
			}
		}
	
}

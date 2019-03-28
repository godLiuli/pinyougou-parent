package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	
	/**
	 * 批量跟新（商家审核商品）
	 * @param ids
	 * @param auditStatus
	 */
	public void update(Long[] ids,String auditStatus);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum,int pageSize);
	
	/**
	 * 批量提交审核商品
	 * @param ids
	 */
	public void update(Long[] ids);
	
	/**
	 * 商品上下架
	 * @param ids
	 */
	public void isMarket(Long[] ids,String marketable);
	
	/**
	 * 根据商品id和状态查询sku列表
	 * @param ids
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status);
}

package com.pinyougou.page.service;
/**
 * 商品详情页接口
 * @author lenovo
 *
 */
public interface ItemPageService {
	/**
	 * 生成商品详细页
	 * @param goodId
	 * @return
	 */
	public boolean getItemHtml(long goodId);
	
	/**
	 * 删除商品详细页
	 * @param goodId
	 * @return
	 */
	public boolean delItemHtml(Long[] ids);
}

package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.core.annotation.Order;

import java.io.Serializable;
import java.util.List;

/**
 * 封装有订单，以及该订单对应的item集合，用于个人中心的订单详情展示
 */
public class OrderWithItems implements Serializable {

    private TbOrder tbOrder;
    private List<TbOrderItem>orderItemList;
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public TbOrder getTbOrder() {
        return tbOrder;
    }

    public void setTbOrder(TbOrder tbOrder) {
        this.tbOrder = tbOrder;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}

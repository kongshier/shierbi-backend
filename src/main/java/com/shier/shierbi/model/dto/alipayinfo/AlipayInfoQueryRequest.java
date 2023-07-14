package com.shier.shierbi.model.dto.alipayinfo;

import com.shier.shierbi.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 次数订单表
 * @author Shier
 * @TableName alipay_info
 */
@Data
public class AlipayInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 支付宝流水账号
     */
    private Long alipayAccountNo;

    /**
     * 支付宝唯一id
     */
    private String alipayId;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 交易金额
     */
    private Double totalAmount;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}
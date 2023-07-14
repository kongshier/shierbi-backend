package com.shier.shierbi.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 次数订单表
 * @author Shier
 * @TableName alipay_info
 */

@Data
public class AlipayInfoVO implements Serializable {

    /**
     * 支付宝流水账号
     */
    private String  alipayAccountNo;

    /**
     * 生成的二维码信息
     */
    private String qrCode;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 支付宝唯一id
     */
    private String alipayId;

    /**
     * 交易金额
     */
    private Double totalAmount;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private Integer payStatus;

}
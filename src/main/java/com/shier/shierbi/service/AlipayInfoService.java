package com.shier.shierbi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shier.shierbi.model.entity.AlipayInfo;

/**
* @author Shier
* @description 针对表【alipay_info(次数订单表)】的数据库操作Service
* @createDate 2023-07-12 17:05:42
*/
public interface AlipayInfoService extends IService<AlipayInfo> {

    long getPayNo(long orderId, long userId);

}

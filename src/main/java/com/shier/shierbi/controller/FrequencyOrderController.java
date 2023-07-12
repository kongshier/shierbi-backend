package com.shier.shierbi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shier.shierbi.common.BaseResponse;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.common.ResultUtils;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.model.entity.AiFrequencyOrder;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.vo.AiFrequencyOrderVO;
import com.shier.shierbi.ordermq.OrderManageProducer;
import com.shier.shierbi.service.AiFrequencyOrderService;
import com.shier.shierbi.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 * @author Shier
 */
@RestController
@RequestMapping("/order")
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class FrequencyOrderController {

    @Resource
    private AiFrequencyOrderService aiFrequencyOrderService;

    @Resource
    private OrderManageProducer orderManageProducer;

    @Resource
    private UserService userService;

    final Double price = 0.1;

    /**
     * 添加订单
     *
     * @param total
     * @param request
     * @return
     */
    @PostMapping("/addorder")
    public BaseResponse<Boolean> addOrder(long total, HttpServletRequest request) {

        if (total <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的次数");
        }

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        double totalAmount = total * price;
        AiFrequencyOrder frequencyOrder = new AiFrequencyOrder();
        frequencyOrder.setUserId(userId);
        frequencyOrder.setPrice(price);
        frequencyOrder.setTotalAmount(totalAmount);
        frequencyOrder.setPurchaseQuantity(total);
        boolean save = aiFrequencyOrderService.save(frequencyOrder);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //将订单发送到延迟队列
        orderManageProducer.sendManage(frequencyOrder);
        return ResultUtils.success(true);
    }

    /**
     * 获取订单列表
     *
     * @param request
     * @return
     */
    @GetMapping("/orderlist")
    public BaseResponse<List<AiFrequencyOrderVO>> getOrderList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        QueryWrapper<AiFrequencyOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        List<AiFrequencyOrder> frequencyOrderList = aiFrequencyOrderService.list(wrapper);
        List<AiFrequencyOrderVO> frequencyOrderVOList = new ArrayList<>();
        for (AiFrequencyOrder frequencyOrder : frequencyOrderList) {
            AiFrequencyOrderVO frequencyOrderVO = new AiFrequencyOrderVO();
            BeanUtils.copyProperties(frequencyOrder, frequencyOrderVO);
            frequencyOrderVOList.add(frequencyOrderVO);
        }
        return ResultUtils.success(frequencyOrderVOList);
    }
}

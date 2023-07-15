package com.shier.shierbi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shier.shierbi.annotation.AuthCheck;
import com.shier.shierbi.common.BaseResponse;
import com.shier.shierbi.common.DeleteRequest;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.common.ResultUtils;
import com.shier.shierbi.constant.CommonConstant;
import com.shier.shierbi.constant.UserConstant;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.exception.ThrowUtils;
import com.shier.shierbi.model.dto.order.AiFrequencyOrderCancelRequest;
import com.shier.shierbi.model.dto.order.AiFrequencyOrderQueryRequest;
import com.shier.shierbi.model.dto.order.AiFrequencyOrderUpdateRequest;
import com.shier.shierbi.model.entity.AiFrequencyOrder;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.enums.PayOrderEnum;
import com.shier.shierbi.model.vo.AiFrequencyOrderVO;
import com.shier.shierbi.ordermq.OrderManageProducer;
import com.shier.shierbi.service.AiFrequencyOrderService;
import com.shier.shierbi.service.UserService;
import com.shier.shierbi.utils.SqlUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 *
 * @author Shier
 */
@RestController
@RequestMapping("/order")
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class AiFrequencyOrderController {

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
    @PostMapping("/add")
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
    @GetMapping("/list")
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

    /**
     * 分页获取订单列表
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/byPage")
    @ApiOperation(value = "（管理员）分页获取订单列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AiFrequencyOrder>> listOrderByPage(@RequestBody AiFrequencyOrderQueryRequest orderQueryRequest,
                                                                HttpServletRequest request) {
        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();
        Page<AiFrequencyOrder> orderPage = aiFrequencyOrderService.page(new Page<>(current, size),
                aiFrequencyOrderService.getOrderQueryWrapper(orderQueryRequest));
        return ResultUtils.success(orderPage);
    }

    /**
     * 分页获取当前用户的订单
     *
     * @param aiFrequencyOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取个人订单")
    public BaseResponse<Page<AiFrequencyOrder>> listMyOrderByPage(@RequestBody AiFrequencyOrderQueryRequest aiFrequencyOrderQueryRequest,
                                                                  HttpServletRequest request) {
        if (aiFrequencyOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        aiFrequencyOrderQueryRequest.setUserId(loginUser.getId());
        long current = aiFrequencyOrderQueryRequest.getCurrent();
        long size = aiFrequencyOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiFrequencyOrder> chartPage = aiFrequencyOrderService.page(new Page<>(current, size),
                getQueryWrapper(aiFrequencyOrderQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 删除订单
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除订单")
    public BaseResponse<Boolean> deleteOrder(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean deleteResult = aiFrequencyOrderService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!deleteResult, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(deleteResult);
    }

    /**
     * 取消订单
     *
     * @param cancelRequest
     * @return
     */
    @PostMapping("/cancel")
    @ApiOperation(value = "取消订单")
    public BaseResponse<Boolean> cancelOrder(@RequestBody AiFrequencyOrderCancelRequest cancelRequest, HttpServletRequest request) {
        Long id = cancelRequest.getId();
        Long userId = cancelRequest.getUserId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (cancelRequest == null || cancelRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiFrequencyOrder order = new AiFrequencyOrder();
        BeanUtils.copyProperties(cancelRequest, order);
        order.setOrderStatus(Integer.valueOf(PayOrderEnum.CANCEL_ORDER.getValue()));
        boolean result = aiFrequencyOrderService.updateById(order);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改订单
     *
     * @param orderUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改订单信息")
    public BaseResponse<Boolean> updateOrder(@RequestBody AiFrequencyOrderUpdateRequest orderUpdateRequest,
                                             HttpServletRequest request) {
        if (orderUpdateRequest == null || orderUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiFrequencyOrder aiFrequencyOrder = new AiFrequencyOrder();
        BeanUtils.copyProperties(orderUpdateRequest, aiFrequencyOrder);
        boolean result = aiFrequencyOrderService.updateOrderInfo(orderUpdateRequest, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 修改之后进入延迟队列
        orderManageProducer.sendManage(aiFrequencyOrder);
        return ResultUtils.success(true);
    }

    /**
     * 获取查询包装类
     *
     * @param aiFrequencyOrderQueryRequest 图表查询条件
     * @return 查询结果
     */
    private QueryWrapper<AiFrequencyOrder> getQueryWrapper(AiFrequencyOrderQueryRequest aiFrequencyOrderQueryRequest) {
        Long id = aiFrequencyOrderQueryRequest.getId();
        Long userId = aiFrequencyOrderQueryRequest.getUserId();
        String sortField = aiFrequencyOrderQueryRequest.getSortField();
        String sortOrder = aiFrequencyOrderQueryRequest.getSortOrder();

        QueryWrapper<AiFrequencyOrder> queryWrapper = new QueryWrapper<>();
        if (aiFrequencyOrderQueryRequest == null) {
            return queryWrapper;
        }
        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }
}

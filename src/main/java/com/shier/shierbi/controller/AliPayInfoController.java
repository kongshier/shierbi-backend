package com.shier.shierbi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shier.shierbi.annotation.AuthCheck;
import com.shier.shierbi.common.BaseResponse;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.common.ResultUtils;
import com.shier.shierbi.constant.CommonConstant;
import com.shier.shierbi.constant.UserConstant;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.exception.ThrowUtils;
import com.shier.shierbi.model.dto.alipayinfo.AlipayInfoQueryRequest;
import com.shier.shierbi.model.entity.AlipayInfo;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.vo.PayInfoVO;
import com.shier.shierbi.service.AlipayInfoService;
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
 * 支付订单
 *
 * @author Shier
 */
@RestController
@RequestMapping("/payInfo")
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class AliPayInfoController {

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private UserService userService;

    /**
     * 获取支付订单列表
     *
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<PayInfoVO>> getPayInfoList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        QueryWrapper<AlipayInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        List<AlipayInfo> alipayInfos = alipayInfoService.list(wrapper);
        List<PayInfoVO> payInfoVOS = new ArrayList<>();
        for (AlipayInfo alipayInfo : alipayInfos) {
            PayInfoVO payInfoVO = new PayInfoVO();
            BeanUtils.copyProperties(alipayInfo, payInfoVO);
            payInfoVOS.add(payInfoVO);
        }
        return ResultUtils.success(payInfoVOS);
    }

    /**
     * 分页获取支付订单列表
     *
     * @param alipayInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/byPage")
    @ApiOperation(value = "（管理员）分页获取订单列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AlipayInfo>> listPayInfoByPage(@RequestBody AlipayInfoQueryRequest alipayInfoQueryRequest,
                                                            HttpServletRequest request) {
        long current = alipayInfoQueryRequest.getCurrent();
        long size = alipayInfoQueryRequest.getPageSize();
        Page<AlipayInfo> orderPage = alipayInfoService.page(new Page<>(current, size),
                getAliPayQueryWrapper(alipayInfoQueryRequest));
        return ResultUtils.success(orderPage);
    }

    /**
     * 分页获取当前用户的订单
     *
     * @param alipayInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/my/page")
    @ApiOperation(value = "获取个人支付订单")
    public BaseResponse<Page<AlipayInfo>> listMyPayInfoByPage(@RequestBody AlipayInfoQueryRequest alipayInfoQueryRequest,
                                                              HttpServletRequest request) {
        if (alipayInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        alipayInfoQueryRequest.setUserId(loginUser.getId());
        long current = alipayInfoQueryRequest.getCurrent();
        long size = alipayInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AlipayInfo> chartPage = alipayInfoService.page(new Page<>(current, size),
                getAliPayQueryWrapper(alipayInfoQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 获取查询包装类
     *
     * @param alipayInfoQueryRequest 图表查询条件
     * @return 查询结果
     */
    private QueryWrapper<AlipayInfo> getAliPayQueryWrapper(AlipayInfoQueryRequest alipayInfoQueryRequest) {

        Long id = alipayInfoQueryRequest.getId();
        Long userId = alipayInfoQueryRequest.getUserId();
        Long orderId = alipayInfoQueryRequest.getOrderId();
        String sortField = alipayInfoQueryRequest.getSortField();
        String sortOrder = alipayInfoQueryRequest.getSortOrder();

        QueryWrapper<AlipayInfo> queryWrapper = new QueryWrapper<>();
        if (alipayInfoQueryRequest == null) {
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

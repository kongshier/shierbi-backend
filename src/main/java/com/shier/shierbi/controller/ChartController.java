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
import com.shier.shierbi.model.dto.chart.*;
import com.shier.shierbi.model.entity.Chart;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.vo.BiResponse;
import com.shier.shierbi.service.ChartService;
import com.shier.shierbi.service.UserService;
import com.shier.shierbi.utils.SqlUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图表接口
 *
 * @author Shier
 */
@RestController
@RequestMapping("/chart")
@Slf4j
@Api(tags = "ChartController")
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建图表")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除图表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除图表")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "管理员更新图表信息")
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据Id获取图表")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取图表")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取我的图表")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 管理员编辑图表
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑图表")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest 图表查询条件
     * @return 查询结果
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {

        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartName = chartQueryRequest.getChartName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(goal), "goal", goal);
        queryWrapper.like(ObjectUtils.isNotEmpty(chartName), "chartName", chartName);
        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 图表文件上传（同步）
     */
    @PostMapping("/gen")
    @ApiOperation(value = "同步图表文件上传")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        BiResponse biResponse = chartService.genChartByAi(multipartFile, genChartByAiRequest, request);
        ThrowUtils.throwIf(biResponse == null, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        return ResultUtils.success(biResponse);
    }

    /**
     * 图表文件上传（异步-线程池）
     */
    @PostMapping("/gen/async")
    @ApiOperation(value = "异步线程池图表文件上传")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        BiResponse biResponse = chartService.genChartByAiAsync(multipartFile, genChartByAiRequest, request);
        ThrowUtils.throwIf(biResponse == null, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        return ResultUtils.success(biResponse);
    }

    /**
     * 图表文件上传（异步 消息队列）
     */
    @PostMapping("/gen/async/mq")
    @ApiOperation(value = "消息队列mq 异步图表文件上传")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        BiResponse biResponse = chartService.genChartByAiAsyncMq(multipartFile, genChartByAiRequest, request);
        ThrowUtils.throwIf(biResponse == null, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        return ResultUtils.success(biResponse);
    }
}

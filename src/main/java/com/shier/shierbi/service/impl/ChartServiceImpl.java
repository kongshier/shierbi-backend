package com.shier.shierbi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shier.shierbi.bizmq.BiMqMessageProducer;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.exception.ThrowUtils;
import com.shier.shierbi.manager.AiManager;
import com.shier.shierbi.manager.RedisLimiterManager;
import com.shier.shierbi.mapper.ChartMapper;
import com.shier.shierbi.model.dto.chart.GenChartByAiRequest;
import com.shier.shierbi.model.entity.Chart;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.enums.ChartStatusEnum;
import com.shier.shierbi.model.vo.BiResponse;
import com.shier.shierbi.service.AiFrequencyService;
import com.shier.shierbi.service.ChartService;
import com.shier.shierbi.service.UserService;
import com.shier.shierbi.utils.ChartUtils;
import com.shier.shierbi.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

import static com.shier.shierbi.constant.ChartConstant.*;

/**
 * @author Shier
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-05-14 19:20:33
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private UserService userService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMqMessageProducer biMqMessageProducer;

    /**
     * 图表生成（同步）
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return
     */
    @Override
    public BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");

        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");

        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 无需Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        // 调用AI
        String chartResult = aiManager.doChat(userInput.toString());
        // 解析内容
        String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
        if (splits.length < GEN_ITEM_NUM) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }
        // 首次生成的内容
        String preGenChart = splits[GEN_CHART_IDX].trim();
        String genResult = splits[GEN_RESULT_IDX].trim();
        String validGenChart = ChartUtils.getValidGenChart(preGenChart);

        //ThrowUtils.throwIf(StringUtils.isBlank(preGenChart), ErrorCode.PARAMS_ERROR, "AI 生成数据为空，请重试~");

        // 插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setGenChart(preGenChart);
        //chart.setGenChart(validGenChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        // 返回到前端
        BiResponse biResponse = new BiResponse();
        //biResponse.setGenChart(validGenChart);
        biResponse.setGenChart(preGenChart);
        biResponse.setChartId(chart.getId());
        biResponse.setGenResult(genResult);
        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return biResponse;
    }

    /**
     * 异步图表生成-线程池
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return
     */
    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");

        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");

        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 无需Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 先插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 任务队列已满
        if (threadPoolExecutor.getQueue().size() > threadPoolExecutor.getMaximumPoolSize()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前任务队列已满");
        }

        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> {
            // 等待-->执行中--> 成功/失败
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
            boolean updateChartById = this.updateById(updateChart);
            if (!updateChartById) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
                return;
            }
            // 调用AI
            String chartResult = aiManager.doChat(userInput.toString());

            // 解析内容
            String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
            if (splits.length < GEN_ITEM_NUM) {
                //throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
                handleChartUpdateError(chart.getId(), "AI生成错误");
                return;
            }
            // 生成前的内容
            String preGenChart = splits[GEN_CHART_IDX].trim();
            String genResult = splits[GEN_RESULT_IDX].trim();
            // 生成后端检验
            String validGenChart = ChartUtils.getValidGenChart(preGenChart);

            // 生成的最终结果-成功
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(preGenChart);
            //updateChartResult.setGenChart(validGenChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
            }
        }, threadPoolExecutor);

        // 等待太久了，抛异常，超时时间
        try {
            completableFuture.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            // 超时失败了
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            this.updateById(updateChartFailed);
            throw new RuntimeException(e);
        }

        // 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());

        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return biResponse;
    }


    /**
     * 异步RabbitMQ 消息队列 图表生成
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return
     */
    @Override
    public BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");

        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 2M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");

        // 用户每秒限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 无需Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 先插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 任务队列已满
        if (threadPoolExecutor.getQueue().size() > threadPoolExecutor.getMaximumPoolSize()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前任务队列已满");
        }

        Long newChartId = chart.getId();
        biMqMessageProducer.sendMessage(String.valueOf(newChartId));

        // 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());

        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");
        return biResponse;
    }


    /**
     * 图表更新错误
     *
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setChartStatus(ChartStatusEnum.FAILED.getValue());
        updateChartResult.setExecMessage("图表更新失败！！");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}





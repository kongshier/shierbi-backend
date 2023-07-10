package com.shier.shierbi.bizmq;

import com.rabbitmq.client.Channel;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.constant.BiMqConstant;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.manager.AiManager;
import com.shier.shierbi.model.entity.Chart;
import com.shier.shierbi.model.enums.ChartStatusEnum;
import com.shier.shierbi.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.shier.shierbi.constant.ChartConstant.*;

/**
 * @author Shier
 * CreateTime 2023/6/24 15:53
 * BI项目 消费者
 */
@Component
@Slf4j
public class BiMqMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    /**
     * 指定程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE}, ackMode = "MANUAL")
    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message={}", message);
        if (StringUtils.isBlank(message)) {
            // 消息为空，则拒绝掉消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接受到的消息为空");
        }
        // 获取到图表的id
        long chartId = Long.parseLong(message);
        // 从数据库中取出id
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        // 等待-->执行中--> 成功/失败
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
        boolean updateChartById = chartService.updateById(updateChart);
        if (!updateChartById) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
            return;
        }
        // 调用AI
        String chartResult = aiManager.doChat(buildUserInput(chart));

        // 解析内容
        String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
        if (splits.length < GEN_ITEM_NUM) {
            // 拒绝消息
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        // 生成前的内容
        String preGenChart = splits[GEN_CHART_IDX].trim();

        if (StringUtils.isBlank(preGenChart)){
            // 内容生成错误，拒绝消息
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"AI生成错误");
        }

        // 判断是否包含有双引号，是否符合JSON格式
        boolean flag = preGenChart.substring(0, 10).chars()
                .mapToObj(c -> (char) c)
                .anyMatch(c -> c == '"');

        if (!flag){
            // 内容生成错误，拒绝消息
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"AI生成错误");
        }

        String genResult = splits[GEN_RESULT_IDX].trim();
        // 生成后端检验
        //String validGenChart = ChartUtils.getValidGenChart(preGenChart);

        // 生成的最终结果-成功
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(preGenChart);
        //updateChartResult.setGenChart(validGenChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
        }

        // 成功，则确认消息
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建用户的输入信息
     *
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();

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
        userInput.append(chartData).append("\n");
        return userInput.toString();
    }
}

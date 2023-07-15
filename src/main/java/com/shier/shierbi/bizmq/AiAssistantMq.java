package com.shier.shierbi.bizmq;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.shier.shierbi.manager.AiManager;
import com.shier.shierbi.model.entity.AiAssistant;
import com.shier.shierbi.model.enums.ChartStatusEnum;
import com.shier.shierbi.service.AiAssistantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.shier.shierbi.constant.BiMqConstant.*;
import static com.shier.shierbi.constant.CommonConstant.AI_MODEL_ID;

/**
 * @author Shier
 * CreateTime 2023/6/25 20:07
 * AI 问答 MQ 队列
 */
@Slf4j
@Component
@AllArgsConstructor
public class AiAssistantMq {

    private final static Gson GSON = new Gson();

    @Resource
    private AiManager aiManager;
    @Resource
    private AiAssistantService aiAssistantService;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue(AI_QUESTION_QUEUE),
                    exchange = @Exchange(AI_QUESTION_EXCHANGE_NAME),
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange",value = AI_DLX_EXCHANGE_NAME)
                    }, key = AI_QUESTION_ROUTING_KEY))
    public void handle(Message message, Channel channel) throws IOException {
        AiAssistant aiAssistant = null;
        try {
            String data = new String(message.getBody());
            aiAssistant = GSON.fromJson(data, AiAssistant.class);
            String questionGoal = aiAssistant.getQuestionGoal();
            // 调用 AI
            String result = aiManager.doAiChat(AI_MODEL_ID, questionGoal);
            aiAssistant.setQuestionResult(result);
            aiAssistant.setQuestionStatus(ChartStatusEnum.SUCCEED.getValue());
            aiAssistantService.updateById(aiAssistant);
            // 交付标签，消息id
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 拒绝后丢弃
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            if (aiAssistant != null) {
                aiAssistant.setQuestionStatus(ChartStatusEnum.FAILED.getValue());
                aiAssistantService.updateById(aiAssistant);
            }
        }
    }
}

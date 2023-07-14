package com.shier.shierbi.ordermq;

import com.shier.shierbi.model.entity.AiFrequencyOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定义延迟队列
 * @author Shier
 */
@Component
public class OrderManageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    // 普通交换机的名称
    public static final String X_EXCHANGE="order_exchange";

    // routingKey
    public static final String DELAYED_ROUTING_KEY = "XA";

    public void sendManage(AiFrequencyOrder frequencyOrder){
        rabbitTemplate.convertAndSend(X_EXCHANGE,DELAYED_ROUTING_KEY,frequencyOrder);
    }
}

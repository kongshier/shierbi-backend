package com.shier.shierbi.bizmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Shier
 * CreateTime 2023/6/24 15:53
 * RabbitMQ 消费者
 */
@Component
@Slf4j
public class RabbitMqMessageConsumer {

    /**
     * 指定程序监听的消息队列和确认机制
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = {"demo_queue"}, ackMode = "MANUAL")
    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
      log.info("receiveMessage = {}",message);
        try {
            // 手动确认消息
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ///**
    // * 指定程序监听的消息队列和确认机制
    // * @param message
    // * @param channel
    // * @param deliveryTag
    // */
    //@RabbitListener(queues = {BiMqConstant.BI_QUEUE}, ackMode = "MANUAL")
    //private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
    //    try {
    //        // 手动确认消息
    //        log.info("receiveMessage = {}",message);
    //        channel.basicNack(deliveryTag,false,false);
    //    } catch (IOException e) {
    //        throw new RuntimeException(e);
    //    }
    //}
}

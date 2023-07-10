package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.shier.shierbi.constant.BiMqConstant;

import java.nio.charset.StandardCharsets;

/**
 * @author Shier
 * 单向生产消费
 */
public class SingleProducer {
    private final static String SINGLE_QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // 创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);
        try (Connection connection = factory.newConnection();
             // 建立链接，创建频道
             Channel channel = connection.createChannel()) {
            // 创建消息队列
            channel.queueDeclare(SINGLE_QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", SINGLE_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
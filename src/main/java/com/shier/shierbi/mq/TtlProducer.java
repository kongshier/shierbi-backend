package com.shier.shierbi.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.shier.shierbi.constant.BiMqConstant;

import java.nio.charset.StandardCharsets;

/**
 * @author Shier
 * 消息过期机制 - 生产者
 */
public class TtlProducer {
    private final static String TTL_QUEUE = "ttl-queue";

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
            // 创建消息队列 要删除掉，因为已经在消费者中创建了队列，没有必要再重新创建一次这个队列，如果在此处还创建队列，里面的参数必须要和消费者的参数一致
            // channel.queueDeclare(SINGLE_QUEUE_NAME, false, false, false, null);

            String message = "Hello World!";

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("60000")
                    .build();
            channel.basicPublish("my-exchange", "routing-key", properties, message.getBytes(StandardCharsets.UTF_8));

            channel.basicPublish("", TTL_QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
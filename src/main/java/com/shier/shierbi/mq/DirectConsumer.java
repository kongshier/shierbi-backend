package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.shier.shierbi.constant.BiMqConstant;

/**
 * @author Shier
 * 直接交换机 - 消费者
 */
public class DirectConsumer {

    private static final String DIRECT_EXCHANGE = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(DIRECT_EXCHANGE, "direct");

        // 创建队列，分配一个队列名称：小紫
        String queueName = "xiaozi_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, DIRECT_EXCHANGE, "xiaozi");

        // 创建队列，分配一个队列名称：小黑
        String queueName2 = "xiaohei_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, DIRECT_EXCHANGE, "xiaohei");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 小紫队列监听机制
        DeliverCallback xiaoziDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaozi] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // 小黑队列监听机制
        DeliverCallback xiaoheiDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaohei] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, xiaoziDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, xiaoheiDeliverCallback, consumerTag -> {
        });
    }
}
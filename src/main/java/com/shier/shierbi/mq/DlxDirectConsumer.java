package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.shier.shierbi.constant.BiMqConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shier
 * 死信队列 - 消费者
 */
public class DlxDirectConsumer {

    private static final String DLX_DIRECT_EXCHANGE = "dlx-direct-exchange";

    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        // 指定死信队列的参数
        Map<String, Object> args = new HashMap<>();
        // 指定死信队列绑定到哪个交换机 ，此处绑定的是 dlx-direct-exchange 交换机
        args.put("x-dead-letter-exchange", DLX_DIRECT_EXCHANGE);
        // 指定死信要转发到哪个死信队列，此处转发到 laoban 这个死信队列
        args.put("x-dead-letter-routing-key", "laoban");

        // 创建队列，分配一个队列名称：小红
        String queueName = "xiaohong_queue";
        channel.queueDeclare(queueName, true, false, false, args);
        channel.queueBind(queueName, WORK_EXCHANGE_NAME, "xiaohong");

        Map<String, Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DLX_DIRECT_EXCHANGE);
        args2.put("x-dead-letter-routing-key", "waibao");

        // 创建队列，分配一个队列名称：小蓝
        String queueName2 = "xiaolan_queue";
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "xiaolan");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 小红队列监听机制
        DeliverCallback xiaohongDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [xiaohong] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // 小蓝队列监听机制
        DeliverCallback xiaolanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [xiaolan] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // 开启消费通道进行监听
        channel.basicConsume(queueName, false, xiaohongDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, xiaolanDeliverCallback, consumerTag -> {
        });
    }
}
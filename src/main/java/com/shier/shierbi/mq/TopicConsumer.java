package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.shier.shierbi.constant.BiMqConstant;

/**
 * @author Shier
 * 主题交换机 - 消费者
 */
public class TopicConsumer {

    private static final String TOPIC_EXCHANGE = "topic-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(TOPIC_EXCHANGE, "topic");

        // 创建前端队列，分配一个队列名称：frontend
        String queueName = "frontend_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, TOPIC_EXCHANGE, "#.前端.#");

        // 创建后端队列，分配一个队列名称：backend
        String queueName2 = "backend_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, TOPIC_EXCHANGE, "#.后端.#");

        // 创建后端队列，分配一个队列名称：backend
        String queueName3 = "product_queue";
        channel.queueDeclare(queueName3, true, false, false, null);
        channel.queueBind(queueName3, TOPIC_EXCHANGE, "#.产品.#");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // A队列监听机制
        DeliverCallback xiaoADeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaoA] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // B队列监听机制
        DeliverCallback xiaoBDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaoB] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // C队列监听机制
        DeliverCallback xiaoCDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaoC] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, xiaoADeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, xiaoBDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName3, true, xiaoCDeliverCallback, consumerTag -> {
        });

    }
}
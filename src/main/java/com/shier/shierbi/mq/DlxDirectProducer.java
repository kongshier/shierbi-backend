package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.shier.shierbi.constant.BiMqConstant;

import java.util.Scanner;

/**
 * @author Shier
 * 死信队列 - 生产者
 */
public class DlxDirectProducer {

    private static final String DLX_DIRECT_EXCHANGE = "dlx-direct-exchange";
    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明私信交换机
            channel.exchangeDeclare(DLX_DIRECT_EXCHANGE, "direct");

            // 创建老板的死信队列
            String queueName = "laoban_dlx_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, DLX_DIRECT_EXCHANGE, "laoban");

            // 创建外包的死信队列
            String queueName2 = "waibao_dlx_queue";
            channel.queueDeclare(queueName2, true, false, false, null);
            channel.queueBind(queueName2, DLX_DIRECT_EXCHANGE, "waibao");

            // 老板队列监听机制
            DeliverCallback laobanDeliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                // 拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                System.out.println(" [laoban] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            // 外包队列监听机制
            DeliverCallback waibaoDeliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                // 拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                System.out.println(" [waibao] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            // 开启消费通道进行监听
            channel.basicConsume(queueName, false, laobanDeliverCallback, consumerTag -> {
            });
            channel.basicConsume(queueName2, false, waibaoDeliverCallback, consumerTag -> {
            });

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                String[] splits = userInput.split(" ");
                if (splits.length < 1) {
                    continue;
                }
                String message = splits[0];
                String routingKey = splits[1];

                channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + " with routing " + routingKey + "'");
            }
        }
    }
}
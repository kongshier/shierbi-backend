package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.shier.shierbi.constant.BiMqConstant;

import java.util.Scanner;

/**
 * @author Shier
 * 直接交换机 - 生产者
 */
public class DirectProducer {

    private static final String DIRECT_EXCHANGER = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(DIRECT_EXCHANGER, "direct");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                String[] splits = userInput.split(" ");
                if (splits.length < 1) {
                    continue;
                }
                String message = splits[0];
                String routingKey = splits[1];

                channel.basicPublish(DIRECT_EXCHANGER, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + " with routing " + routingKey + "'");
            }
        }
    }
}
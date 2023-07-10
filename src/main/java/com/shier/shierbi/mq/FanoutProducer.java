package com.shier.shierbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.shier.shierbi.constant.BiMqConstant;

import java.util.Scanner;

/**
 * @author Shier
 * 广播交换机 - 生产者
 */
public class FanoutProducer {
    private static final String FANOUT_EXCHANGE_NAME = "fanout-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost(BiMqConstant.BI_MQ_HOST);
        factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
        factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 创建交换机
            channel.exchangeDeclare(FANOUT_EXCHANGE_NAME, "fanout");
            // 发送给所有的队列，所以说不要写队列名称
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                channel.basicPublish(FANOUT_EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }

        }
    }
}
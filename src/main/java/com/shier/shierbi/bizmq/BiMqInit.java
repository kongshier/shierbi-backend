package com.shier.shierbi.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.shier.shierbi.constant.BiMqConstant;

import java.util.HashMap;
import java.util.Map;

import static com.shier.shierbi.constant.BiMqConstant.*;

/**
 * @author Shier
 * CreateTime 2023/6/24 16:08
 * 用于BI项目,创建测试测序用到的交换机和队列 (仅执行一次)
 */
public class BiMqInit {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // 设置 rabbitmq 对应的信息
            factory.setHost(BiMqConstant.BI_MQ_HOST);
            factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
            factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String biExchange = BiMqConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(biExchange, BiMqConstant.BI_DIRECT_EXCHANGE);

            // 创建队列
            String queueName = BI_QUEUE;
            Map<String,Object> map = new HashMap<>();

            // 普通队列绑定死信交换机
            map.put("x-dead-letter-exchange", BI_DLX_EXCHANGE_NAME);
            map.put("x-dead-letter-routing-key", BI_DLX_ROUTING_KEY);
            channel.queueDeclare(queueName,true,false,false,map);
            channel.queueBind(queueName,BiMqConstant.BI_EXCHANGE_NAME,BI_ROUTING_KEY);

            //创建死信队列和死信交换机

            //创建死信队列
            channel.queueDeclare(BI_DLX_QUEUE_NAME,true,false,false,null);
            //创建死信交换机
            channel.exchangeDeclare(BI_DLX_EXCHANGE_NAME,BI_DIRECT_EXCHANGE);

            channel.queueBind(BI_DLX_QUEUE_NAME,BI_DLX_EXCHANGE_NAME,BI_DLX_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

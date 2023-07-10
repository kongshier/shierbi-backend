package com.shier.shierbi.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Shier
 * CreateTime 2023/6/24 16:17
 */
@SpringBootTest
class RabbitMqMessageProducerTest {

    @Resource
    private RabbitMqMessageProducer messageProducer;

    @Test
    void sendMessage() {
        messageProducer.sendMessage("demo_exchange","demo_routingKey","欢迎来到十二智能BI系统");
    }
}
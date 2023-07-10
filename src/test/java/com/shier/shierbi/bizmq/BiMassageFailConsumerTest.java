package com.shier.shierbi.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Shier
 * CreateTime 2023/7/3 17:48
 */
@SpringBootTest
class BiMassageFailConsumerTest {

    @Resource
    private BiMassageFailConsumer biMassageFailConsumer;


    @Test
    void receiveMessage() {

    }
}
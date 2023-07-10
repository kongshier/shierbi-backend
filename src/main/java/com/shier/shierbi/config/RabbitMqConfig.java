package com.shier.shierbi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.shier.shierbi.constant.BiMqConstant.*;

/**
 * @author Shier
 */
@Configuration
public class RabbitMqConfig {
    /**
     * 问答队列
     */
    @Bean
    public Queue aiQuestionQueue() {
        return new Queue(AI_QUESTION_QUEUE, true);
    }

    /**
     * 交换机
     */
    @Bean
    DirectExchange qaDirectExchange() {
        return new DirectExchange(AI_QUESTION_EXCHANGE_NAME, true, false);
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(aiQuestionQueue()).to(qaDirectExchange()).with(AI_QUESTION_ROUTING_KEY);
    }
}

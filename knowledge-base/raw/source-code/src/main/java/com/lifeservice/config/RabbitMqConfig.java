package com.lifeservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // --- 缓存广播相关配置 (原有) ---
    @Value("${app.rabbitmq.exchange:cache.fanout.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Bean
    public FanoutExchange cacheFanoutExchange() {
        return new FanoutExchange(exchangeName, true, false);
    }

    @Bean
    public Queue cacheQueue() {
        return new Queue(queueName, false, true, true);
    }

    @Bean
    public Binding bindingCacheQueue(Queue cacheQueue, FanoutExchange cacheFanoutExchange) {
        return BindingBuilder.bind(cacheQueue).to(cacheFanoutExchange);
    }

    // --- 秒杀订单异步处理配置 (新增) ---
    public static final String SECKILL_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ROUTING_KEY = "seckill.order";

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillQueue() {
        // 秒杀订单队列设为持久化
        return new Queue(SECKILL_QUEUE, true, false, false);
    }

    @Bean
    public Binding bindingSeckillQueue(Queue seckillQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with(SECKILL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

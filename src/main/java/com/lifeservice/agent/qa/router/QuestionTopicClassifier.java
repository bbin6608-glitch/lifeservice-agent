package com.lifeservice.agent.qa.router;

import org.springframework.stereotype.Component;

@Component
public class QuestionTopicClassifier {

    public enum Topic {
        SECKILL_FLOW,
        SHOP_CACHE,
        LOGIN_AUTH,
        APP_PROFILE,
        REDIS_USAGE,
        RABBITMQ_USAGE,
        CANAL_USAGE,
        LOAD_TEST,
        GENERAL
    }

    public Topic classify(String question) {
        if (question == null) return Topic.GENERAL;
        String q = question.toLowerCase();

        if (q.contains("秒杀") || q.contains("seckill") || q.contains("下单")) {
            return Topic.SECKILL_FLOW;
        }
        if (q.contains("缓存") || q.contains("cache") || q.contains("商铺") || q.contains("店铺") || q.contains("shop")) {
            return Topic.SHOP_CACHE;
        }
        if (q.contains("登录") || q.contains("login") || q.contains("auth") || q.contains("拦截器") || q.contains("token") || q.contains("验证码")) {
            return Topic.LOGIN_AUTH;
        }
        if (q.contains("app1") || q.contains("app2") || q.contains("profile") || q.contains("差异") || q.contains("区别")) {
            return Topic.APP_PROFILE;
        }
        if (q.contains("redis")) {
            return Topic.REDIS_USAGE;
        }
        if (q.contains("rabbitmq") || q.contains("mq") || q.contains("消息队列")) {
            return Topic.RABBITMQ_USAGE;
        }
        if (q.contains("canal") || q.contains("binlog")) {
            return Topic.CANAL_USAGE;
        }
        if (q.contains("压测") || q.contains("性能") || q.contains("qps") || q.contains("jmeter") || q.contains("load test")) {
            return Topic.LOAD_TEST;
        }

        return Topic.GENERAL;
    }
}

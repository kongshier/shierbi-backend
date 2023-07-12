package com.shier.shierbi.manager;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Shier
 * CreateTime 2023/5/27 23:07
 * 专门提供RedisLimiter 限流服务的
 */
@Service
@Slf4j
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     *
     * @param key 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public boolean doRateLimit(String key) {
        // 创建一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 每秒最多访问 2 次
        // 参数1 type：限流类型，可以是自定义的任何类型，用于区分不同的限流策略。
        // 参数2 rate：限流速率，即单位时间内允许通过的请求数量。
        // 参数3 rateInterval：限流时间间隔，即限流速率的计算周期长度。
        // 参数4 unit：限流时间间隔单位，可以是秒、毫秒等。
        boolean trySetRate = rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        if (trySetRate) {
            log.info("init rate = {}, interval = {}", rateLimiter.getConfig().getRate(), rateLimiter.getConfig().getRateInterval());
        }
        // 每当一个操作来了后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        return canOp;
    }


    /**
     * 限制调用次数
     *
     * @param key 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public boolean doRateLimitCount(String key) {
        // 创建一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 每秒最多访问 2 次
        // 参数1 type：限流类型，可以是自定义的任何类型，用于区分不同的限流策略。
        // 参数2 rate：限流速率，即单位时间内允许通过的请求数量。
        // 参数3 rateInterval：限流时间间隔，即限流速率的计算周期长度。
        // 参数4 unit：限流时间间隔单位，可以是秒、毫秒等。
        boolean trySetRate = rateLimiter.trySetRate(RateType.OVERALL, 5, 5, RateIntervalUnit.MINUTES);
        if (trySetRate) {
            log.info("init rate = {}, interval = {}", rateLimiter.getConfig().getRate(), rateLimiter.getConfig().getRateInterval());
        }
        // 每当一个操作来了后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        return canOp;
    }

}

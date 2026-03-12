package com.lifeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeservice.cache.CacheClient;
import com.lifeservice.dto.Result;
import com.lifeservice.entity.Shop;
import com.lifeservice.mapper.ShopMapper;
import com.lifeservice.service.IShopService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.lifeservice.cache.RedisConstants.*;

/**
 * 商铺服务实现类
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private CacheClient cacheClient;

    /**
     * 根据ID查询商铺（带缓存）
     */
    @Override
    public Result queryById(Long id) {
        Shop shop = cacheClient.queryWithLogicalExpire(
                CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        return Result.ok(shop);
    }

    /**
     * 更新商铺信息
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        updateById(shop);
        return Result.ok();
    }
}

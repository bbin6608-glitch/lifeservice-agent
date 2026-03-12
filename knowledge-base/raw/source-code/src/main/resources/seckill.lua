-- 1. 参数列表
-- 1.1. 优惠券id
local voucherId = ARGV[1]
-- 1.2. 用户id
local userId = ARGV[2]

-- 2. 数据 key
-- 2.1. 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2. 订单key (保存已抢购成功的用户id集合)
local orderKey = 'seckill:order:' .. voucherId

-- 3. 脚本业务
-- 3.1. 获取并检查库存是否在 Redis 中存在 (nil 防御)
local stockVal = redis.call('get', stockKey)
if not stockVal then
    -- 库存未初始化，返回3
    return 3
end

local stock = tonumber(stockVal)
-- 3.2. 判断库存是否充足
if (stock <= 0) then
    -- 库存不足，返回1
    return 1
end

-- 3.3. 判断用户是否下单 sismember orderKey userId
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 重复下单，返回2
    return 2
end

-- 3.4. 扣减库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)
-- 3.5. 下单（保存用户） sadd orderKey userId
redis.call('sadd', orderKey, userId)
return 0

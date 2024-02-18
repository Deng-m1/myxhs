-- 从Redis中获取用户ID
local userId = ARGV[1]


-- 用户ID存在，将其添加到列表最左侧
redis.call('LPUSH', KEYS[1], userId)

-- 判断列表长度是否大于等于200
local listSize = redis.call('LLEN', KEYS[1])
if listSize > 200 then
    -- 列表长度超过200，从右侧弹出一个元素
    redis.call('RPOP', KEYS[1])
end

    -- 返回列表当前长度
return listSize


local countKey = KEYS[1]
local followNumField = ARGV[1]

-- Check if the field exists in the hash
if redis.call('hexists', countKey, followNumField) == 1 then
    -- Retrieve the current value
    local currentValue = redis.call('hget', countKey, followNumField)

    -- Check the expiration time (TTL) of the hash key
    local ttl = redis.call('ttl', countKey)

    -- If the expiration time is less than 60 seconds, extend it by one hour (3600 seconds)
    if ttl < 60 then
        redis.call('expire', countKey, 3600)
    end

    -- Return the current value
    return currentValue
else
    -- Field does not exist in the hash
    return nil
end

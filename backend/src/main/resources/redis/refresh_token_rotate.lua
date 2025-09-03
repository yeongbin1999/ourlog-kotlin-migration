-- 입력값
-- KEYS[1]: Redis 키 "refresh:{userId}:{deviceId}"
-- ARGV[1]: 클라이언트가 제시한 refreshToken
-- ARGV[2]: 새로 저장할 refreshToken
-- ARGV[3]: 만료 시간(초) (expiration)

local storedToken = redis.call("GET", KEYS[1])

-- 기존 값이 nil이면 실패
if not storedToken then
    return -1
end

-- presentedToken이 일치하지 않으면 실패
if storedToken ~= ARGV[1] then
    return 0
end

-- 일치할 경우 새로운 refreshToken으로 덮어쓰기
local expiration = tonumber(ARGV[3])
redis.call("SETEX", KEYS[1], expiration, ARGV[2])

return 1

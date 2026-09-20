package com.echozoo.config.web.controller;

import com.echozoo.config.common.ApiException;
import com.echozoo.config.common.ApiResponse;
import com.echozoo.config.common.ErrorCode;
import com.echozoo.config.sdk.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/{key}")
    public ApiResponse<Object> getByKey(@PathVariable String key) {
        Object value = configService.get(key);
        if (value == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "配置不存在: " + key);
        }
        return ApiResponse.ok(value);
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getBatch(
            @RequestParam(required = false) String keys,
            @RequestParam(required = false) String prefix) {
        Map<String, Object> result;
        if (prefix != null && !prefix.isEmpty()) {
            result = configService.getByPrefix(prefix);
        } else if (keys != null && !keys.isEmpty()) {
            List<String> keyList = Arrays.stream(keys.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            result = configService.getBatch(keyList);
        } else {
            throw new ApiException(ErrorCode.BAD_REQUEST, "必须提供 keys 或 prefix");
        }
        return ApiResponse.ok(result);
    }
}

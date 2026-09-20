package com.echozoo.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echozoo.config.domain.ConfigItem;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.domain.ValueType;
import com.echozoo.config.mapper.ConfigItemMapper;
import com.echozoo.config.sdk.ConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ConfigService 实现。
 *
 * <p>读取优先级：
 * <ol>
 *   <li>value 非空 → 使用 value</li>
 *   <li>value 空，defaultValue 非空 → 使用 defaultValue</li>
 *   <li>value 空，defaultValue 空 → 带参重载返回传入参数；无参重载返回 null</li>
 * </ol>
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigItemMapper itemMapper;
    private final ObjectMapper objectMapper;

    public ConfigServiceImpl(ConfigItemMapper itemMapper, ObjectMapper objectMapper) {
        this.itemMapper = itemMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object get(String key) {
        ConfigItem item = findActiveItem(key);
        String raw = resolveRawValue(item);
        if (raw == null) {
            return null;
        }
        return parseValue(key, item.getValueType(), raw);
    }

    @Override
    public String getString(String key) {
        return (String) getRaw(key, ValueType.STRING, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return (String) getRaw(key, ValueType.STRING, defaultValue);
    }

    @Override
    public Integer getInt(String key) {
        return (Integer) getRaw(key, ValueType.INTEGER, null);
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        return (Integer) getRaw(key, ValueType.INTEGER, defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return (Long) getRaw(key, ValueType.LONG, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return (Long) getRaw(key, ValueType.LONG, defaultValue);
    }

    @Override
    public BigDecimal getDecimal(String key) {
        return (BigDecimal) getRaw(key, ValueType.DECIMAL, null);
    }

    @Override
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return (BigDecimal) getRaw(key, ValueType.DECIMAL, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key) {
        return (Boolean) getRaw(key, ValueType.BOOLEAN, null);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return (Boolean) getRaw(key, ValueType.BOOLEAN, defaultValue);
    }

    @Override
    public <T> T getJson(String key, Class<T> clazz) {
        ConfigItem item = findActiveItem(key);
        String raw = resolveRawValue(item);
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("配置 JSON 解析失败: " + key, e);
        }
    }

    @Override
    public Map<String, Object> getBatch(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }
        List<ConfigItem> items = itemMapper.selectList(new LambdaQueryWrapper<ConfigItem>()
                .in(ConfigItem::getKey, keys)
                .eq(ConfigItem::getStatus, ConfigStatus.ACTIVE));
        return toResultMap(items);
    }

    @Override
    public Map<String, Object> getByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Map.of();
        }
        List<ConfigItem> items = itemMapper.selectList(new LambdaQueryWrapper<ConfigItem>()
                .likeRight(ConfigItem::getKey, prefix)
                .eq(ConfigItem::getStatus, ConfigStatus.ACTIVE));
        return toResultMap(items);
    }

    private Object getRaw(String key, ValueType expectedType, Object defaultValue) {
        ConfigItem item = findActiveItem(key);
        String raw = resolveRawValue(item);
        if (raw == null) {
            return defaultValue;
        }
        return parseValue(key, item.getValueType(), raw);
    }

    private ConfigItem findActiveItem(String key) {
        ConfigItem item = itemMapper.selectOne(new LambdaQueryWrapper<ConfigItem>()
                .eq(ConfigItem::getKey, key)
                .eq(ConfigItem::getStatus, ConfigStatus.ACTIVE)
                .last("LIMIT 1"));
        return item;
    }

    private String resolveRawValue(ConfigItem item) {
        if (item == null) {
            return null;
        }
        if (item.getValue() != null && !item.getValue().isEmpty()) {
            return item.getValue();
        }
        if (item.getDefaultValue() != null && !item.getDefaultValue().isEmpty()) {
            return item.getDefaultValue();
        }
        return null;
    }

    private Map<String, Object> toResultMap(List<ConfigItem> items) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (ConfigItem item : items) {
            String raw = resolveRawValue(item);
            result.put(item.getKey(), raw == null ? null : parseValue(item.getKey(), item.getValueType(), raw));
        }
        return result;
    }

    private Object parseValue(String key, ValueType valueType, String raw) {
        try {
            return switch (valueType) {
                case STRING -> raw;
                case INTEGER -> Integer.valueOf(raw.trim());
                case LONG -> Long.valueOf(raw.trim());
                case DECIMAL -> new BigDecimal(raw.trim());
                case BOOLEAN -> parseBoolean(raw);
                case JSON -> objectMapper.readTree(raw);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("配置类型转换失败: key=" + key + ", type=" + valueType + ", value=" + raw, e);
        }
    }

    private boolean parseBoolean(String raw) {
        String s = raw.trim();
        return switch (s.toLowerCase()) {
            case "true", "1", "yes", "on" -> true;
            case "false", "0", "no", "off" -> false;
            default -> throw new IllegalArgumentException("无法解析的布尔值: " + s);
        };
    }
}

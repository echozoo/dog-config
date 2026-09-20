package com.echozoo.config.sdk;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 业务通用配置读取契约（独立 SDK，供业务系统依赖）。
 *
 * <p>读取优先级（对齐规格 docs/v0.1/api-design.md §7）：
 * <ol>
 *   <li>value 非空 → 使用 value</li>
 *   <li>value 空，defaultValue 非空 → 使用 defaultValue（DB 集中兜底）</li>
 *   <li>value 空，defaultValue 空 → 带参重载返回调用方传入参数；无参重载返回 null</li>
 * </ol>
 */
public interface ConfigService {

    /**
     * 按 valueType 解析后返回泛型值（动态类型场景，如 HTTP 层）。
     */
    Object get(String key);

    String getString(String key);

    String getString(String key, String defaultValue);

    Integer getInt(String key);

    Integer getInt(String key, Integer defaultValue);

    Long getLong(String key);

    Long getLong(String key, Long defaultValue);

    BigDecimal getDecimal(String key);

    BigDecimal getDecimal(String key, BigDecimal defaultValue);

    Boolean getBoolean(String key);

    Boolean getBoolean(String key, Boolean defaultValue);

    <T> T getJson(String key, Class<T> clazz);

    Map<String, Object> getBatch(List<String> keys);

    Map<String, Object> getByPrefix(String prefix);
}

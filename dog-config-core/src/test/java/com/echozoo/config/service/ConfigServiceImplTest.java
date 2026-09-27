package com.echozoo.config.service;

import com.echozoo.config.domain.ConfigItem;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.domain.ValueType;
import com.echozoo.config.domain.ValueTypeCodec;
import com.echozoo.config.mapper.ConfigItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ConfigServiceImpl 值解析规则测试。
 *
 * <p>读取优先级：value → defaultValue → 调用方参数 / null
 */
class ConfigServiceImplTest {

    private ConfigItemMapper itemMapper;
    private ConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        itemMapper = mock(ConfigItemMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        service = new ConfigServiceImpl(itemMapper, new ValueTypeCodec(objectMapper), objectMapper);
    }

    private void mockItem(ConfigItem item) {
        when(itemMapper.selectOne(any())).thenReturn(item);
    }

    @Test
    void valuePreferredOverDefault() {
        ConfigItem item = item("order.timeout", "60", "30", ValueType.INTEGER);
        mockItem(item);
        assertThat(service.getInt("order.timeout")).isEqualTo(60);
    }

    @Test
    void defaultValueUsedWhenValueEmpty() {
        ConfigItem item = item("order.timeout", "", "30", ValueType.INTEGER);
        mockItem(item);
        assertThat(service.getInt("order.timeout")).isEqualTo(30);
    }

    @Test
    void fallbackToCallerDefaultWhenBothEmpty() {
        ConfigItem item = item("order.timeout", "", "", ValueType.INTEGER);
        mockItem(item);
        assertThat(service.getInt("order.timeout", 999)).isEqualTo(999);
    }

    @Test
    void nullWhenBothEmptyAndNoCallerDefault() {
        ConfigItem item = item("order.timeout", "", "", ValueType.INTEGER);
        mockItem(item);
        assertThat(service.getInt("order.timeout")).isNull();
    }

    @Test
    void nullWhenItemNotFound() {
        when(itemMapper.selectOne(any())).thenReturn(null);
        assertThat(service.getString("not.exists")).isNull();
        assertThat(service.getBoolean("not.exists", true)).isTrue();
    }

    @Test
    void booleanParsing() {
        mockItem(item("order.over.sell", "false", "false", ValueType.BOOLEAN));
        assertThat(service.getBoolean("order.over.sell")).isFalse();
    }

    @Test
    void decimalParsing() {
        mockItem(item("order.rate", "3.14", "3.14", ValueType.DECIMAL));
        assertThat(service.getDecimal("order.rate")).isEqualByComparingTo(new BigDecimal("3.14"));
    }

    @Test
    void jsonParsing() {
        ConfigItem item = new ConfigItem();
        item.setKey("order.strategy");
        item.setValue("{\"name\":\"vip\"}");
        item.setValueType(ValueType.JSON);
        item.setStatus(ConfigStatus.ACTIVE);
        mockItem(item);
        Map result = service.getJson("order.strategy", Map.class);
        assertThat(result.get("name")).isEqualTo("vip");
    }

    @Test
    void typeMismatchThrows() {
        mockItem(item("order.timeout", "not-a-number", "30", ValueType.INTEGER));
        assertThatThrownBy(() -> service.getInt("order.timeout"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ConfigItem item(String key, String value, String defaultValue, ValueType type) {
        ConfigItem item = new ConfigItem();
        item.setKey(key);
        item.setValue(value);
        item.setDefaultValue(defaultValue);
        item.setValueType(type);
        item.setStatus(ConfigStatus.ACTIVE);
        return item;
    }
}

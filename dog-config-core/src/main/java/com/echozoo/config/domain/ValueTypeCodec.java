package com.echozoo.config.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 配置值编解码器：读取解析与写入校验使用同一份类型规则。
 *
 * <p>读取优先级中的类型转换、写入侧的合法性校验都经由本类，避免两侧规则漂移。
 */
@Component
public class ValueTypeCodec {

    private final ObjectMapper objectMapper;

    public ValueTypeCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 按 valueType 解析原始字符串。raw 为 null 时返回 null；无法解析抛 {@link IllegalArgumentException}。
     */
    public Object parse(ValueType valueType, String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return switch (valueType) {
                case STRING -> raw;
                case INTEGER -> Integer.valueOf(raw.trim());
                case LONG -> Long.valueOf(raw.trim());
                case DECIMAL -> new java.math.BigDecimal(raw.trim());
                case BOOLEAN -> parseBoolean(raw);
                case JSON -> objectMapper.readTree(raw);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("配置值无法按 " + valueType + " 解析: " + raw, e);
        }
    }

    /**
     * 校验值能否按 valueType 解析；空值跳过（由 required 规则单独约束）。
     */
    public void validateValue(ValueType valueType, String raw, String field) {
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }
        try {
            parse(valueType, raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " 无法按 " + valueType + " 解析: " + raw);
        }
    }

    /**
     * 校验 componentType 与 valueType 是否兼容。
     */
    public void validateComponent(ValueType valueType, ComponentType componentType) {
        boolean compatible = switch (componentType) {
            case SWITCH -> valueType == ValueType.BOOLEAN;
            case NUMBER -> valueType == ValueType.INTEGER
                    || valueType == ValueType.LONG
                    || valueType == ValueType.DECIMAL;
            case INPUT -> valueType == ValueType.STRING;
            case TEXTAREA -> valueType == ValueType.STRING || valueType == ValueType.JSON;
            case SELECT, RADIO -> valueType == ValueType.STRING;
        };
        if (!compatible) {
            throw new IllegalArgumentException(
                    "componentType " + componentType + " 与 valueType " + valueType + " 不兼容");
        }
    }

    /**
     * 校验 options：SELECT / RADIO 必须提供合法 JSON 数组；其他组件不校验。
     */
    public void validateOptions(ComponentType componentType, String options) {
        if (componentType != ComponentType.SELECT && componentType != ComponentType.RADIO) {
            return;
        }
        if (options == null || options.trim().isEmpty()) {
            throw new IllegalArgumentException("componentType " + componentType + " 必须提供 options");
        }
        try {
            JsonNode node = objectMapper.readTree(options);
            if (!node.isArray()) {
                throw new IllegalArgumentException("options 必须是 JSON 数组");
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("options 不是合法的 JSON 数组");
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

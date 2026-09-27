package com.echozoo.config.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ValueTypeCodec 类型解析与写入校验测试。
 */
class ValueTypeCodecTest {

    private final ValueTypeCodec codec = new ValueTypeCodec(new ObjectMapper());

    @Test
    void nullValueReturnsNull() {
        assertThat(codec.parse(ValueType.INTEGER, null)).isNull();
    }

    @Test
    void parseString() {
        assertThat(codec.parse(ValueType.STRING, "abc")).isEqualTo("abc");
    }

    @Test
    void parseIntegerValid() {
        assertThat(codec.parse(ValueType.INTEGER, " 30 ")).isEqualTo(30);
    }

    @Test
    void parseIntegerInvalidThrows() {
        assertThatThrownBy(() -> codec.parse(ValueType.INTEGER, "abc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseLongValid() {
        assertThat(codec.parse(ValueType.LONG, "123456789012")).isEqualTo(123456789012L);
    }

    @Test
    void parseLongInvalidThrows() {
        assertThatThrownBy(() -> codec.parse(ValueType.LONG, "1.5"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseDecimalValid() {
        assertThat(codec.parse(ValueType.DECIMAL, "3.14")).isEqualTo(new BigDecimal("3.14"));
    }

    @Test
    void parseDecimalInvalidThrows() {
        assertThatThrownBy(() -> codec.parse(ValueType.DECIMAL, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseBooleanVariants() {
        assertThat(codec.parse(ValueType.BOOLEAN, "true")).isEqualTo(true);
        assertThat(codec.parse(ValueType.BOOLEAN, "1")).isEqualTo(true);
        assertThat(codec.parse(ValueType.BOOLEAN, "off")).isEqualTo(false);
    }

    @Test
    void parseBooleanInvalidThrows() {
        assertThatThrownBy(() -> codec.parse(ValueType.BOOLEAN, "maybe"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseJsonValid() {
        assertThat(codec.parse(ValueType.JSON, "{\"name\":\"vip\"}")).isNotNull();
    }

    @Test
    void parseJsonInvalidThrows() {
        assertThatThrownBy(() -> codec.parse(ValueType.JSON, "{not-json"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateValueSkipsBlank() {
        assertThatCode(() -> codec.validateValue(ValueType.INTEGER, "", "value")).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateValue(ValueType.INTEGER, null, "value")).doesNotThrowAnyException();
    }

    @Test
    void validateValueRejectsIllegal() {
        assertThatThrownBy(() -> codec.validateValue(ValueType.INTEGER, "abc", "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
    }

    @Test
    void validateComponentAcceptsKnownPairs() {
        assertThatCode(() -> codec.validateComponent(ValueType.BOOLEAN, ComponentType.SWITCH)).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateComponent(ValueType.INTEGER, ComponentType.NUMBER)).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateComponent(ValueType.STRING, ComponentType.INPUT)).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateComponent(ValueType.JSON, ComponentType.TEXTAREA)).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateComponent(ValueType.STRING, ComponentType.SELECT)).doesNotThrowAnyException();
    }

    @Test
    void validateComponentRejectsMismatch() {
        assertThatThrownBy(() -> codec.validateComponent(ValueType.BOOLEAN, ComponentType.INPUT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> codec.validateComponent(ValueType.STRING, ComponentType.NUMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateOptionsRequiresJsonArrayForSelect() {
        assertThatCode(() -> codec.validateOptions(ComponentType.SELECT, "[{\"value\":\"SF\"}]"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> codec.validateOptions(ComponentType.SELECT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> codec.validateOptions(ComponentType.SELECT, "{\"a\":1}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> codec.validateOptions(ComponentType.SELECT, "not-json"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateOptionsIgnoresNonSelectComponents() {
        assertThatCode(() -> codec.validateOptions(ComponentType.INPUT, null)).doesNotThrowAnyException();
        assertThatCode(() -> codec.validateOptions(ComponentType.NUMBER, null)).doesNotThrowAnyException();
    }
}

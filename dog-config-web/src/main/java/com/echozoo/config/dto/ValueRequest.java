package com.echozoo.config.dto;

import jakarta.validation.constraints.NotNull;

public class ValueRequest {

    @NotNull(message = "value 不能为空")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

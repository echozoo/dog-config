package com.echozoo.config.dto;

import com.echozoo.config.domain.ConfigStatus;
import jakarta.validation.constraints.NotNull;

public class StatusRequest {

    @NotNull(message = "status 不能为空")
    private ConfigStatus status;

    public ConfigStatus getStatus() {
        return status;
    }

    public void setStatus(ConfigStatus status) {
        this.status = status;
    }
}

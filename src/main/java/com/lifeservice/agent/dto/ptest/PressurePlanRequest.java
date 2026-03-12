package com.lifeservice.agent.dto.ptest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PressurePlanRequest {
    @NotBlank(message = "接口地址不能为空")
    private String endpoint;
    private String goal;
    private Integer users = 10;
    private Integer durationSec = 60;
}

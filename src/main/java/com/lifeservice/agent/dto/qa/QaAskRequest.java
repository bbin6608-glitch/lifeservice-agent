package com.lifeservice.agent.dto.qa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QaAskRequest {
    @NotBlank(message = "问题不能为空")
    private String question;
    private Integer topK = 5;
    private Boolean includeCitations = true;
    
    /**
     * 问答模式: local, rag
     */
    private String mode = "local";
}

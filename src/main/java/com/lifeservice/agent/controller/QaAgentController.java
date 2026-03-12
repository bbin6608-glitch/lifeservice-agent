package com.lifeservice.agent.controller;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.qa.service.QaAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "智能问答")
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaAgentController {

    private final QaAgentService qaAgentService;

    @Operation(summary = "提问")
    @PostMapping("/ask")
    public QaAskResponse ask(@Valid @RequestBody QaAskRequest request) {
        return qaAgentService.ask(request);
    }
}

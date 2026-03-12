package com.lifeservice.agent.qa.service;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.dto.qa.QaAskResponse;

public interface QaAgentService {
    QaAskResponse ask(QaAskRequest request);
}

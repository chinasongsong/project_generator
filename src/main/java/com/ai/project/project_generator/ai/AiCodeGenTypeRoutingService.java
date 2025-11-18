/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai;

import com.ai.project.project_generator.model.enums.CodegenTypeEnum;

import dev.langchain4j.service.SystemMessage;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/18
 */

public interface AiCodeGenTypeRoutingService {

    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodegenTypeEnum routeCodeGenType(String userPrompt);

}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai;

import com.ai.project.project_generator.ai.model.HtmlCodeResult;
import com.ai.project.project_generator.ai.model.MultiFileCodeResult;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/22
 */

public interface AiCodeGeneratorService {

    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String prompt);

    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String prompt);

    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}

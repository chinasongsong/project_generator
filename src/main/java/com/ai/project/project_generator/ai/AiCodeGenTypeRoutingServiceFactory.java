/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/18
 */

@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTypeRoutingService getAiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class).chatModel(chatModel).build();
    }
}

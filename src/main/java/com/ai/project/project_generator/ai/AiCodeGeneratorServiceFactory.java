/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai;

import com.ai.project.project_generator.ai.tool.FileWriteTool;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/22
 */

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodegenTypeEnum codeGenType) {
        log.info("为appId: {} 创建新的AI服务实例", appId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();
        try {
            chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
            log.debug("为appId: {} 异步加载历史对话完成", appId);
        } catch (Exception e) {
            log.warn("为appId: {} 异步加载历史对话失败，将使用空上下文: {}", appId, e.getMessage());
        }

        return switch (codeGenType) {
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemory(chatMemory)
                .tools(new FileWriteTool())
                .build();
            case VUE -> AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(reasoningStreamingChatModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .tools(new FileWriteTool())
                .hallucinatedToolNameStrategy(
                    // 幻觉工具名称策略，找不到工具时配置的策略，让框架处理AI出现幻觉的情况
                    toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest,
                        "Error: there is no tool called" + toolExecutionRequest.name()))
                .build();
            default -> throw new IllegalArgumentException("不支持的代码生成类型: " + codeGenType.getValue());
        };

    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(30))
        .expireAfterAccess(Duration.ofMinutes(10))
        .removalListener((key, value, cause) -> {
            log.debug("AI服务实例被移除，appId: {}, 原因: {}", key, cause);
        })
        .build();

    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return getAiCodeGeneratorService(appId, CodegenTypeEnum.MULTI_FILE);
    }

    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodegenTypeEnum codegenTypeEnum) {
        String cacheKey = buildCacheKey(appId, codegenTypeEnum);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codegenTypeEnum));
    }

    private String buildCacheKey(Long appId, CodegenTypeEnum codegenTypeEnum) {
        return appId + ":" + codegenTypeEnum.getValue();
    }

}

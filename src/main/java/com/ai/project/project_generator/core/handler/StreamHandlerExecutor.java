/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.core.handler;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/14
 */

import com.ai.project.project_generator.model.entity.User;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.service.ChatHistoryService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Component;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 1. 传统的 Flux<String> 流（HTML、MULTI_FILE） -> SimpleTextStreamHandler
 * 2. TokenStream 格式的复杂流（VUE_PROJECT） -> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
        ChatHistoryService chatHistoryService,
        long appId, User loginUser, CodegenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE -> // 使用注入的组件实例
                jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> // 简单文本处理器不需要依赖注入
                new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}

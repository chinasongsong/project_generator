/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai;

import com.ai.project.project_generator.langgraph4j.tools.ImageSearchTool;
import com.ai.project.project_generator.langgraph4j.tools.LogoGeneratorTool;
import com.ai.project.project_generator.langgraph4j.tools.MermaidDiagramTool;
import com.ai.project.project_generator.langgraph4j.tools.UndrawIllustrationTool;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/20
 */

@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
            .chatModel(chatModel)
            .tools(imageSearchTool, undrawIllustrationTool, mermaidDiagramTool, logoGeneratorTool)
            .build();
    }
}

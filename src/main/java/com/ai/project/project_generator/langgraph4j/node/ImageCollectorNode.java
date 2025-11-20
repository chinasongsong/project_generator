/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.ai.project.project_generator.ai.ImageCollectionService;
import com.ai.project.project_generator.langgraph4j.state.ImageCategoryEnum;
import com.ai.project.project_generator.langgraph4j.state.ImageResource;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;
import com.ai.project.project_generator.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/19
 */

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);

                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败：{}", e.getMessage());
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}

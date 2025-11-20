/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.ai.project.project_generator.langgraph4j.state.ImageResource;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/19
 */

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr();
            List<ImageResource> imageList = context.getImageList();
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            if (CollUtil.isNotEmpty(imageList) || StrUtil.isNotEmpty(imageListStr)) {
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                enhancedPromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\\n");
                if (CollUtil.isNotEmpty(imageList)) {
                    for (ImageResource image : imageList) {
                        enhancedPromptBuilder.append("-")
                            .append(image.getCategory().getText())
                            .append(": ")
                            .append(image.getDescription())
                            .append("( ")
                            .append(image.getUrl())
                            .append(" ) \n");
                    }
                } else {
                    enhancedPromptBuilder.append(imageListStr);
                }
            }

            // 更新状态
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPromptBuilder.toString());
            log.info("提示词增强完成, 增强后长度：{}字符", enhancedPromptBuilder.toString().length());
            return WorkflowContext.saveContext(context);
        });
    }
}

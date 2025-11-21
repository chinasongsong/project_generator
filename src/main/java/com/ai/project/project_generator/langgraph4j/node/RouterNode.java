/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.ai.project.project_generator.ai.AiCodeGenTypeRoutingService;
import com.ai.project.project_generator.ai.AiCodeGenTypeRoutingServiceFactory;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/19
 */

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            CodegenTypeEnum generationType;
            try {
                AiCodeGenTypeRoutingServiceFactory routingServiceFactory = SpringContextUtil.getBean(
                    AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService routingService
                    = routingServiceFactory.createAiCodeGenTypeRoutingService();
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI智能路由选择完成，选择类型：{}（{}）", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型：{}", e.getMessage());
                generationType = CodegenTypeEnum.HTML;
            }
            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}

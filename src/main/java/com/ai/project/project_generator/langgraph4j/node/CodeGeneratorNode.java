/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.ai.project.project_generator.constant.AppConstant;
import com.ai.project.project_generator.core.AiCodeGeneratorFacade;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.time.Duration;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/19
 */

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");

            String userMessage = context.getOriginalPrompt();
            CodegenTypeEnum generationType = context.getGenerationType();
            Long appId = 0L;
            log.info("开始生成代码，类型：{}，{}", generationType.getValue(), generationType.getText());
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType,
                appId);
            codeStream.blockLast(Duration.ofMinutes(10));
            String generatedCodeDir = String.format("%s%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,
                generationType.getValue(), appId);
            log.info("AI代码生成完成，生成目录：{}", generatedCodeDir);
            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

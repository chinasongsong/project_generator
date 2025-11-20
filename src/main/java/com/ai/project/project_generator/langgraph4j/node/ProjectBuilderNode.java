/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.ai.project.project_generator.core.builder.VueProjectBuilder;
import com.ai.project.project_generator.exception.BusinessException;
import com.ai.project.project_generator.exception.ErrorCode;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.springframework.core.SpringVersion;

import java.io.File;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/19
 */

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            String generatedCodeDir = context.getGeneratedCodeDir();
            CodegenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;
            try {
                VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                boolean buildSuccess = vueProjectBuilder.buildProject(generatedCodeDir);

                if (buildSuccess) {
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("VUE项目构建成功，dist目录：{}", buildResultDir);
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "VUE项目构建失败");
                }

            } catch (Exception e) {
                log.error("VUE项目构建异常：{}", e.getMessage(), e);
                buildResultDir = generatedCodeDir;
            }
         
            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

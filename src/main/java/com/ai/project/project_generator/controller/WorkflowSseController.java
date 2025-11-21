/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.controller;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/21
 */

import com.ai.project.project_generator.langgraph4j.CodeGenWorkflow;
import com.ai.project.project_generator.langgraph4j.state.WorkflowContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 工作流 SSE 控制器
 * 演示 LangGraph4j 工作流的流式输出功能
 */
@RestController
@RequestMapping("/workflow")
@Slf4j
public class WorkflowSseController {

    /**
     * 同步执行工作流
     */
    @PostMapping("/execute")
    public WorkflowContext executeWorkflow(@RequestParam String prompt) {
        log.info("收到同步工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflow(prompt);
    }

    /**
     * Flux 流式执行工作流
     */
    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowWithFlux(@RequestParam String prompt) {
        log.info("收到 Flux 工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflowWithFlux(prompt);
    }

    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeWorkflowWithSseEmitter(@RequestParam String prompt) {
        log.info("收到SSE工作流执行请求：{}", prompt);
        return new CodeGenWorkflow().executeWorkflowWithSse(prompt);
    }
}

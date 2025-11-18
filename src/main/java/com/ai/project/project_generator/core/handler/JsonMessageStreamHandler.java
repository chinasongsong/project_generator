/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.core.handler;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/14
 */

import com.ai.project.project_generator.ai.model.message.AiResponseMessage;
import com.ai.project.project_generator.ai.model.message.StreamMessage;
import com.ai.project.project_generator.ai.model.message.StreamMessageTypeEnum;
import com.ai.project.project_generator.ai.model.message.ToolExecutedMessage;
import com.ai.project.project_generator.ai.model.message.ToolRequestMessage;
import com.ai.project.project_generator.model.entity.User;
import com.ai.project.project_generator.model.enums.MessageTypeEnum;
import com.ai.project.project_generator.service.ChatHistoryService;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux 原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId,
        User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux.map(chunk -> {
                // 解析每个 JSON 消息块
                return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
            }).filter(StrUtil::isNotEmpty) // 过滤空字串
            .doOnComplete(() -> {
                // 流式响应完成后，添加 AI 消息到对话历史
                String aiResponse = chatHistoryStringBuilder.toString();
                chatHistoryService.saveChatMessage(appId, aiResponse, MessageTypeEnum.AI.getValue(), loginUser.getId());
            }).doOnError(error -> {
                // 如果AI回复失败，也要记录错误消息
                String errorMessage = "AI回复失败: " + error.getMessage();
                chatHistoryService.saveChatMessage(appId, errorMessage, MessageTypeEnum.AI.getValue(),
                    loginUser.getId());
            }).onErrorResume(error -> {
                log.error("AI处理失败", error);
                String errorMessage = getErrorMessage(error);
                chatHistoryService.saveChatMessage(appId, errorMessage, MessageTypeEnum.AI.getValue(),
                    loginUser.getId());
                return Flux.just("【错误】" + errorMessage);
            });
    }

    /**
     * 解析并收集 TokenStream 数据  将Json转成对应的消息对象
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder,
        Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                // 检查是否是第一次看到这个工具 ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用这个工具，记录 ID 并完整返回工具信息
                    seenToolIds.add(toolId);
                    return "\n\n[选择工具] 写入文件\n\n";
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String relativeFilePath = jsonObject.getStr("relativePath");
                String suffix = FileUtil.getSuffix(relativeFilePath);
                String content = jsonObject.getStr("content");
                String result = String.format("""
                    [工具调用] 写入文件 %s
                    ```%s
                    %s
                    ```
                    """, relativeFilePath, suffix, content);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }

    /**
     * 根据异常类型获取友好的错误消息
     *
     * @param error 异常对象
     * @return 友好的错误消息
     */
    private String getErrorMessage(Throwable error) {
        if (error instanceof ResourceAccessException) {
            Throwable cause = error.getCause();
            if (cause instanceof SocketTimeoutException) {
                return "AI服务响应超时，请稍后重试";
            } else if (cause instanceof SocketException) {
                String message = cause.getMessage();
                if (message != null && message.contains("Unexpected end of file")) {
                    return "AI服务连接中断，可能是网络不稳定或服务异常，请稍后重试";
                }
                return "AI服务网络连接失败，请检查网络连接后重试";
            }
            return "AI服务访问失败: " + error.getMessage();
        } else if (error instanceof SocketTimeoutException) {
            return "AI服务响应超时，请稍后重试";
        } else if (error instanceof SocketException) {
            return "AI服务网络连接失败，请检查网络连接后重试";
        } else {
            return "AI服务调用失败: " + (error.getMessage() != null
                ? error.getMessage()
                : error.getClass().getSimpleName());
        }
    }
}

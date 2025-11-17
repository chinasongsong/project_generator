/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai.model.message;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/14
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 *  
 * 约定一种消息返回格式，区分不同的消息类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    private String type;
}

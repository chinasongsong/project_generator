/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.core.parser;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/23
 */

public interface CodeParser<T> {
    T parseCode(String codeContent);
}

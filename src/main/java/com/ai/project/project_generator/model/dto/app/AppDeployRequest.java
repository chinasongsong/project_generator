/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/25
 */

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}

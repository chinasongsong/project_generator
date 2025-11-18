/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/18
 */

public interface ProjectDownloadService {

    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);

}

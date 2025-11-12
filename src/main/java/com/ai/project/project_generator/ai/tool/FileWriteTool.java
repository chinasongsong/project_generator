/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai.tool;

import com.ai.project.project_generator.constant.AppConstant;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/11/12
 */

@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件相对路径") String relativePath,

        @P("写入内容") String content,

        @ToolMemoryId Long appId) throws IOException {

        try {
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativePath);
            }

            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("写入文件成功，文件路径: {}", path.toAbsolutePath());
            return "写入文件成功" + relativePath;
        } catch (IOException e) {
            String errorMsg = "写入文件失败，文件路径:" + relativePath + ",错误信息：" + e.getMessage();
            log.error(errorMsg);
            return errorMsg;
        }

    }

}

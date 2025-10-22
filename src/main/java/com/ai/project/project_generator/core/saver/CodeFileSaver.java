/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.core.saver;

import com.ai.project.project_generator.ai.model.HtmlCodeResult;
import com.ai.project.project_generator.ai.model.MultiFileCodeResult;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/22
 */

public class CodeFileSaver {
    private static final String FILE_SAVE_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDir = buildUniqueDirectoryName(CodegenTypeEnum.HTML.getValue());
        saveFileToPresetDirectory(baseDir, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDir);
    }

    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCode) {
        String baseDir = buildUniqueDirectoryName(CodegenTypeEnum.MULTI_FILE.getValue());
        saveFileToPresetDirectory(baseDir, "index.html", multiFileCode.getHtmlCode());
        saveFileToPresetDirectory(baseDir, "style.css", multiFileCode.getCcsCode());
        saveFileToPresetDirectory(baseDir, "script.js", multiFileCode.getJsCode());
        return new File(baseDir);
    }

    private static String buildUniqueDirectoryName(String bizType) {
        String uniqueName = StrUtil.format("{}-{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String directoryName = FILE_SAVE_PATH + File.separator + uniqueName;
        FileUtil.mkdir(directoryName);
        return directoryName;
    }

    private static void saveFileToPresetDirectory(String dirPath, String fileName, String content) {

        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}

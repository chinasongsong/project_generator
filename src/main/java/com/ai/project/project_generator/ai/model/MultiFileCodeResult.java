/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.ai.project.project_generator.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author Feng Zengsong
 * @version 0.0.1
 * @since 2025/10/22
 */

@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("css代码")
    private String ccsCode;

    @Description("js代码")
    private String jsCode;

    @Description("描述")
    private String description;

}

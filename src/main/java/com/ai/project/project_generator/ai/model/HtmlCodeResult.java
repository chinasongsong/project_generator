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

@Description("生成HTML代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;

}

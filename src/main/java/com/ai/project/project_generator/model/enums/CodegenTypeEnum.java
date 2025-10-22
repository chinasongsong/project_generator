package com.ai.project.project_generator.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @Author: f00804724
 * @Date: 2025/10/22
 * @Description:
 */

@Getter
public enum CodegenTypeEnum {

    HTML("html", "HTML"),
    MULTI_FILE("multi_file", "多文件");

    private final String text;

    private final String value;

    CodegenTypeEnum(String text, String value) {

        this.text = text;
        this.value = value;
    }

    public static CodegenTypeEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {

            return null;
        }

        for (CodegenTypeEnum anEum : CodegenTypeEnum.values()) {
            if (anEum.value.equals(value)) {
                return anEum;
            }
        }
        return null;
    }
}

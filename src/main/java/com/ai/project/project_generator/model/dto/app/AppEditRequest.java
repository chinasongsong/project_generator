package com.ai.project.project_generator.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * 应用封面
     */
    private String cover;
    
    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;
    
    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;
    
    /**
     * 部署标识
     */
    private String deployKey;
    
    /**
     * 优先级
     */
    private Integer priority;
}


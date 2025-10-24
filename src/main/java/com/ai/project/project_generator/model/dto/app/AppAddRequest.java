package com.ai.project.project_generator.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;
}

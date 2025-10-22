package com.ai.project.project_generator.core;

import static org.junit.jupiter.api.Assertions.*;

import com.ai.project.project_generator.ai.AiCodeGeneratorService;
import com.ai.project.project_generator.ai.AiCodeGeneratorServiceFactory;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * @Author: f00804724
 * @Date: 2025/10/22
 * @Description:
 */

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    
    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("请做一个计算器", CodegenTypeEnum.HTML);
        assertNotNull(file);
    }
}
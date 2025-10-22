package com.ai.project.project_generator.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.ai.project.project_generator.ai.model.HtmlCodeResult;
import com.ai.project.project_generator.ai.model.MultiFileCodeResult;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: f00804724
 * @Date: 2025/10/22
 * @Description:
 */

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {

        String prompt = "请做一个计算器";
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode(prompt);
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        String prompt = "请做一个记账本";
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode(prompt);
        Assertions.assertNotNull(multiFileCode);
    }
}
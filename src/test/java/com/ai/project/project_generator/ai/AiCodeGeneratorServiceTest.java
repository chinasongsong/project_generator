package com.ai.project.project_generator.ai;

import com.ai.project.project_generator.ai.model.HtmlCodeResult;
import com.ai.project.project_generator.ai.model.MultiFileCodeResult;
import com.ai.project.project_generator.core.AiCodeGeneratorFacade;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

/**
 * @Author: f00804724
 * @Date: 2025/10/22
 * @Description:
 */

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

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


    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("任务记录网站", CodegenTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodegenTypeEnum.MULTI_FILE, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


}
package com.ai.project.project_generator.langgraph4j.tools;

import cn.hutool.core.collection.CollUtil;
import com.ai.project.project_generator.langgraph4j.state.ImageCategoryEnum;
import com.ai.project.project_generator.langgraph4j.state.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UndrawIllustrationToolTest {

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Test
    void testSearchIllustrations() {
        // 测试正常搜索插画
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("happy");
        assertNotNull(illustrations);
        // 验证返回的插画资源
        if (CollUtil.isNotEmpty(illustrations)) {
            ImageResource firstIllustration = illustrations.get(0);
            assertEquals(ImageCategoryEnum.ILLUSTRATION, firstIllustration.getCategory());
            assertNotNull(firstIllustration.getDescription());
            assertNotNull(firstIllustration.getUrl());
            assertTrue(firstIllustration.getUrl().startsWith("http"));
            System.out.println("搜索到 " + illustrations.size() + " 张插画");
            illustrations.forEach(illustration ->
                    System.out.println("插画: " + illustration.getDescription() + " - " + illustration.getUrl())
            );
        } else {
            System.out.println("未找到任何插画");
        }

    }
}

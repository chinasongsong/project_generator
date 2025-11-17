package com.ai.project.project_generator.controller;

import com.ai.project.project_generator.common.BaseResponse;
import com.ai.project.project_generator.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthyController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        System.out.println("health check");
        return ResultUtils.success("ok");
    }
}

package com.ai.project.project_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)

public class ProjectGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectGeneratorApplication.class, args);
    }

}

package com.ai.project.project_generator.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.project.project_generator.model.entity.App;
import com.ai.project.project_generator.mapper.AppMapper;
import com.ai.project.project_generator.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}

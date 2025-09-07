package com.ai.project.project_generator.service;

import com.ai.project.project_generator.model.entity.User;
import com.mybatisflex.core.service.IService;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);
}

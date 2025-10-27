package com.ai.project.project_generator.service;

import com.ai.project.project_generator.model.dto.app.AppQueryRequest;
import com.ai.project.project_generator.model.entity.App;
import com.ai.project.project_generator.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用
     *
     * @param app 应用
     */
    void validApp(App app);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装
     *
     * @param app     应用
     * @param request 请求
     * @return 应用封装
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取应用封装
     *
     * @param appList 应用列表
     * @param request 请求
     * @return 应用封装列表
     */
    List<AppVO> getAppVOList(List<App> appList, HttpServletRequest request);

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 精选应用列表
     */
    List<AppVO> getFeaturedAppVOList(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 精选应用分页列表
     */
    Page<AppVO> getFeaturedAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用列表
     */
    Page<AppVO> getMyAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用列表
     */
    List<AppVO> getAdminAppVOList(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用分页列表
     */
    Page<AppVO> getAdminAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 根据 id 获取应用详情（用户端）
     *
     * @param id      应用 id
     * @param request 请求
     * @return 应用详情
     */
    AppVO getAppVOById(Long id, HttpServletRequest request);

    /**
     * 根据 id 获取应用详情（管理员）
     *
     * @param id      应用 id
     * @return 应用详情
     */
    App getAppById(Long id);

    /**
     * 创建应用
     *
     * @param app     应用
     * @param request 请求
     * @return 新应用 id
     */
    Long addApp(App app, HttpServletRequest request);

    /**
     * 更新应用（用户端）
     *
     * @param app     应用
     * @param request 请求
     * @return 更新结果
     */
    Boolean updateApp(App app, HttpServletRequest request);

    /**
     * 更新应用（管理员）
     *
     * @param app     应用
     * @param request 请求
     * @return 更新结果
     */
    Boolean editAppByAdmin(App app, HttpServletRequest request);

    /**
     * 删除应用（用户端）
     *
     * @param id      应用 id
     * @param request 请求
     * @return 删除结果
     */
    Boolean deleteApp(Long id, HttpServletRequest request);

    /**
     * 删除应用（管理员）
     *
     * @param id      应用 id
     * @param request 请求
     * @return 删除结果
     */
    Boolean deleteAppByAdmin(Long id, HttpServletRequest request);


    /**
     * 生成应用
     */
    Flux<String> charToGenerateApp(Long appId, String userMessage, HttpServletRequest request);

    
    String deployApp(Long appId, HttpServletRequest request);
}
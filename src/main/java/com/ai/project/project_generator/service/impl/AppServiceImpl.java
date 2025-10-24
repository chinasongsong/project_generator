package com.ai.project.project_generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.project.project_generator.constant.AppConstant;
import com.ai.project.project_generator.core.AiCodeGeneratorFacade;
import com.ai.project.project_generator.exception.BusinessException;
import com.ai.project.project_generator.exception.ErrorCode;
import com.ai.project.project_generator.exception.ThrowUtils;
import com.ai.project.project_generator.mapper.AppMapper;
import com.ai.project.project_generator.model.dto.app.AppQueryRequest;
import com.ai.project.project_generator.model.entity.App;
import com.ai.project.project_generator.model.entity.User;
import com.ai.project.project_generator.model.enums.CodegenTypeEnum;
import com.ai.project.project_generator.model.vo.AppVO;
import com.ai.project.project_generator.model.vo.UserVO;
import com.ai.project.project_generator.service.AppService;
import com.ai.project.project_generator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    @Override
    public void validApp(App app) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList, HttpServletRequest request) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<AppVO> appVOList = appList.stream().map(app -> {
            AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
            Long userId = app.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
            return appVO;
        }).collect(Collectors.toList());
        return appVOList;
    }

    @Override
    public List<AppVO> getFeaturedAppVOList(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        // 精选应用：优先级为精选应用优先级的应用
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        queryWrapper.eq("priority", AppConstant.FEATURED_APP_PRIORITY);
        List<App> appList = this.list(queryWrapper);
        return getAppVOList(appList, request);
    }

    @Override
    public Page<AppVO> getFeaturedAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        long pageSize = appQueryRequest.getPageSize();
        long pageNum = appQueryRequest.getPageNum();
        appQueryRequest.setPriority(AppConstant.FEATURED_APP_PRIORITY);
        // 精选应用：优先级为精选应用优先级的应用
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        Page<App> appPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords(), request);

        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public Page<AppVO> getMyAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long pageSize = appQueryRequest.getPageSize();
        long pageNum = appQueryRequest.getPageNum();

        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        Page<App> appPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords(), request);

        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public List<AppVO> getAdminAppVOList(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        List<App> appList = this.list(queryWrapper);
        return getAppVOList(appList, request);
    }

    @Override
    public Page<AppVO> getAdminAppVOPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        long pageSize = appQueryRequest.getPageSize();
        long pageNum = appQueryRequest.getPageNum();

        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        Page<App> appPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords(), request);

        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public AppVO getAppVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return getAppVO(app, request);
    }

    @Override
    public App getAppById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return app;
    }

    @Override
    public Long addApp(App app, HttpServletRequest request) {

        String initPrompt = app.getInitPrompt();
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化的 prompt 不能为空");
        }

        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化的 prompt 过长");
        }
        // 填充默认值
        User loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getId());

        if (StrUtil.isNotBlank(initPrompt)) {
            String appName = initPrompt.length() > 12 ? initPrompt.substring(0, 12) : initPrompt;
            app.setAppName(appName);
        }

        // 设置生成类型为多文件生成
        app.setCodeGenType(CodegenTypeEnum.MULTI_FILE.getValue());

        // 设置默认优先级
        if (app.getPriority() == null) {
            app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        }

        // 写入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public Boolean updateApp(App app, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        long id = app.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可编辑
        if (!oldApp.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 只允许更新应用名称
        App updateApp = new App();
        updateApp.setId(id);
        updateApp.setAppName(app.getAppName());
        // 设置编辑时间为当前时间
        updateApp.setEditTime(LocalDateTime.now());

        // 数据校验
        validApp(updateApp);
        return this.updateById(updateApp);
    }

    @Override
    public Boolean editAppByAdmin(App app, HttpServletRequest request) {
        // 数据校验
        validApp(app);
        app.setEditTime(LocalDateTime.now());
        long id = app.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        return this.updateById(app);
    }

    @Override
    public Boolean deleteApp(Long id, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public Boolean deleteAppByAdmin(Long id, HttpServletRequest request) {
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        return this.removeById(id);
    }

    @Override
    public Flux<String> charToGenerateApp(Long appId, String userMessage, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(userMessage == null || userMessage.length() == 0, ErrorCode.PARAMS_ERROR, "用户输入不能为空");
        App app = this.getAppById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        User loinUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loinUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }

        String codeGenType = app.getCodeGenType();
        CodegenTypeEnum enumByValue = CodegenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenType);

        return aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, enumByValue, appId);
    }
}
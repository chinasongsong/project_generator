package com.ai.project.project_generator.controller;

import com.ai.project.project_generator.annotation.AuthCheck;
import com.ai.project.project_generator.common.BaseResponse;
import com.ai.project.project_generator.common.DeleteRequest;
import com.ai.project.project_generator.common.ResultUtils;
import com.ai.project.project_generator.constant.UserConstant;
import com.ai.project.project_generator.exception.BusinessException;
import com.ai.project.project_generator.exception.ErrorCode;
import com.ai.project.project_generator.exception.ThrowUtils;
import com.ai.project.project_generator.model.dto.app.AppAddRequest;
import com.ai.project.project_generator.model.dto.app.AppDeployRequest;
import com.ai.project.project_generator.model.dto.app.AppEditRequest;
import com.ai.project.project_generator.model.dto.app.AppQueryRequest;
import com.ai.project.project_generator.model.dto.app.AppUpdateRequest;
import com.ai.project.project_generator.model.entity.App;
import com.ai.project.project_generator.model.vo.AppVO;
import com.ai.project.project_generator.service.AppService;
import com.mybatisflex.core.paginate.Page;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    // region 用户端接口

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> charToGenerateApp(@RequestParam Long appId, @RequestParam String userMessage,
        HttpServletRequest request) {
        Flux<String> stringFlux = appService.charToGenerateApp(appId, userMessage, request);
        Flux<ServerSentEvent<String>> dataFlux = stringFlux.map(chunk -> {
            Map<String, String> wrapper = Map.of("d", chunk);
            String jsonStr = JSONUtil.toJsonStr(wrapper);
            return ServerSentEvent.<String>builder().data(jsonStr).build();
        }).onErrorResume(error -> {
            // 将错误转换为 SSE 格式返回
            log.error("流式生成代码时发生错误", error);
            String errorMessage = getErrorMessage(error);
            Map<String, String> errorWrapper = Map.of("error", errorMessage);
            String errorJsonStr = JSONUtil.toJsonStr(errorWrapper);
            return Flux.just(ServerSentEvent.<String>builder().event("error").data(errorJsonStr).build());
        });

        // 在正常完成或错误后都发送 done 事件
        return dataFlux.concatWith(Mono.just(ServerSentEvent.<String>builder().event("done").data("").build()))
            .onErrorResume(error -> {
                // 如果发送完成事件时也失败，确保返回错误事件
                log.error("发送完成事件时发生错误", error);
                Map<String, String> errorWrapper = Map.of("error", "系统错误: " + error.getMessage());
                String errorJsonStr = JSONUtil.toJsonStr(errorWrapper);
                return Flux.just(ServerSentEvent.<String>builder().event("error").data(errorJsonStr).build());
            });
    }

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request 请求
     * @return 新应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null || StrUtil.isBlank(appAddRequest.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化的 prompt 不能为空");
        }
        // 在此处将实体类和 DTO 进行转换
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        Long newAppId = appService.addApp(app, request);
        return ResultUtils.success(newAppId);
    }

    /**
     * 删除应用
     *
     * @param deleteRequest 删除请求
     * @param request 请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = appService.deleteApp(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（仅本人）
     *
     * @param appUpdateRequest 更新应用请求
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        App app = new App();
        BeanUtil.copyProperties(appUpdateRequest, app);
        Boolean result = appService.updateApp(app, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取应用（封装类）
     *
     * @param id 应用 id
     * @param request 请求
     * @return 应用封装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        AppVO appVO = appService.getAppVOById(id, request);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request 请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
        HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long size = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AppVO> myAppVOPage = appService.getMyAppVOPage(appQueryRequest, request);
        return ResultUtils.success(myAppVOPage);
    }

    // endregion

    // region 管理员接口

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request 请求
     * @return 精选应用列表
     */
    @PostMapping("/list/page/vo/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
        HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long size = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AppVO> appVOPage = appService.getFeaturedAppVOPage(appQueryRequest, request);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 根据 id 获取应用（仅管理员）
     *
     * @param id 应用 id
     * @param request 请求
     * @return 应用封装类
     */
    @GetMapping("/get")
    public BaseResponse<AppVO> getAppById(long id, HttpServletRequest request) {
        AppVO appVO = appService.getAppVOById(id, request);
        return ResultUtils.success(appVO);
    }

    /**
     * 删除应用（仅管理员）
     *
     * @param deleteRequest 删除请求
     * @param request 请求
     * @return 删除结果
     */
    @PostMapping("/delete/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest,
        HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = appService.deleteAppByAdmin(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（仅管理员）
     *
     * @param appEditRequest 更新应用请求
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editAppByAdmin(@RequestBody AppEditRequest appEditRequest,
        HttpServletRequest request) {
        if (appEditRequest == null || appEditRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        App app = new App();
        BeanUtil.copyProperties(appEditRequest, app);
        Boolean result = appService.editAppByAdmin(app, request);
        return ResultUtils.success(result);
    }

    // endregion

    /**
     * 分页获取应用列表（仅管理员）
     *
     * @param appQueryRequest 查询请求
     * @param request 请求
     * @return 应用列表
     */
    @PostMapping("/list/page/vo/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageAdmin(@RequestBody AppQueryRequest appQueryRequest,
        HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<AppVO> appVOPage = appService.getAdminAppVOPage(appQueryRequest, request);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request 请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, request);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 根据异常类型获取友好的错误消息
     *
     * @param error 异常对象
     * @return 友好的错误消息
     */
    private String getErrorMessage(Throwable error) {
        if (error instanceof ResourceAccessException) {
            Throwable cause = error.getCause();
            if (cause instanceof SocketTimeoutException) {
                return "AI服务响应超时，请稍后重试";
            } else if (cause instanceof SocketException) {
                String message = cause.getMessage();
                if (message != null && message.contains("Unexpected end of file")) {
                    return "AI服务连接中断，可能是网络不稳定或服务异常，请稍后重试";
                }
                return "AI服务网络连接失败，请检查网络连接后重试";
            }
            return "AI服务访问失败: " + error.getMessage();
        } else if (error instanceof SocketTimeoutException) {
            return "AI服务响应超时，请稍后重试";
        } else if (error instanceof SocketException) {
            return "AI服务网络连接失败，请检查网络连接后重试";
        } else {
            return "AI服务调用失败: " + (error.getMessage() != null
                ? error.getMessage()
                : error.getClass().getSimpleName());
        }
    }

}
package com.ai.project.project_generator.controller;

import com.ai.project.project_generator.annotation.AuthCheck;
import com.ai.project.project_generator.common.BaseResponse;
import com.ai.project.project_generator.common.ResultUtils;
import com.ai.project.project_generator.constant.UserConstant;
import com.ai.project.project_generator.exception.BusinessException;
import com.ai.project.project_generator.exception.ErrorCode;
import com.ai.project.project_generator.exception.ThrowUtils;
import com.ai.project.project_generator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ai.project.project_generator.model.vo.ChatHistoryVO;
import com.ai.project.project_generator.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 根据应用id分页查询对话历史（基于游标的分页查询）
     *
     * @param appId 应用id
     * @param cursorTime 游标时间（可选，如果提供则查询创建时间小于该时间的消息，否则查询最新消息）
     * @param pageSize 每页大小（默认10）
     * @param request 请求
     * @return 对话历史列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<ChatHistoryVO>> getMessages(@RequestParam Long appId,
        @RequestParam(required = false) LocalDateTime cursorTime, @RequestParam(required = false) Integer pageSize,
        HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        Page<ChatHistoryVO> messagesByAppId = chatHistoryService.getMessagesByAppId(appId, cursorTime, pageSize,
            request);
        return ResultUtils.success(messagesByAppId);
    }

    // endregion

    // region 管理员接口

    /**
     * 管理员查看所有应用的对话历史（分页）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @param request 请求
     * @return 对话历史分页列表
     */
    @PostMapping("/list/page/vo/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryVOByPageAdmin(
        @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.getAdminChatHistoryVOPage(chatHistoryQueryRequest,
            request);
        return ResultUtils.success(chatHistoryVOPage);
    }

    // endregion
}


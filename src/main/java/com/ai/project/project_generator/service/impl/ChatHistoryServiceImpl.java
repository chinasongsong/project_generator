package com.ai.project.project_generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.ai.project.project_generator.exception.BusinessException;
import com.ai.project.project_generator.exception.ErrorCode;
import com.ai.project.project_generator.exception.ThrowUtils;
import com.ai.project.project_generator.mapper.ChatHistoryMapper;
import com.ai.project.project_generator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ai.project.project_generator.model.entity.App;
import com.ai.project.project_generator.model.entity.ChatHistory;
import com.ai.project.project_generator.model.entity.User;
import com.ai.project.project_generator.model.enums.MessageTypeEnum;
import com.ai.project.project_generator.model.vo.ChatHistoryVO;
import com.ai.project.project_generator.service.AppService;
import com.ai.project.project_generator.service.ChatHistoryService;
import com.ai.project.project_generator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public Boolean saveChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
            .appId(appId)
            .message(message)
            .messageType(messageType)
            .userId(userId)
            .build();
        return this.save(chatHistory);
    }

    @Override
    public Page<ChatHistoryVO> getMessagesByAppId(Long appId, LocalDateTime cursorTime, Integer pageSize,
        HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 验证应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 权限校验：仅应用创建者和管理员可见
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        // 设置默认分页大小
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        // 限制每页最大数量
        if (pageSize > 50) {
            pageSize = 50;
        }

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);

        // 如果有游标时间，则查询创建时间小于该时间的消息；否则查询最新消息
        if (cursorTime != null) {
            queryWrapper.lt("createTime", cursorTime);
        }

        // 按创建时间降序排列
        queryWrapper.orderBy("createTime", false).limit(pageSize);

        Page<ChatHistory> page = this.page(Page.of(1, pageSize), queryWrapper);
        List<ChatHistoryVO> chatHistoryVOList = getChatHistoryVOList(page.getRecords());
        return new Page<>(chatHistoryVOList, page.getPageNumber(), page.getPageSize(), page.getTotalRow());
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = chatHistoryQueryRequest.getId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        Long parentId = chatHistoryQueryRequest.getParentId();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();

        QueryWrapper queryWrapper = QueryWrapper.create()
            .eq("id", id)
            .eq("messageType", messageType)
            .eq("appId", appId)
            .eq("userId", userId)
            .eq("parentId", parentId);

        if (null != lastCreateTime) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 默认按创建时间降序
        if (StrUtil.isBlank(sortField)) {
            sortField = "createTime";
        }
        if (StrUtil.isBlank(sortOrder)) {
            sortOrder = "descend";
        }
        queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));

        return queryWrapper;
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = BeanUtil.copyProperties(chatHistory, ChatHistoryVO.class);
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream().map(this::getChatHistoryVO).collect(Collectors.toList());
    }

    @Override
    public Page<ChatHistoryVO> getAdminChatHistoryVOPage(ChatHistoryQueryRequest chatHistoryQueryRequest,
        HttpServletRequest request) {
        long pageSize = chatHistoryQueryRequest.getPageSize();
        long pageNum = chatHistoryQueryRequest.getPageNum();

        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);

        Page<ChatHistory> chatHistoryPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, chatHistoryPage.getTotalRow());
        List<ChatHistoryVO> chatHistoryVOList = this.getChatHistoryVOList(chatHistoryPage.getRecords());

        chatHistoryVOPage.setRecords(chatHistoryVOList);

        return chatHistoryVOPage;
    }

    @Override
    public Boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);

        return this.remove(queryWrapper);
    }
}


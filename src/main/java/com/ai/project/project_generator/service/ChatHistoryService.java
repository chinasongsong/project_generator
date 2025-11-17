package com.ai.project.project_generator.service;

import com.ai.project.project_generator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ai.project.project_generator.model.entity.ChatHistory;
import com.ai.project.project_generator.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 统一的保存接口
     *
     * @param appId 应用id
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 用户id
     * @return 保存成功
     */
    Boolean saveChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用id分页查询对话历史（基于游标的分页查询）
     *
     * @param appId 应用id
     * @param cursorTime 游标时间（可选，如果提供则查询创建时间小于该时间的消息，否则查询最新消息）
     * @param pageSize 每页大小（默认10）
     * @param request 请求
     * @return 对话历史列表
     */
    Page<ChatHistoryVO> getMessagesByAppId(Long appId, LocalDateTime cursorTime, Integer pageSize,
        HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 获取对话历史封装
     *
     * @param chatHistory 对话历史
     * @return 对话历史封装
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取对话历史封装列表
     *
     * @param chatHistoryList 对话历史列表
     * @return 对话历史封装列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);

    /**
     * 管理员查看所有应用的对话历史（分页）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @param request 请求
     * @return 对话历史分页列表
     */
    Page<ChatHistoryVO> getAdminChatHistoryVOPage(ChatHistoryQueryRequest chatHistoryQueryRequest,
        HttpServletRequest request);

    /**
     * 删除应用的所有对话历史
     *
     * @param appId 应用id
     * @return 删除结果
     */
    Boolean deleteByAppId(Long appId);
    
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}


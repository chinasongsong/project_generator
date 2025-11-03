package com.ai.project.project_generator.model.dto.chatHistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加对话历史请求。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 父消息id（用于上下文关联）
     */
    private Long parentId;
}

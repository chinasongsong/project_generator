package com.ai.project.project_generator.model.dto.chatHistory;

import com.ai.project.project_generator.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询对话历史请求。
 *
 * @author <a href="https://github.com/chinasongsong">fzs</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 父消息id
     */
    private Long parentId;

    /**
     * 游标id（用于分页加载历史记录，查询创建时间小于该id的消息）
     */
    private LocalDateTime lastCreateTime;
}

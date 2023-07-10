package com.shier.shierbi.model.dto.aiassistant;

import com.baomidou.mybatisplus.annotation.TableField;
import com.shier.shierbi.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 问答助手信息表
 * @author Shier
 * @TableName ai_assistant
 */

@Data
public class AiAssistantQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 问题名称
     */
    private String questionName;

    /**
     * 问题概述
     */
    private String questionGoal;

    /**
     * 问答结果
     */
    private String questionResult;

    /**
     * 问题类型
     */
    private String questionType;

    /**
     * wait-等待,running-生成中,succeed-成功生成,failed-生成失败
     */
    private String questionStatus;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 创建用户 id
     */
    private Long userId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
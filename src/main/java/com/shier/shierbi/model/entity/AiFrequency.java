package com.shier.shierbi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ai调用次数表
 * @author Shier
 * @TableName ai_frequency
 */
@TableName(value ="ai_frequency")
@Data
public class AiFrequency implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 总调用次数
     */
    private Integer totalFrequency;

    /**
     * 剩余调用次数
     */
    private Integer remainFrequency;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
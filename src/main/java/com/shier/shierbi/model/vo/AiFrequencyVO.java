package com.shier.shierbi.model.vo;

import lombok.Data;

/**
 * @author Shier
 * @createTime 2023/7/11 星期二 23:07
 */
@Data
public class AiFrequencyVO {

    /**
     * id
     */
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
}

package com.shier.shierbi.model.vo;

import lombok.Data;

/**
 * @author Shier
 * CreateTime 2023/5/21 12:41
 * BI 返回结果
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    /**
     * 新生成的ID
     */
    private Long chartId;
}

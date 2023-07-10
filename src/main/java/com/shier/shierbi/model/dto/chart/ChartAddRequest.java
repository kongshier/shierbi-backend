package com.shier.shierbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建图表请求
 *
 * @author Shier
 */
@Data
public class ChartAddRequest implements Serializable {

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表信息
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
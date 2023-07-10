package com.shier.shierbi.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author Shier
 * CreateTime 2023/5/21 17:38
 */
public interface ChartConstant {
    /**
     * AI生成的内容分隔符
     */
    String GEN_CONTENT_SPLITS = "【【【【【";

    /**
     * AI 生成的内容的元素为3个
     */
    int GEN_ITEM_NUM = 3;

    /**
     * 生成图表的数据下标
     */
    int GEN_CHART_IDX = 1;

    /**
     * 生成图表的分析结果的下标
     */
    int GEN_RESULT_IDX = 2;

    /**
     * 提取生成的图表的Echarts配置的正则
     */
    String GEN_CHART_REGEX = "\\{(?>[^{}]*(?:\\{[^{}]*}[^{}]*)*)}";
    /**
     * 图表默认名称的前缀
     */
    String DEFAULT_CHART_NAME_PREFIX = "分析图表_";

    /**
     * 图表默认名称的后缀长度
     */
    int DEFAULT_CHART_NAME_SUFFIX_LEN = 10;

    /**
     * 图表上传文件大小 1M
     */
    long FILE_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    List<String>  VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");

    /**
     * 用户头像上传文件大小 2M
     */
    long USER_FILE_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    List<String>  USER_VALID_FILE_SUFFIX= Arrays.asList("png","jpg","jpeg");
}

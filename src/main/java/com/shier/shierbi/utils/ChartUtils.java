package com.shier.shierbi.utils;

import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.exception.ThrowUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shier.shierbi.constant.ChartConstant.*;

/**
 * @author Shier
 * CreateTime 2023/5/21 17:41
 * 图表工具类
 */
public class ChartUtils {
    private static final Pattern VALID_GEN_CHART_PATTERN = Pattern.compile(GEN_CHART_REGEX, Pattern.COMMENTS);

    /**
     * 依照正则表达式来匹配合法的图表的echarts配置
     *
     * @param preGenChart 提取前的原数据
     * @return 提取后的json串
     */
    public static String getValidGenChart(String preGenChart) {
        Matcher matcher = VALID_GEN_CHART_PATTERN.matcher(preGenChart);
        ThrowUtils.throwIf(!matcher.find(), ErrorCode.SYSTEM_ERROR, "AI生成图表错误");
        return matcher.group();
    }

    /**
     * 如果用户没有传图表的名称，生成默认的
     *
     * @return 默认的图表名称
     */
    public static String genDefaultChartName() {
        return DEFAULT_CHART_NAME_PREFIX + RandomStringUtils.randomAlphabetic(DEFAULT_CHART_NAME_SUFFIX_LEN);
    }
}

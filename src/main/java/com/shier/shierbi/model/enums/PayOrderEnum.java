package com.shier.shierbi.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图表状态枚举类
 *
 * @author Shier
 */
public enum PayOrderEnum {

    WAIT_PAY("待付款", "0"),
    COMPLETE("已完成", "1"),
    TIMEOUT_ORDER("超时订单", "2"),
    CANCEL_ORDER("取消订单", "3");
    private final String text;

    private final String value;

    PayOrderEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static PayOrderEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (PayOrderEnum anEnum : PayOrderEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}

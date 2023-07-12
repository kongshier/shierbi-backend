package com.shier.shierbi.model.dto.frequency;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Shier
 * 使用次数
 */
@Data
public class FrequencyRequest implements Serializable {
    private int frequency;
}

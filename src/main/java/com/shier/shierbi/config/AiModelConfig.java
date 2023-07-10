package com.shier.shierbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Shier
 * CreateTime 2023/5/21 18:27
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.model")
public class AiModelConfig {
    /**
     * 模型id
     */
    private Long modelId;
}

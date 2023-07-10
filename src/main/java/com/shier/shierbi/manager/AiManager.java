package com.shier.shierbi.manager;

import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.config.AiModelConfig;
import com.shier.shierbi.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Shier
 * CreateTime 2023/5/21 10:42
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient congMingClient;

    @Resource
    private AiModelConfig aiModelConfig;

    /**
     * AI 对话
     *
     * @param message 消息
     * @return
     */
    public String doChat(String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        // 鱼聪明平台模型ID
        devChatRequest.setModelId(aiModelConfig.getModelId());
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = congMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }

    public String doAiChat(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = congMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }
}

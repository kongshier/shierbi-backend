package com.shier.shierbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.exception.ThrowUtils;
import com.shier.shierbi.mapper.AiFrequencyMapper;
import com.shier.shierbi.model.entity.AiFrequency;
import com.shier.shierbi.service.AiFrequencyService;
import org.springframework.stereotype.Service;

/**
 * @author Shier
 * @description 针对表【ai_frequency(ai调用次数表)】的数据库操作Service实现
 * @createDate 2023-07-11 20:47:00
 */
@Service
public class AiFrequencyServiceImpl extends ServiceImpl<AiFrequencyMapper, AiFrequency>
        implements AiFrequencyService {

    /**
     * 进行一次智能分析则调用次数自动减一
     *
     * @param userId
     * @return
     */
    @Override
    public synchronized boolean invokeAutoDecrease(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求错误");
        }
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency aiFrequency = this.getOne(wrapper);
        ThrowUtils.throwIf(aiFrequency == null, ErrorCode.NULL_ERROR, "此id用户不存在");

        Integer totalFrequency = aiFrequency.getTotalFrequency();
        Integer remainFrequency = aiFrequency.getRemainFrequency();
        // 总调用次数 +1
        totalFrequency = totalFrequency + 1;
        // 剩余次数 -1
        remainFrequency = remainFrequency - 1;

        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "剩余调用次数为0");
        }
        aiFrequency.setTotalFrequency(totalFrequency);
        aiFrequency.setRemainFrequency(remainFrequency);
        boolean result = this.updateById(aiFrequency);
        ThrowUtils.throwIf(!result, ErrorCode.NULL_ERROR);
        return true;
    }

    /**
     * 查看用户是否有次数
     *
     * @param userId
     * @return
     */
    @Override
    public boolean hasFrequency(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求错误");
        }
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency aiFrequency = this.getOne(wrapper);
        ThrowUtils.throwIf(aiFrequency == null, ErrorCode.NULL_ERROR, "此id用户不存在");
        int remainFrequency = aiFrequency.getRemainFrequency();
        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用次数不足");
        }
        return true;
    }
}





package com.shier.shierbi.controller;


import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shier.shierbi.common.BaseResponse;
import com.shier.shierbi.common.ErrorCode;
import com.shier.shierbi.common.ResultUtils;
import com.shier.shierbi.exception.BusinessException;
import com.shier.shierbi.model.entity.AiFrequency;
import com.shier.shierbi.model.entity.AiFrequencyOrder;
import com.shier.shierbi.model.entity.AlipayInfo;
import com.shier.shierbi.model.entity.User;
import com.shier.shierbi.model.enums.PayOrderEnum;
import com.shier.shierbi.model.vo.AlipayInfoVO;
import com.shier.shierbi.service.AiFrequencyOrderService;
import com.shier.shierbi.service.AiFrequencyService;
import com.shier.shierbi.service.AlipayInfoService;
import com.shier.shierbi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author niuma
 * @create 2023-04-30 15:43
 */
@Controller
@RequestMapping("/alipay")
@Slf4j
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class AliPayController {

    @Resource
    private AiFrequencyOrderService aiFrequencyOrderService;

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private UserService userService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    public static String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    public static String CHARSET = "UTF-8";
    public static String SIGNTYPE = "RSA2";

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    /**
     * 支付接口
     *
     * @param alipayAccountNo
     * @param response
     * @throws AlipayApiException
     * @throws IOException
     */
    @GetMapping("/pay")
    public void pay(String alipayAccountNo, HttpServletResponse response) throws AlipayApiException, IOException {
        if (StringUtils.isBlank(alipayAccountNo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AlipayInfo alipayInfo = getTotalAmount(alipayAccountNo);
        AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, SIGNTYPE);
        AlipayTradeWapPayRequest aliPayRequest = new AlipayTradeWapPayRequest();

        //异步通知的地址
        // liPayRequest.setNotifyUrl("http://dogwx.nat300.top/api/third/alipay/notify");

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", alipayInfo.getAlipayAccountNo());
        bizContent.put("total_amount", alipayInfo.getTotalAmount());
        bizContent.put("subject", "智能AI使用次数");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        aliPayRequest.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse aliPayResponse = alipayClient.pageExecute(aliPayRequest);

        if (!aliPayResponse.isSuccess()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI调用失败");
        }

        if (aliPayResponse.isSuccess()) {
            System.out.println("调用成功");
            response.setContentType("text/html;charset=" + CHARSET);
            String form = aliPayResponse.getBody();
            response.getWriter().write(form);
            response.getWriter().flush();
        }
    }

    /**
     * 生成二维码
     *
     * @param orderId
     * @param request
     * @return
     */
    @PostMapping("/payCode")
    @ResponseBody
    public BaseResponse<AlipayInfoVO> payCode(long orderId, HttpServletRequest request) {
        if (orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long alipayAccountNo = alipayInfoService.getPayNo(orderId, loginUser.getId());
        //String url = String.format("http://xxxxxx:8103/api/alipay/pay?alipayAccountNo=%s", alipayAccountNo);
        String url = String.format("http://192.168.11.219:8103/api/alipay/pay?alipayAccountNo=%s", alipayAccountNo);
        String generateQrCode = QrCodeUtil.generateAsBase64(url, new QrConfig(400, 400), "png");
        AlipayInfoVO alipayInfoVO = new AlipayInfoVO();
        alipayInfoVO.setAlipayAccountNo(String.valueOf(alipayAccountNo));
        alipayInfoVO.setQrCode(generateQrCode);
        alipayInfoVO.setOrderId(orderId);
        return ResultUtils.success(alipayInfoVO);
    }

    /**
     * 查询交易结果
     *
     * @throws AlipayApiException
     */
    @Transactional
    @PostMapping("/tradeQuery")
    public void tradeQuery(String alipayAccountNo) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, SIGNTYPE);
        AlipayInfo alipayInfo = getTotalAmount(alipayAccountNo);
        Long orderId = alipayInfo.getOrderId();
        AiFrequencyOrder orderId1 = getOrder(orderId);
        if (orderId1.getOrderStatus() == 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "你已经支付过了");
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", alipayAccountNo);

        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (!response.isSuccess()) {
            log.error("查询交易结果失败");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用失败");
        }
        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        alipayInfo.setPayStatus(Integer.valueOf(PayOrderEnum.COMPLETE.getValue()));
        alipayInfo.setAlipayId(trade_no);
        boolean updateComplete = alipayInfoService.updateById(alipayInfo);
        if (!updateComplete) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiFrequencyOrder order = getOrder(orderId);
        order.setOrderStatus(Integer.valueOf(PayOrderEnum.COMPLETE.getValue()));
        aiFrequencyOrderService.updateById(order);
        // 获取充值次数
        Long total = order.getPurchaseQuantity();
        Long userId = order.getUserId();
        AiFrequency aiFrequency = getHartFrequency(userId);

        if (aiFrequency == null) {
            AiFrequency frequency = new AiFrequency();
            frequency.setUserId(userId);
            frequency.setTotalFrequency(Integer.valueOf(PayOrderEnum.WAIT_PAY.getValue()));
            frequency.setRemainFrequency(Math.toIntExact(total));
            boolean save = aiFrequencyService.save(frequency);
            if (!save) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付成功回调发生错误");
            }
        }
        Integer remainFrequency = aiFrequency.getRemainFrequency();
        int i = Math.toIntExact(total);
        aiFrequency.setRemainFrequency(remainFrequency + i);
        boolean updateFrequency = aiFrequencyService.updateById(aiFrequency);
        if (!updateFrequency) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付成功回调发生错误");
        }
        log.info("调用成功，结果：" + response.getBody());
        //return ResultUtils.success(resultJson);
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param alipayAccountNo 支付交易号
     * @return 支付结果
     */
    @PostMapping("/query/payNo")
    @ResponseBody
    public void queryPayResultFromAlipay(String alipayAccountNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        try {
            bizContent.put("out_trade_no", alipayAccountNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
    }

    /**
     * 获取支付宝流水账号
     *
     * @param alipayAccountNo
     * @return
     */
    public AlipayInfo getTotalAmount(String alipayAccountNo) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("alipayAccountNo", alipayAccountNo);
        AlipayInfo aliPayOne = alipayInfoService.getOne(wrapper);
        if (aliPayOne == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有这个记录");
        }
        return aliPayOne;
    }

    /**
     * 获取Ai次数订单
     *
     * @param orderId
     * @return
     */
    public AiFrequencyOrder getOrder(Long orderId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("orderId", orderId);
        AiFrequencyOrder frequencyOrderServiceById = aiFrequencyOrderService.getById(orderId);
        if (frequencyOrderServiceById == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有这个记录");
        }
        return frequencyOrderServiceById;
    }

    /***
     * 获取Ai调用次数
     * @param userId
     * @return
     */
    public AiFrequency getHartFrequency(long userId) {

        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency one = aiFrequencyService.getOne(wrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return one;
    }
}

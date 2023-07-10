package com.shier.shierbi.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.shier.shierbi.service.OssService;
import com.shier.shierbi.utils.OssUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Shier
 */
@Service
public class OssServiceImpl implements OssService {

    /**
     * 上传头像到OSS
     */
    @Override
    public String uploadFileAvatar(MultipartFile file) {

        //工具类获取值
        String endpoint = OssUtil.END_POINT;
        String accessKeyId = OssUtil.KEY_ID;
        String accessKeySecret = OssUtil.KEY_SECRET;
        String bucketName = OssUtil.BUCKET_NAME;
        InputStream inputStream = null;
        try {
            // 创建OSS实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 获取上传文件的输入流
            inputStream = file.getInputStream();

            //获取文件名称
            String fileName = file.getOriginalFilename();
            //添加随机值
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            fileName = uuid + fileName;

            //把文件按照日期分类
            //获取当前日期
            String datePath = new DateTime().toString("yyyy-MM-dd");
            //拼接日期
            fileName = datePath + "/" + fileName;

            //调用oss实例中的方法实现上传
            //参数1： Bucket名称
            //参数2： 上传到oss文件路径和文件名称 /aa/bb/1.jpg
            //参数3： 上传文件的输入流
            ossClient.putObject(bucketName, fileName, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();

            // 把上传后文件路径返回
            // 需要把上传到阿里云oss路径手动拼接出来
            // https://achang-edu.oss-cn-hangzhou.aliyuncs.com/default.gif
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
package com.shier.shierbi.utils;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 常量类，读取配置文件application.yml中的配置
 * @author Shier
 */
@Component
public class FileUtils implements InitializingBean {

    public static String END_POINT;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String BUCKET_NAME;
    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.file.keyid}")
    private String keyid;
    @Value("${aliyun.oss.file.keysecret}")
    private String keysecret;
    @Value("${aliyun.oss.file.bucketname}")
    private String bucketname;

    @Override
    public void afterPropertiesSet() throws Exception {
        KEY_ID = this.keyid;
        KEY_SECRET = this.keysecret;
        END_POINT = this.endpoint;
        BUCKET_NAME = this.bucketname;
    }
}
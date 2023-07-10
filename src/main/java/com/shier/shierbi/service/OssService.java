package com.shier.shierbi.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Shier
 */
public interface OssService {
    /**
     * 上传头像到OSS
     *
     * @param file
     * @return
     */
    String uploadFileAvatar(MultipartFile file);
}

package com.shier.shierbi.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author Shier
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 密码
     */
    private String userPassword;


    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * 性别 男 女
     */
    private String gender;

    /**
     * 状态 0 - 正常 1-注销 2-封号
     */
    private Integer userStatus;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;


    private static final long serialVersionUID = 1L;
}
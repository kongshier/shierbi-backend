package com.shier.shierbi.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户编号
 * @author Shier
 */
@Data
public class UserCodeVO extends UserVO implements Serializable {

    /**
     * id
     */
    private Long id;


    private static final long serialVersionUID = 1L;
}
package com.woj.model.dto.questionsubmit;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 问题id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}
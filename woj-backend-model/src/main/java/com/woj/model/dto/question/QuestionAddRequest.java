package com.woj.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;


    /**
     * 判题参数（json 数组）
     */
    private JudgeConfig judgeConfig;

    /**
     * 判题实例（json 数组）
     */
    private List<JudgeCase> judgeCase;


    private static final long serialVersionUID = 1L;
}
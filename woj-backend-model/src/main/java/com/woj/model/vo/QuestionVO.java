package com.woj.model.vo;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.woj.model.dto.question.JudgeConfig;
import com.woj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class QuestionVO implements Serializable {

    private final static Gson GSON = new Gson();
    /**
     * id
     */
    private Long id;

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
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 判题参数（json 数组）
     */
    private JudgeConfig judgeConfig;


    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private UserVO user;


    private static final long serialVersionUID = 1L;

    public static QuestionVO objToVo(Question question) {
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question,questionVO);

        questionVO.setTags(GSON.fromJson(question.getTags(),new TypeToken<List<String>>() {
        }.getType()));
        questionVO.setJudgeConfig(GSON.fromJson(question.getJudgeConfig(),JudgeConfig.class));

        return questionVO;
    }
}

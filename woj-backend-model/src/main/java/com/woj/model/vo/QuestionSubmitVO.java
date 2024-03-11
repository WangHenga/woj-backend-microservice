package com.woj.model.vo;

import com.google.gson.Gson;

import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class QuestionSubmitVO implements Serializable {

    private final static Gson GSON = new Gson();
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 数组）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态，0-待判题，1-判题中，2-成功，3-失败
     */
    private Integer status;

    /**
     * 问题id
     */
    private Long questionId;

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

    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit,questionSubmitVO);
        questionSubmitVO.setJudgeInfo(GSON.fromJson(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        return questionSubmitVO;
    }
}

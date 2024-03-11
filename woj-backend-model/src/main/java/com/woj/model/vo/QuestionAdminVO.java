package com.woj.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.woj.model.dto.question.JudgeCase;
import com.woj.model.dto.question.JudgeConfig;
import com.woj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class QuestionAdminVO {
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
     * 题目答案
     */
    private String answer;

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
     * 判题实例（json 数组）
     */
    private List<JudgeCase> judgeCase;

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

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static QuestionAdminVO objToVo(Question question) {
        QuestionAdminVO questionAdminVO = new QuestionAdminVO();
        BeanUtils.copyProperties(question,questionAdminVO);

        questionAdminVO.setTags(GSON.fromJson(question.getTags(),new TypeToken<List<String>>() {
        }.getType()));
        questionAdminVO.setJudgeConfig(GSON.fromJson(question.getJudgeConfig(),JudgeConfig.class));
        questionAdminVO.setJudgeCase(GSON.fromJson(question.getJudgeCase(),new TypeToken<List<JudgeCase>>() {
        }.getType()));
        return questionAdminVO;
    }
}

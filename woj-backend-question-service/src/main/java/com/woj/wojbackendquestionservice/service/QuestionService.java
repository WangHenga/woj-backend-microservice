package com.woj.wojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.woj.model.dto.question.QuestionQueryRequest;
import com.woj.model.entity.Question;
import com.woj.model.entity.User;
import com.woj.model.vo.QuestionAdminVO;
import com.woj.model.vo.QuestionVO;


/**
* @author WangH
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-12-15 16:28:12
*/
public interface QuestionService extends IService<Question> {
    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    

    /**
     * 获取题目封装
     *
     * @param question
     * @param user
     * @return
     */
    QuestionVO getQuestionVO(Question question, User user);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param user
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, User user);

    Page<QuestionAdminVO> getQuestionAdminVOPage(Page<Question> questionPage);
}

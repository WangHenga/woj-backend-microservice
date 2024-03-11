package com.woj.wojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.woj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.woj.model.entity.QuestionSubmit;
import com.woj.model.entity.User;
import com.woj.model.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author WangH
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-12-17 20:58:41
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    void validQuestionSubmit(QuestionSubmit questionSubmit);

    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User user, HttpServletRequest request);

    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User user);
}

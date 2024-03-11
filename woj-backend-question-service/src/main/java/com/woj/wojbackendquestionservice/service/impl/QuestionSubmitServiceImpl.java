package com.woj.wojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;

import com.woj.common.common.ErrorCode;
import com.woj.common.constant.CommonConstant;
import com.woj.common.exception.BusinessException;
import com.woj.common.utils.SqlUtils;
import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.woj.model.entity.QuestionSubmit;
import com.woj.model.entity.User;
import com.woj.model.vo.QuestionSubmitVO;
import com.woj.wojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.woj.wojbackendquestionservice.service.QuestionSubmitService;
import com.woj.wojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author WangH
* @description 针对表【questionSubmit_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-12-17 20:58:41
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {
    private final static Gson GSON = new Gson();
    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public void validQuestionSubmit(QuestionSubmit questionSubmit) {
        String code = questionSubmit.getCode();
        if (StringUtils.isNotBlank(code) && code.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码过长");
        }
    }

    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User user, HttpServletRequest request) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        if(user==null){
            User loginUser = userFeignClient.getLoginUser(request);
            questionSubmitList=questionSubmitList.stream().map(questionSubmit->{
                if(loginUser==null|| !Objects.equals(loginUser.getId(), questionSubmit.getUserId()))
                    questionSubmit.setCode(null);
                return questionSubmit;
            }).collect(Collectors.toList());
        }
        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit,user)).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit,User user) {
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();

        BeanUtils.copyProperties(questionSubmit,questionSubmitVO);
        String judgeInfo = questionSubmit.getJudgeInfo();
        if(judgeInfo!=null){
            questionSubmitVO.setJudgeInfo(GSON.fromJson(judgeInfo, JudgeInfo.class));
        }
        Long userId = questionSubmit.getUserId();
        if(userId!=null){
            questionSubmitVO.setUser(userFeignClient.getUserVO(user));
        }
        return questionSubmitVO;
    }
}





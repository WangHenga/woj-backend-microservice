package com.woj.wojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;

import com.woj.common.constant.RedisConstant;
import com.woj.model.annotation.AuthCheck;
import com.woj.common.common.BaseResponse;
import com.woj.common.common.DeleteRequest;
import com.woj.common.common.ErrorCode;
import com.woj.common.common.ResultUtils;
import com.woj.common.constant.UserConstant;
import com.woj.common.exception.BusinessException;
import com.woj.common.exception.ThrowUtils;
import com.woj.model.dto.question.*;
import com.woj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.woj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.woj.model.entity.Question;
import com.woj.model.entity.QuestionSubmit;
import com.woj.model.entity.User;
import com.woj.model.enums.QuestionSubmitLanguageEnum;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.vo.QuestionAdminVO;
import com.woj.model.vo.QuestionSubmitVO;
import com.woj.model.vo.QuestionVO;
import com.woj.wojbackendquestionservice.rabbitmq.MessageProducer;
import com.woj.wojbackendquestionservice.service.QuestionService;
import com.woj.wojbackendquestionservice.service.QuestionSubmitService;
import com.woj.wojbackendquestionservice.task.RankTask;
import com.woj.wojbackendserviceclient.service.JudgeFeignClient;
import com.woj.wojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class QuestionController {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private MessageProducer messageProducer;
    @Resource
    private RankTask rankTask;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
//        User loginUser = userFeignClient.getLoginUser(request);
//        if(loginUser==null||!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())){
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();

        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        if(judgeConfig!=null){
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        if(judgeCase!=null){
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        questionService.validQuestion(question, true);
        question.setUserId(userFeignClient.getLoginUser(request).getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
//        User loginUser = userFeignClient.getLoginUser(request);
//        if(loginUser==null||!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())){
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userFeignClient.getLoginUser(request);
        long id = deleteRequest.getId();
        redisTemplate.delete(String.format("question_%d",id));
        // 如果删除的是热题，将其从热题列表中移除
        rankTask.removeFromChart(id);
        // 判断是否存在
        Question oldQuestion=questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userFeignClient.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        if(b) {
            // 延迟双删
            CompletableFuture.runAsync(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    redisTemplate.delete(String.format("question_%d",id));
                }
            });
        }
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest,
                                                HttpServletRequest request) {
//        User loginUser = userFeignClient.getLoginUser(request);
//        if(loginUser==null||!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())){
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();

        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        String key = String.format("question_%d", question.getId());
        redisTemplate.delete(key);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        if(judgeConfig!=null){
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        if(judgeCase!=null){
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        if(result){
            // 延迟双删
            CompletableFuture.runAsync(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    redisTemplate.delete(key);
                }
            });
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        String key=String.format("question_%d",id);
        QuestionVO questionVO= (QuestionVO) redisTemplate.opsForValue().get(key);
        redisTemplate.opsForZSet().incrementScore(RedisConstant.QUESTION_VIEW,id,1);
        if(questionVO!=null){
            return ResultUtils.success(questionVO);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        questionVO=questionService.getQuestionVO(question, user);
        if(rankTask.isQuestionInChart(id))
            redisTemplate.opsForValue().set(key,questionVO,2, TimeUnit.HOURS);
        return ResultUtils.success(questionVO);
    }
    /**
     * 获取热题列表
     */
    @GetMapping("/get/hotlist")
    public BaseResponse<List<QuestionVO>> getHotQuestionList(){
        Set<Long> charts = rankTask.getCharts();
        List<QuestionVO> hotList=new ArrayList<>();
        for(long questionId:charts){
            String key=String.format("question_%d", questionId);
            QuestionVO questionVO = (QuestionVO) redisTemplate.opsForValue().get(key);
            if(questionVO==null){
                Question question = questionService.getById(questionId);
                if(question==null){
                    continue;
                }
                Long userId = question.getUserId();
                User user = null;
                if (userId != null && userId > 0) {
                    user = userFeignClient.getById(userId);
                }
                questionVO=questionService.getQuestionVO(question, user);
                redisTemplate.opsForValue().set(key,questionVO,2, TimeUnit.HOURS);
            }
            hotList.add(questionVO);
        }
        return ResultUtils.success(hotList);
    }

    /**
     * 根据 id 获取(管理员)
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionAdminVO> getQuestionById(long id, HttpServletRequest request) {
//        User loginUser = userFeignClient.getLoginUser(request);
//        if(loginUser==null||!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())){
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(QuestionAdminVO.objToVo(question));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        Long userId = questionQueryRequest.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, user));
    }

    /**
     * 分页获取资源列表(管理员)
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                  HttpServletRequest request) {
//        User loginUser = userFeignClient.getLoginUser(request);
//        if(loginUser==null||!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())){
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }


    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();

        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        if(judgeConfig!=null){
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        if(judgeCase!=null){
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userFeignClient.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userFeignClient.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 题目提交数
     */
    @PostMapping("/submit/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        // 登录才能操作
        final User loginUser = userFeignClient.getLoginUser(request);
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        if (questionSubmitAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if(question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if(languageEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitAddRequest, questionSubmit);

        questionSubmitService.validQuestionSubmit(questionSubmit);
        questionSubmit.setUserId(loginUser.getId());
        // 设置初始status
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());

        boolean result = questionSubmitService.save(questionSubmit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"数据插入失败");
        // 题目提交数加1
        Question newQuestion = new Question();
        newQuestion.setId(question.getId());
        newQuestion.setSubmitNum(question.getSubmitNum()+1);
        questionService.updateById(newQuestion);
        long newQuestionSubmitId = questionSubmit.getId();
//        CompletableFuture.runAsync(()->{
//            judgeFeignClient.doJudge(newQuestionSubmitId);
//        });
        messageProducer.sendMessage("exchange.woj","default",String.valueOf(newQuestionSubmitId));
        return ResultUtils.success(newQuestionSubmitId);
    }

    /**
     * 获取我的提交列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     */
    @PostMapping("/submit/my/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listMyQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                           HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 只查询当前用户的提交
        questionSubmitQueryRequest.setUserId(loginUser.getId());
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        User user = userFeignClient.getLoginUser(request);
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, user,request));
    }

    /**
     * 获取所有用户提交列表
     * TODO 管理员可以查看所有提交，普通用户只能查看自己的提交
     * @param questionSubmitQueryRequest
     * @param request
     */
    @PostMapping("/submit/list/page")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, null,request));
    }

}

package com.woj.wojbackendquestionservice.task;

import com.woj.common.common.ErrorCode;
import com.woj.common.constant.RedisConstant;
import com.woj.common.exception.BusinessException;
import com.woj.model.entity.Question;
import com.woj.model.entity.User;
import com.woj.model.vo.QuestionVO;
import com.woj.wojbackendquestionservice.service.QuestionService;
import com.woj.wojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RankTask {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private QuestionService questionService;
    @Resource
    private UserFeignClient userFeignClient;
    // 排行榜，保存前一天的热题
    private Set<Long> charts;
    public Set<Long> getCharts(){
        return charts;
    }
    // 初始化方法
    @PostConstruct
    public void init(){
        charts = redisTemplate.opsForZSet().reverseRange(RedisConstant.OLD_QUESTION_VIEW,0,-1);
    }

    // 判断题目是否在排行榜中
    public boolean isQuestionInChart(Long questionId){
        return charts.contains(questionId);
    }
    public void removeFromChart(Long questionId){
        if(isQuestionInChart(questionId)){
            charts.remove(questionId);
            redisTemplate.opsForZSet().remove(RedisConstant.OLD_QUESTION_VIEW,questionId);
        }
    }
    @Scheduled(cron = "0 0 12 * * ?")
    public void rankQuestionAndCache(){
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        charts = zSetOperations.reverseRange(RedisConstant.QUESTION_VIEW, 0, 9);
        redisTemplate.delete(RedisConstant.QUESTION_VIEW);
        int score=10;
        for(long questionId: charts){
            Question question = questionService.getById(questionId);
            if (question == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            zSetOperations.add(RedisConstant.OLD_QUESTION_VIEW,questionId,score--);
            Long userId = question.getUserId();
            User user = null;
            if (userId != null && userId > 0) {
                user = userFeignClient.getById(userId);
            }
            QuestionVO questionVO= questionService.getQuestionVO(question, user);
            redisTemplate.opsForValue().set(String.format("question_%d",questionId),questionVO,24, TimeUnit.HOURS);
        }
        log.info("Updated the rank of the questions");
    }
}

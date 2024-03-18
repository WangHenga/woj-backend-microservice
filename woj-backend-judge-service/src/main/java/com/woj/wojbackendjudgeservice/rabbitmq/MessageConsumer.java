package com.woj.wojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.woj.wojbackendjudgeservice.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class MessageConsumer {

    @Resource
    private JudgeService judgeService;

    private final ExecutorService executor;

    public MessageConsumer(){
        executor=Executors.newFixedThreadPool(3);
    }

    @PreDestroy
    public void shutdown(){
        executor.shutdown();
    }
    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {"queue.woj"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        executor.submit(()->{
            log.info("receiveMessage message = {}", message);
            long questionSubmitId = Long.parseLong(message);
            try {
                judgeService.doJudge(questionSubmitId);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                try {
                    channel.basicNack(deliveryTag, false, false);
                } catch (IOException ex) {
                    log.error("Error sending NACK for message with delivery tag {}", deliveryTag, ex);
                }
            }
        });
    }

}


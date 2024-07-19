package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author 吴勇华
 * @description: TODO
 */
@Component
@Slf4j
public class MyTask {
    /*
     * 定时任务 每隔 5 秒触发一次
     * @return: void
     **/
    @Scheduled(cron = "0,10,20,40 * * * * ?")
    public void executeTask() {
//        log.info("定时任务开始执行：{}", new Date());
    }
}

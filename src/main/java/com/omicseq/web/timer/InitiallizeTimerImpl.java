package com.omicseq.web.timer;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InitiallizeTimerImpl implements InitiallizeTimer {

	@Scheduled(cron="5 * *  * * ?")   //启动后5秒执行一次
	@Override
	public void initial() {
		System.out.println("##########################################" + new Date());
	}
	 
}

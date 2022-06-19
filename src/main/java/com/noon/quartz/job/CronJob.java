package com.noon.quartz.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.noon.quartz.config.QuartzSchedulerConfig;
import com.noon.quartz.util.Constants;
import com.noon.quartz.util.HttpUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.noon.quartz.service.JobService;

public class CronJob extends QuartzJobBean implements InterruptableJob{

	@Autowired
	HttpUtils httpUtils;

	@Autowired
	QuartzSchedulerConfig quartzSchedulerConfig;
	
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		Constants.requests.increment();
		JobKey key = jobExecutionContext.getJobDetail().getKey();
		System.out.println("Cron Job started with key :" + key.getName() + ", Group :"+key.getGroup() + " , Thread Name :"+Thread.currentThread().getName() + " ,Time now :"+new Date());
		
//		System.out.println("======================================");
//		System.out.println("Accessing annotation example: "+jobService.getAllJobs());
//		List<Map<String, Object>> list = jobService.getAllJobs();
//		System.out.println("Job list :"+list);
//		System.out.println("======================================");
		
		//*********** For retrieving stored key-value pairs ***********/
		JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
		if (!dataMap.containsKey("target")){
			return;
		}
		String target = dataMap.getString("target");

		Map<String, String> targetService = new HashMap<>();

		quartzSchedulerConfig.getServicesConfig().getServices().forEach((mapKey, mapValue) -> {
			if (mapValue.getTarget().contains(target)){
				targetService.put(target, mapKey);
			}
		});

		if (targetService.size() == 0){
			return;
		}

		httpUtils.post(key.getName(), targetService.get(target), dataMap);

		System.out.println("Thread: "+ Thread.currentThread().getName() +" stopped.");
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		System.out.println("Stopping thread... ");
	}

}
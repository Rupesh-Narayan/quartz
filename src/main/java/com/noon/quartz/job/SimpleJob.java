package com.noon.quartz.job;

import com.noon.quartz.config.QuartzSchedulerConfig;
import com.noon.quartz.util.Constants;
import com.noon.quartz.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SimpleJob extends QuartzJobBean implements InterruptableJob{

	@Autowired
	HttpUtils httpUtils;

	@Autowired
	QuartzSchedulerConfig quartzSchedulerConfig;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) {
		Constants.requests.increment();
		JobKey key = jobExecutionContext.getJobDetail().getKey();
		System.out.println("Simple Job started with key :" + key.getName() + ", Group :"+key.getGroup() + " , Thread Name :"+Thread.currentThread().getName());
		
//		System.out.println("======================================");
//		System.out.println("Accessing annotation example: "+jobService.getAllJobs());
//		List<Map<String, Object>> list = jobService.getAllJobs();
//		System.out.println("Job list :"+list);
//		System.out.println("======================================");
		
		//*********** For retrieving stored key-value pairs ***********/
		JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
		log.info("reached simple job");
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

		log.info("before post");

		httpUtils.post(key.getName(), targetService.get(target), dataMap);

		log.info("after post");

		//*********** For retrieving stored object, It will try to deserialize the bytes Object. ***********/
		/*
		SchedulerContext schedulerContext = null;
        try {
            schedulerContext = jobExecutionContext.getScheduler().getContext();
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        }
        YourClass yourClassObject = (YourClass) schedulerContext.get("storedObjectKey");
		 */

		System.out.println("Thread: "+ Thread.currentThread().getName() +" stopped.");
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		System.out.println("Stopping thread... ");
	}

}
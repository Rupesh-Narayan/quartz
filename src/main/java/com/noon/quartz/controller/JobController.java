package com.noon.quartz.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.noon.quartz.config.QuartzSchedulerConfig;
import com.noon.quartz.dto.ServerResponse;
import com.noon.quartz.job.CronJob;
import com.noon.quartz.job.SimpleJob;
import com.noon.quartz.service.JobService;
import com.noon.quartz.util.Constants;
import com.noon.quartz.util.ServerResponseCode;
import com.noon.quartz.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/quartz/v1/")
@Slf4j
public class JobController {

	@Autowired
	@Lazy
	JobService jobService;

	@Autowired
	Validations validations;

	@Autowired
	QuartzSchedulerConfig quartzSchedulerConfig;

	@RequestMapping("schedule")	
	public ResponseEntity<Object> schedule(@RequestParam("job_name") String jobName,
								   @RequestParam(value = "job_schedule_time", required = false) Long jobScheduleTimeLong,
								   @RequestParam(value = "cron_expression", required = false) String cronExpression, @RequestBody Map<String, Object> payload){
		System.out.println("JobController.schedule()");
		log.info("Schedule: jon_name: {}, job_schedule_time: {}, cron_expression: {}", jobName, jobScheduleTimeLong, cronExpression);
		if(cronExpression == null && jobScheduleTimeLong == null){
			return ResponseEntity.status(ServerResponseCode.ERROR).body("either cron_expression or job_schedule_time should be there in query param");
		}

		Date jobScheduleTime = null;
		if(jobScheduleTimeLong != null){
			jobScheduleTime = new Date(jobScheduleTimeLong);
		}

		if (!validations.validatePayload(payload)){
			return ResponseEntity.status(ServerResponseCode.PAYLOAD_NOT_PROPER).body("body having invalid target");
		}


		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			return ResponseEntity.status(ServerResponseCode.JOB_NAME_NOT_PRESENT).body("job_name not present in body");
		}

		//Check if job Name is unique;
		if(!jobService.isJobWithNamePresent(jobName)){

			if(cronExpression == null || cronExpression.trim().equals("")){
				//Single Trigger
				if (jobScheduleTimeLong <= System.currentTimeMillis()){
					return ResponseEntity.status(ServerResponseCode.JOB_SCHEDULE_TIME_BEFORE_CURRENT_TIME).body("schedule time before current time");
				}
				boolean status = jobService.scheduleOneTimeJob(jobName, payload, SimpleJob.class, jobScheduleTime);
				if(status){
					return ResponseEntity.status(ServerResponseCode.SUCCESS).body(null);
				}else{
					return ResponseEntity.status(ServerResponseCode.ERROR).body("Error in job configuring");
				}
				
			}else{
				//Cron Trigger
				boolean status = jobService.scheduleCronJob(jobName, payload, CronJob.class, jobScheduleTime, cronExpression);
				if(status){
					return ResponseEntity.status(ServerResponseCode.SUCCESS).body(null);
				}else{
					return ResponseEntity.status(ServerResponseCode.ERROR).body("Error in job configuring");
				}				
			}
		}else{
			return ResponseEntity.status(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST).body("job_name already exist");
		}
	}

	@RequestMapping("unschedule")
	public void unschedule(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.unschedule()");
		jobService.unScheduleJob(jobName);
	}

		@RequestMapping("delete")
	public ResponseEntity<Object> delete(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.delete()");
		log.info("Delete: jon_name: {}", jobName);
		if(jobService.isJobWithNamePresent(jobName)){
			boolean isJobRunning = jobService.isJobRunning(jobName);

			if(!isJobRunning){
				boolean status = jobService.deleteJob(jobName);
				if(status){
					return ResponseEntity.status(ServerResponseCode.SUCCESS).body(null);
				}else{
					return ResponseEntity.status(ServerResponseCode.ERROR).body("Error in job configuring");
				}
			}else{
				return ResponseEntity.status(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE).body(null);
			}
		}else{
			//Job doesn't exist
			return ResponseEntity.status(ServerResponseCode.JOB_DOESNT_EXIST).body("job doesn't exist");
		}
	}

	@RequestMapping("pause")
	public ServerResponse pause(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.pause()");

		if(jobService.isJobWithNamePresent(jobName)){

			boolean isJobRunning = jobService.isJobRunning(jobName);

			if(!isJobRunning){
				boolean status = jobService.pauseJob(jobName);
				if(status){
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					return getServerResponse(ServerResponseCode.ERROR, false);
				}			
			}else{
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		}else{
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}		
	}

	@RequestMapping("resume")
	public ServerResponse resume(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.resume()");

		if(jobService.isJobWithNamePresent(jobName)){
			String jobState = jobService.getJobState(jobName);

			if(jobState.equals("PAUSED")){
				System.out.println("Job current state is PAUSED, Resuming job...");
				boolean status = jobService.resumeJob(jobName);

				if(status){
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			}else{
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_PAUSED_STATE, false);
			}

		}else{
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("update")
	public ResponseEntity<Object> updateJob(@RequestParam("job_name") String jobName,
			@RequestParam(value = "job_schedule_time", required = false) Long jobScheduleTimeLong,
			@RequestParam(value = "cron_expression", required = false) String cronExpression){
		System.out.println("JobController.updateJob()");
		log.info("Update: jon_name: {}, job_schedule_time: {}, cron_expression: {}", jobName, jobScheduleTimeLong, cronExpression);
		if(cronExpression == null && jobScheduleTimeLong == null){
			return ResponseEntity.status(ServerResponseCode.ERROR).body("either cron_expression or job_schedule_time should be there in query param");
		}

		Date jobScheduleTime = null;
		if(jobScheduleTimeLong != null){
			jobScheduleTime = new Date(jobScheduleTimeLong);
		}

		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			return ResponseEntity.status(ServerResponseCode.JOB_NAME_NOT_PRESENT).body("job_name not present in body");
		}

		//Edit Job
		if(jobService.isJobWithNamePresent(jobName)){
			
			if(cronExpression == null || cronExpression.trim().equals("")){
				//Single Trigger
				if (jobScheduleTimeLong <= System.currentTimeMillis()){
					return ResponseEntity.status(ServerResponseCode.JOB_SCHEDULE_TIME_BEFORE_CURRENT_TIME).body("schedule time before current time");
				}
				boolean status = jobService.updateOneTimeJob(jobName, jobScheduleTime);
				if(status){
					return ResponseEntity.status(ServerResponseCode.SUCCESS).body(null);
				}else{
					return ResponseEntity.status(ServerResponseCode.ERROR).body("Error in job configuring");
				}
				
			}else{
				//Cron Trigger
				boolean status = jobService.updateCronJob(jobName, jobScheduleTime, cronExpression);
				if(status){
					return ResponseEntity.status(ServerResponseCode.SUCCESS).body(null);
				}else{
					return ResponseEntity.status(ServerResponseCode.ERROR).body("Error in job configuring");
				}				
			}
			
			
		}else{
			return ResponseEntity.status(ServerResponseCode.JOB_DOESNT_EXIST).body("job doesn't exist");
		}
	}

	@RequestMapping("jobs")
	public ServerResponse getAllJobs(){
		System.out.println("JobController.getAllJobs()");

		List<Map<String, Object>> list = jobService.getAllJobs();
		return getServerResponse(ServerResponseCode.SUCCESS, list);
	}

	@RequestMapping("checkJobName")
	public ServerResponse checkJobName(@RequestParam("job_name") String jobName){
		System.out.println("JobController.checkJobName()");

		//Job Name is mandatory
		if(jobName == null || jobName.trim().equals("")){
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}
		
		boolean status = jobService.isJobWithNamePresent(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, status);
	}

	@RequestMapping("isJobRunning")
	public ServerResponse isJobRunning(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.isJobRunning()");

		boolean status = jobService.isJobRunning(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, status);
	}

	@RequestMapping("jobState")
	public ServerResponse getJobState(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.getJobState()");

		String jobState = jobService.getJobState(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, jobState);
	}

	@RequestMapping("stop")
	public ServerResponse stopJob(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.stopJob()");

		if(jobService.isJobWithNamePresent(jobName)){

			if(jobService.isJobRunning(jobName)){
				boolean status = jobService.stopJob(jobName);
				if(status){
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				}else{
					//Server error
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			}else{
				//Job not in running state
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_RUNNING_STATE, false);
			}

		}else{
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("start")
	public ServerResponse startJobNow(@RequestParam("job_name") String jobName) {
		System.out.println("JobController.startJobNow()");

		if(jobService.isJobWithNamePresent(jobName)){

			if(!jobService.isJobRunning(jobName)){
				boolean status = jobService.startJobNow(jobName);

				if(status){
					//Success
					return getServerResponse(ServerResponseCode.SUCCESS, true);

				}else{
					//Server error
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			}else{
				//Job already running
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		}else{
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	public ServerResponse getServerResponse(int responseCode, Object data){
		ServerResponse serverResponse = new ServerResponse();
		serverResponse.setStatusCode(responseCode);
		serverResponse.setData(data);
		return serverResponse; 
	}
}

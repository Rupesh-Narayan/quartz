package com.noon.quartz.util;

public class ServerResponseCode {
	
	//SPECIFIC ERROR CODES
	public static final int JOB_WITH_SAME_NAME_EXIST = 409;
	public static final int JOB_NAME_NOT_PRESENT = 400;
	
	public static final int JOB_ALREADY_IN_RUNNING_STATE = 431;
	
	public static final int JOB_NOT_IN_PAUSED_STATE = 432;
	public static final int JOB_NOT_IN_RUNNING_STATE = 433;
	
	public static final int JOB_DOESNT_EXIST = 400;

	public static final int PAYLOAD_NOT_PROPER = 400;

	public static final int JOB_SCHEDULE_TIME_BEFORE_CURRENT_TIME = 406;
	
	//GENERIC ERROR
	public static final int ERROR = 500;
	
	//SUCCESS CODES
	public static final int SUCCESS = 200;
}

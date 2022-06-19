package com.noon.quartz.service;

import com.noon.quartz.repository.QrtzAuditDaoImpl;
import com.noon.quartz.util.Constants;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class TriggerListenerImpl implements TriggerListener {

    @Autowired
    @Lazy
    QrtzAuditDaoImpl qrtzAuditDao;

    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        String jobName = trigger.getJobKey().getName();
        Constants.AUDIT_JOB_TYPE auditJobType;
        if (trigger.mayFireAgain()){
            auditJobType = Constants.AUDIT_JOB_TYPE.cron;
        }
        else {
            auditJobType = Constants.AUDIT_JOB_TYPE.simple;
        }
        Constants.hits.increment();
        qrtzAuditDao.save(jobName, jobName, trigger.getPreviousFireTime(), auditJobType, Constants.AUDIT_EVENT_TYPE.fire);
    	System.out.println("TriggerListenerImpl.triggerFired()");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
    	System.out.println("TriggerListenerImpl.vetoJobExecution()");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        Constants.misfires.increment();
    	System.out.println("TriggerListenerImpl.triggerMisfired()");
        String jobName = trigger.getJobKey().getName();
        Constants.AUDIT_JOB_TYPE auditJobType;
        if (trigger.mayFireAgain()){
            auditJobType = Constants.AUDIT_JOB_TYPE.cron;
        }
        else {
            auditJobType = Constants.AUDIT_JOB_TYPE.simple;
        }
        qrtzAuditDao.save(jobName, jobName, trigger.getPreviousFireTime(), auditJobType, Constants.AUDIT_EVENT_TYPE.misfire);
        System.out.println("Job name: " + jobName + " is misfired");
        
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        System.out.println("TriggerListenerImpl.triggerComplete()");
    }
}

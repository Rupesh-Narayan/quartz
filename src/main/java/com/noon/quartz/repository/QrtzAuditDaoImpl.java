package com.noon.quartz.repository;

import com.noon.quartz.model.QrtzAudit;
import com.noon.quartz.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class QrtzAuditDaoImpl {

    @Autowired
    QuartzRepository quartzRepository;

    public void save(String jobName, String triggerName, Constants.AUDIT_JOB_TYPE auditJobType, Constants.AUDIT_EVENT_TYPE auditEventType){
        Date currTime = new Date();
        quartzRepository.save(QrtzAudit.builder().jobName(jobName).triggerName(triggerName).jobType(auditJobType.toString()).eventType(auditEventType.toString()).createdAt(currTime).build());
    }

    public void save(String jobName, String triggerName, Date fireTime, Constants.AUDIT_JOB_TYPE auditJobType, Constants.AUDIT_EVENT_TYPE auditEventType){
        Date currTime = new Date();
        quartzRepository.save(QrtzAudit.builder().jobName(jobName).triggerName(triggerName).fireTime(fireTime).jobType(auditJobType.toString()).eventType(auditEventType.toString()).createdAt(currTime).build());
    }

}

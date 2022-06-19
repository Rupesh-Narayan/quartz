package com.noon.quartz.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "qrtz_AUDIT")
@JsonIgnoreProperties(value = {"createdAt"},
        allowGetters = true)
@Builder
public class QrtzAudit implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name="job_name")
    private String jobName;

    @NotBlank
    @Column(name="trigger_name")
    private String triggerName;

    @Column(name="previous_fire_time")
    private Date fireTime;

    @NotBlank
    @Column(name="event_type")
    private String eventType;

    @NotBlank
    @Column(name="job_type")
    private String jobType;

    @Column(name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

}

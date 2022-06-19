package com.noon.quartz.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Getter
@Slf4j
public class Constants {

    public final static String NOONGATE_SERVICE = "noongate";

    public static final Counter requests = Metrics.counter("entity.count", "type", "order");

    public static final Counter misfires = Metrics.counter("misfires.count", "type", "order");

    public static final Counter hits = Metrics.counter("hits.count", "type", "order");

    public final static String NOONGATE_INJEST = "noongateInjest";

    public enum AUDIT_EVENT_TYPE{
        add, update, delete, fire, misfire, pause, resume, start, stop
    }

    public enum AUDIT_JOB_TYPE{
        simple, cron, general
    }

}

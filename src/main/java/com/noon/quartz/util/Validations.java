package com.noon.quartz.util;

import com.noon.quartz.config.QuartzSchedulerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class Validations {

    @Autowired
    QuartzSchedulerConfig quartzSchedulerConfig;


    public Boolean validatePayload(Map<String, Object> payload) {

        String target = (String) payload.get("target");

        Map<String, String> targetService = new HashMap<>();

        quartzSchedulerConfig.getServicesConfig().getServices().forEach((mapKey, mapValue) -> {
            if (mapValue.getTarget().contains(target)){
                targetService.put(target, mapKey);
            }
        });

        if (targetService.size() == 0){
            return false;
        }

        return true;

    }

}

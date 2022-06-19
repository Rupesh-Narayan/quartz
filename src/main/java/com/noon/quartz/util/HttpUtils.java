package com.noon.quartz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noon.commons.config.HttpServiceConfig;
import com.noon.commons.httpclient.NoonHttpClient;
import com.noon.commons.httpclient.NoonHttpResponse;
import com.noon.quartz.config.QuartzSchedulerConfig;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HttpUtils {

    @Autowired
    QuartzSchedulerConfig quartzSchedulerConfig;

    @Autowired
    ObjectMapper objectMapper;

    public Boolean post(String job, String target, JobDataMap payload) {

        NoonHttpResponse noonHttpResponse = new NoonHttpResponse();
        HttpServiceConfig httpServiceConfig = quartzSchedulerConfig.getExternalServicesConfig().getServices().get(Constants.NOONGATE_SERVICE);
        String url = httpServiceConfig.getHost() + ":" + httpServiceConfig.getPort() + httpServiceConfig.getApis().get(Constants.NOONGATE_INJEST).getEndpoint() + quartzSchedulerConfig.getServicesConfig().getServices().get(target).getTopic();
        NoonHttpClient noonHttpClient = NoonHttpClient.getInstance();
        String jsonString;

        try {
            jsonString = objectMapper.writeValueAsString(payload);
        }
        catch (JsonProcessingException e){
            log.error("Json formatting error for job: {} and target: {}", job, target);
            return false;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");


        try {
            noonHttpResponse = noonHttpClient.send(HttpMethod.POST, url, null, jsonString, headers, httpServiceConfig.getApis().get(Constants.NOONGATE_INJEST));
        }
        catch (URISyntaxException e){
            log.error("URI syntax error for job: {} and target: {}", job, target);
            return false;
        }
        catch (IOException e){
            log.error("IO exception for job: {} and target: {}", job, target);
            return false;
        }

        if (noonHttpResponse == null){
            log.error("Http post error for job: {} and target: {}", job, target);
            return false;
        }

        if (noonHttpResponse.getStatusCode() >= 400){
            log.error("Http post error for job: {} and target: {}", job, target);
            return false;
        }

        return true;

    }

}

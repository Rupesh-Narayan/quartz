package com.noon.quartz.config;

import lombok.Data;

import java.util.List;

@Data
public class ServiceConfig {

    String topic;

    List<String> target;
}

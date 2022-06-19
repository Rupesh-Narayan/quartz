package com.noon.quartz.config;

import lombok.Data;

import java.util.Map;

@Data
public class ServicesConfig {

    Map<String, ServiceConfig> services;
}

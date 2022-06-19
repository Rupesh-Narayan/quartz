package com.noon.quartz.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.noon.commons.config.ExternalServicesConfig;
import com.noon.commons.config.HttpClientConfig;
import com.noon.commons.config.MysqlConfig;
import com.noon.commons.httpclient.NoonHttpClient;
import com.noon.quartz.service.TriggerListenerImpl;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.noon.quartz.service.JobsListener;

@Data
@ToString
@Configuration
@ConfigurationProperties
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class QuartzSchedulerConfig extends io.dropwizard.Configuration {

    private MysqlConfig mysqlConfig;

    private ExternalServicesConfig externalServicesConfig;

    private ServicesConfig servicesConfig;
 
    @Autowired
    private ApplicationContext applicationContext;
     
    @Autowired
    private TriggerListenerImpl triggerListenerImpl;

    @Autowired
    private JobsListener jobsListener;

    @Autowired
    private ModelMapper modelMapper;
    
    /**
     * create scheduler
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
 
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource());
        factory.setQuartzProperties(quartzProperties());
        
        //Register listeners to get notification on Trigger misfire etc
        factory.setGlobalTriggerListeners(triggerListenerImpl);
        factory.setGlobalJobListeners(jobsListener);
        
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);
        
        return factory;
    }
 
    /**
     * Configure quartz using properties file
     */
    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public ModelMapper modelMapper(){
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    @Autowired
    public DataSource dataSource() {
        final BoneCPConfig boneCPConfig = modelMapper.map(mysqlConfig, BoneCPConfig.class);
        return new BoneCPDataSource(boneCPConfig);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public HttpClientConfig httpClientConfig(){
        return new HttpClientConfig();
    }

    @Bean
    public NoonHttpClient noonHttpClient(){
        return NoonHttpClient.getInstance(httpClientConfig());
    }

    @Bean
    public CloseableHttpClient getCloseableHttpClient() {
        return HttpClientBuilder.create().build();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }
 
  
}
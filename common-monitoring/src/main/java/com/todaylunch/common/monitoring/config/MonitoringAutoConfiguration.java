package com.todaylunch.common.monitoring.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:META-INF/monitoring-defaults.properties")
public class MonitoringAutoConfiguration {
}

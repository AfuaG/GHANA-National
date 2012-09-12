package org.motechproject.ghana.national.logger.configuration;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.motechproject.MotechException;
import org.motechproject.event.metrics.MetricsAgent;
import org.motechproject.event.metrics.MetricsAgentBackend;
import org.motechproject.event.metrics.impl.MultipleMetricsAgentImpl;
import org.motechproject.ghana.national.logger.advice.BackgroundJobDbLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.sql.DataSource;
import java.util.ArrayList;

@Configuration
@ImportResource("classpath:statMetricsAgent.xml")
public class BackgroundJobLoggerConfiguration {

    @Autowired
    @Qualifier("loggingAgent")
    private MetricsAgentBackend loggingAgent;

    @Autowired
    @Qualifier("statsdAgent")
    private MetricsAgentBackend statsdAgent;

    @Bean(name="backgroundJobDbLogger")
    public MetricsAgentBackend dbLogger(){
        return new BackgroundJobDbLogger(dataSource());
    }


    // create table background_job_logs(metric varchar(250), st_time datetime, duration mediumint unsigned);
    @Bean(name = "performanceTestlogsDataBase")
    public DataSource dataSource(){
        try {
            final ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
            comboPooledDataSource.setDriverClass("com.mysql.jdbc.Driver");
            comboPooledDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/motech_logs?autoReconnect=true");
            comboPooledDataSource.setUser("root");
            comboPooledDataSource.setPassword("password");
            comboPooledDataSource.setAcquireIncrement(5);
            comboPooledDataSource.setMinPoolSize(30);
            comboPooledDataSource.setMaxPoolSize(50);
            comboPooledDataSource.setMaxIdleTime(3600);
            return comboPooledDataSource;
        } catch (Exception e) {
            throw new MotechException("Encountered error while creating datasource for logging", e);
        }
    }

    @Bean(name = "backgroundJobMetricAgent")
    public MetricsAgent metricsAgent(){
        MultipleMetricsAgentImpl multipleMetricsAgent = new MultipleMetricsAgentImpl();
        multipleMetricsAgent.setMetricsAgents(new ArrayList<MetricsAgentBackend>(){{
            add(loggingAgent);
            add(statsdAgent);
            add(dbLogger());
        }});
        return multipleMetricsAgent;
    }


}

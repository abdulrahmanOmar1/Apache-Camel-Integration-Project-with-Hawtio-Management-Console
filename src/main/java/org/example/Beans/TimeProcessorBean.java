package org.example.Beans;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.camel.Exchange;
import org.apache.camel.util.StopWatch;
import org.example.entity.RequestLog;

@Singleton
@Named("TimeProcessorBean")
@Unremovable
public class TimeProcessorBean {
    public void startTime(Exchange exchange) {
        StopWatch stopWatch = new StopWatch();
        exchange.setProperty("stopWatch", stopWatch);
    }

    public void calculateExecutionTime(Exchange exchange) {
        StopWatch stopWatch = exchange.getProperty("stopWatch", StopWatch.class);
        long executionTime = stopWatch.taken();

        RequestLog requestLog = exchange.getIn().getBody(RequestLog.class);
        requestLog.setExecutionTime(executionTime);

        exchange.setProperty("executionTime", executionTime);
        exchange.getIn().setBody(requestLog);
    }
}

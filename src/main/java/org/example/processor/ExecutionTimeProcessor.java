package org.example.processor;//package com.apress.integration.processor;
//
//import com.apress.integration.entity.RequestLog;
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//import org.apache.camel.util.StopWatch;
//
//public class ExecutionTimeProcessor implements Processor {
//
//    @Override
//    public void process(Exchange exchange) throws Exception {
//        StopWatch stopWatch = exchange.getProperty("stopWatch", StopWatch.class);
//        long executionTime = stopWatch.taken();
//
//        RequestLog requestLog = exchange.getIn().getBody(RequestLog.class);
//        requestLog.setExecutionTime(executionTime);
//
//        exchange.setProperty("executionTime", executionTime);
//
//        exchange.getIn().setBody(requestLog);
//
//        //https://stackoverflow.com/questions/28670256/how-to-measure-elapsed-time-with-apache-camel
//    }
//}

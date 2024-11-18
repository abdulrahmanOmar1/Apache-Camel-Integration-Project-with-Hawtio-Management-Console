package org.example.route;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.example.Beans.TimeProcessorBean;
import org.example.entity.RequestLog;
import org.example.exception.TestRequestNameException;


import static org.example.constant.HTTPConstant.APP_JSON;

public class RequestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        configureRestApi();
        configureExceptionHandling();
        defineRestEndpoints();
        defineRoutes();
    }

    private void configureRestApi() {
        restConfiguration()
                .contextPath("/api/v1")
                .apiContextPath("/api-docs")
                .apiProperty("api.title", "Request API")
                .apiProperty("api.version", "1.0.0")
                .apiProperty("api.description", "API for managing requests")
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .dataFormatProperty("prettyPrint", "true");
    }

    private void configureExceptionHandling() {
        onException(TestRequestNameException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "Business Rule Violation: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("error:Invalid Request, message:${exception.message}"));

        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "Unexpected Exception: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("error:Internal Server Error, message:${exception.message}"));
    }

    private void defineRestEndpoints() {
        rest("/requests")
                .consumes(APP_JSON)
                .produces(APP_JSON)

                .post()
                .description("Create a new request")
                .type(RequestLog.class)
                .responseMessage().code(201).message("Request created").endResponseMessage()
                .to("direct:createRequest")

                .get()
                .description("Retrieve all requests")
                .responseMessage().code(200).message("Successful").endResponseMessage()
                .to("direct:getAllRequests")

                .get("/{id}")
                .description("Retrieve a request by ID")
                .responseMessage().code(200).message("Request found").endResponseMessage()
                .responseMessage().code(404).message("Request not found").endResponseMessage()
                .to("direct:getRequestById");
    }

    private void defineRoutes() {
        from("direct:createRequest")
                .routeId("create-request-route")
                .transacted()
                .bean(TimeProcessorBean.class, "startTime")
                .choice()
                .when(simple("${body.senderName} == 'test'"))
                .log("Request with senderName 'test' is being processed")
                .throwException(new TestRequestNameException("Request name 'test' is not allowed"))
                .otherwise()
                .to("jpa:" + RequestLog.class.getName())
                .end()
                .bean(TimeProcessorBean.class, "calculateExecutionTime")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setBody(simple("message :Request created successfully, executionTime:${exchangeProperty.executionTime} ms"));

        from("direct:getAllRequests")
                .routeId("get-all-requests-route")
                .to("jpa:" + RequestLog.class.getName() + "?query={{query.RequestLog}}");

        from("direct:getRequestById")
                .routeId("get-request-by-id-route")
                .log("Retrieving request with id = ${header.id}")
                .setBody(header("id").convertTo(Long.class))
                .to("jpa:" + RequestLog.class.getName() + "?findEntity=true")
                .choice()
                .when(body().isNull())
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setBody(simple("error:Request not found"))
                .otherwise()
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}

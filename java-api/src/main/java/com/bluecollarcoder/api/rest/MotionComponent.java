/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bluecollarcoder.api.rest;

import com.amazonaws.kinesis.producer.Configuration;
import com.amazonaws.kinesis.producer.KinesisProducer;
import com.amazonaws.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * @author wayne
 */
@Component
@Path("/motion")
@PropertySource("classpath:credentials.properties")
public class MotionComponent implements InitializingBean {
    
    private final AtomicLong completed = new AtomicLong(0);
    private final AtomicLong submitted = new AtomicLong(0);
    
    @Value("${aws.kinesis.access-key}")
    private String accessKey;
    @Value("${aws.kinesis.secret-key}")
    private String secretKey;
    private String STREAM = "kinesis-test";
    private String PARTITION = "user-1";
    private KinesisProducer producer;

    @Override
    public void afterPropertiesSet() throws Exception {
        Configuration config = new Configuration();
        config.setRegion("us-east-1");
        config.setAwsAccessKeyId(accessKey);
        config.setAwsSecretKey(secretKey);
        config.setMaxConnections(1);
        config.setRequestTimeout(30000);
        config.setRecordMaxBufferedTime(10000);
        producer = new KinesisProducer(config);
    }
    
    @GET
    public void ping(@Suspended final AsyncResponse resp) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (Exception ex) {}
            resp.resume("Alive - send motion\n" + submitted + " records sent\n" + completed + " records successful");
        }).start();
        System.out.println("Exiting ping");
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public void recordMotion(@Suspended final AsyncResponse resp) {
        FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
            @Override
            public void onSuccess(UserRecordResult v) {
                resp.resume("Successfully submitted motion record " + completed.incrementAndGet());
            }
            @Override
            public void onFailure(Throwable thrwbl) {
                resp.resume(thrwbl);
            }
        };
        ListenableFuture<UserRecordResult> future = producer.addUserRecord(STREAM, PARTITION, ByteBuffer.allocate(Long.SIZE).putLong(submitted.get()));
        Futures.addCallback(future, callback);
    }
    
}

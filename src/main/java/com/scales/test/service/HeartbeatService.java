package com.scales.test.service;

import com.scales.test.service.ip.IServerType;
import com.scales.test.service.ip.ServerTypeEc2;
import com.scales.test.service.ip.ServerTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HeartbeatService {

    @Value("${config.heartbeat-queue.url}")
    private String queueUrl;

    @Value("${config.server-type}")
    private String serverType;

    @Value("${server.port}")
    private String port;

    @Autowired
    private ServerTypeFactory serverTypeFactory;

    private String instanceIP;
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);
    private final SqsClient sqsClient;

    public HeartbeatService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void fetchPublicIp() {
        IServerType serverTypeInstance = serverTypeFactory.getServerType(serverType);

        int retries = 3; // Maximum retries
        while (retries > 0) {
            try {
                this.instanceIP = serverTypeInstance.getIp();
                logger.info("Successfully fetched public IP for {} server: {}", serverType, instanceIP);
                return; // Exit the method on successful fetch
            } catch (Exception e) {
                retries--;
                logger.error("Attempt to fetch public IP failed: {}. Retries left: {}", e.getMessage(), retries);
                if (retries == 0) {
                    logger.error("Failed to fetch public IP after maximum retries. Setting instanceIP to null.");
                    this.instanceIP = null;
                }
            }
        }
    }

    public void sendHeartbeat() {
        try {
            if (instanceIP == null || instanceIP.isEmpty()) {
                fetchPublicIp();
                if (instanceIP == null || instanceIP.isEmpty()) {
                    logger.warn("IP not set.");
                    return;
                }

            }

            String message = "http://" + instanceIP + ":" + port;
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build();
            sqsClient.sendMessage(sendMessageRequest);

            logger.info("Heartbeat sent with message: {}", message);
        } catch (Exception e) {
            logger.error("Failed to publish to queue: {}", e.getMessage());
        }
    }

}

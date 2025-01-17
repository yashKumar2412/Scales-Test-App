package com.scales.test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Value("${server.port}")
    private String port;

    private String instanceIP;
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);
    private final SqsClient sqsClient;

    public HeartbeatService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void fetchPublicIp() {
        int retries = 3; // Maximum retries
        while (retries > 0) {
            try {
                // Step 1: Fetch the IMDSv2 token
                URL tokenUrl = new URL("http://169.254.169.254/latest/api/token");
                HttpURLConnection tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
                tokenConnection.setRequestMethod("PUT");
                tokenConnection.setRequestProperty("X-aws-ec2-metadata-token-ttl-seconds", "21600");
                tokenConnection.setDoOutput(true);

                BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
                String token = tokenReader.readLine();
                tokenReader.close();

                // Step 2: Use the token to fetch the public IP
                URL ipUrl = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
                HttpURLConnection ipConnection = (HttpURLConnection) ipUrl.openConnection();
                ipConnection.setRequestMethod("GET");
                ipConnection.setRequestProperty("X-aws-ec2-metadata-token", token);

                BufferedReader ipReader = new BufferedReader(new InputStreamReader(ipConnection.getInputStream()));
                this.instanceIP = ipReader.readLine();
                ipReader.close();

                logger.info("Successfully fetched public IP: {}", instanceIP);
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

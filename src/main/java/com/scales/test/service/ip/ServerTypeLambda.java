package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

@Service
public class ServerTypeLambda implements IServerType {

    private static final String FUNCTION_URL = System.getenv("LAMBDA_FUNCTION_URL");

    @Override
    public String getIp() throws Exception {
        if (FUNCTION_URL == null || FUNCTION_URL.isEmpty()) {
            throw new IllegalStateException("LAMBDA_FUNCTION_URL is not set. Ensure the environment variable is configured.");
        }

        // Return the function URL
        return FUNCTION_URL;
    }
}
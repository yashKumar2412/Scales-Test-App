package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ServerTypeEcs implements IServerType {

    private static final String ECS_METADATA_URI = System.getenv("ECS_CONTAINER_METADATA_URI");

    @Override
    public String getIp() throws Exception {
        if (ECS_METADATA_URI == null) {
            throw new IllegalStateException("ECS_CONTAINER_METADATA_URI is not set. Ensure the task is running on ECS.");
        }

        // Step 1: Fetch container metadata
        URL metadataUrl = new URL(ECS_METADATA_URI);
        HttpURLConnection metadataConnection = (HttpURLConnection) metadataUrl.openConnection();
        metadataConnection.setRequestMethod("GET");

        BufferedReader metadataReader = new BufferedReader(new InputStreamReader(metadataConnection.getInputStream()));
        StringBuilder metadataResponse = new StringBuilder();
        String line;

        while ((line = metadataReader.readLine()) != null) {
            metadataResponse.append(line);
        }
        metadataReader.close();

        // Step 2: Parse the IP from the metadata response
        String metadataJson = metadataResponse.toString();

        return extractIpFromMetadata(metadataJson);
    }

    private String extractIpFromMetadata(String metadataJson) throws Exception {
        // Parse JSON to extract private IP (simplified example, use a JSON library like Jackson for robustness)
        String ipKey = "\"IPv4Addresses\":";
        int ipIndex = metadataJson.indexOf(ipKey);
        if (ipIndex == -1) {
            throw new IllegalStateException("No IPv4Addresses field found in metadata.");
        }

        int start = metadataJson.indexOf("[", ipIndex) + 1;
        int end = metadataJson.indexOf("]", ipIndex);
        String ip = metadataJson.substring(start, end).replace("\"", "").trim();

        if (ip.isEmpty()) {
            throw new IllegalStateException("No IP address found in ECS metadata.");
        }

        return ip;
    }
}
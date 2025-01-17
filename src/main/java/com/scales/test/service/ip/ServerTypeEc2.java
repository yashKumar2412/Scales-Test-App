package com.scales.test.service.ip;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ServerTypeEc2 implements IServerType {

    private static final String METADATA_URL = "http://169.254.169.254/latest/meta-data/public-ipv4";

    @Override
    public String getIp() throws Exception {
        // Step 1: Check if the metadata service is reachable
        if (!isMetadataServiceAvailable()) {
            throw new IllegalStateException("EC2 metadata service is not available. Ensure the instance is running on EC2.");
        }

        // Step 2: Fetch the public IP from the metadata service
        URL url = new URL(METADATA_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String ip = reader.readLine();
        reader.close();

        if (ip == null || ip.isEmpty()) {
            throw new IllegalStateException("Failed to fetch public IP from EC2 metadata service.");
        }

        return ip;
    }

    private boolean isMetadataServiceAvailable() {
        try {
            URL url = new URL("http://169.254.169.254/latest/meta-data/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
